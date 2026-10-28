package com.github.kr328.clash.update

import android.app.DownloadManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.FileProvider
import com.github.kr328.clash.common.compat.versionCodeCompat
import java.io.File
import java.io.IOException
import java.util.UUID

data class DownloadState(
    val id: Long,
    val status: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val tag: String,
    val error: String? = null,
)

/** DownloadManager owns the transfer, so leaving the activity does not cancel it. */
object UpdateDownload {
    private const val MIME_APK = "application/vnd.android.package-archive"
    private const val KEY_ID = "download_id"
    private const val KEY_TAG = "release_tag"
    private const val KEY_URL = "asset_url"
    private const val KEY_FILE = "file_name"
    private const val KEY_INSTALLED_CODE = "installed_version_code"
    private val fileNamePattern = Regex("update-[a-f0-9-]+\\.apk")

    private fun preferences(context: Context): SharedPreferences =
        context.getSharedPreferences("application_update_download", Context.MODE_PRIVATE)

    private fun manager(context: Context): DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: throw IOException("系统下载服务不可用，请启用下载管理器后重试。")

    @Synchronized
    fun start(context: Context, release: ReleaseInfo): Long {
        val uri = Uri.parse(release.assetUrl)
        val path = uri.pathSegments
        if (uri.scheme != "https" || uri.host != "github.com" || uri.userInfo != null ||
            (uri.port != -1 && uri.port != 443) || uri.query != null || uri.fragment != null ||
            path.size != 6 || path.take(4) != listOf("Roylyl", "Clash-Meta-Plus", "releases", "download") ||
            path[4] != release.tagName || path[5] != release.assetName ||
            !release.assetName.endsWith(".apk", ignoreCase = true)
        ) {
            throw IOException("更新包地址无效，请重新检查更新。")
        }

        val prefs = preferences(context)
        val previous = query(context)
        if (previous != null && previous.status != DownloadManager.STATUS_FAILED) {
            if (prefs.getString(KEY_URL, null) == release.assetUrl) return previous.id
            if (previous.status != DownloadManager.STATUS_SUCCESSFUL) {
                throw IOException("另一个更新正在下载，请先取消当前下载。")
            }
        }
        if (previous != null) cancel(context)

        val directory = context.getExternalFilesDir("updates")
            ?: throw IOException("下载存储空间不可用，请稍后重试。")
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("无法创建更新下载目录，请检查可用存储空间。")
        }
        val fileName = "update-${UUID.randomUUID()}.apk"
        val destination = File(directory, fileName)
        val request = DownloadManager.Request(uri)
            .setTitle("Clash Meta Plus ${release.tagName}")
            .setDescription("正在下载应用更新")
            .setMimeType(MIME_APK)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destination))

        val downloads = manager(context)
        val installedCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCodeCompat
        val id = try {
            downloads.enqueue(request)
        } catch (e: RuntimeException) {
            throw IOException("无法启动下载，请检查系统下载管理器和存储空间。", e)
        }
        // Persist synchronously before returning so a completion broadcast can identify the task.
        if (!prefs.edit().putLong(KEY_ID, id).putString(KEY_TAG, release.tagName)
                .putString(KEY_URL, release.assetUrl).putString(KEY_FILE, fileName)
                .putLong(KEY_INSTALLED_CODE, installedCode).commit()
        ) {
            downloads.remove(id)
            throw IOException("无法保存下载任务，请稍后重试。")
        }
        return id
    }

    @Synchronized
    fun query(context: Context): DownloadState? {
        val prefs = preferences(context)
        val id = prefs.getLong(KEY_ID, -1L)
        if (id < 0) return null
        val tag = prefs.getString(KEY_TAG, "") ?: ""
        val downloads = manager(context)
        val cursor = try {
            downloads.query(DownloadManager.Query().setFilterById(id))
        } catch (e: RuntimeException) {
            throw IOException("无法读取下载进度，请检查系统下载管理器。", e)
        }
        cursor?.use {
            if (it.moveToFirst()) {
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                if (status == DownloadManager.STATUS_SUCCESSFUL && downloadedFile(context)?.isFile != true) {
                    return DownloadState(id, DownloadManager.STATUS_FAILED, downloaded, total, tag,
                        "下载的更新包已不存在，请重新下载。")
                }
                if (status == DownloadManager.STATUS_SUCCESSFUL && isAlreadyInstalled(context, prefs)) {
                    // Installation replaces this process; discard its old download on the next launch.
                    cancel(context)
                    return null
                }
                return DownloadState(id, status, downloaded, total, tag,
                    if (status == DownloadManager.STATUS_FAILED) failureMessage(reason) else null)
            }
        }
        return DownloadState(id, DownloadManager.STATUS_FAILED, 0, 0, tag,
            "下载任务已被移除，请重新下载。")
    }

    @Synchronized
    fun cancel(context: Context) {
        val prefs = preferences(context)
        val id = prefs.getLong(KEY_ID, -1L)
        if (id >= 0) manager(context).remove(id)
        downloadedFile(context)?.delete()
        prefs.edit().clear().commit()
    }

    /** Call from a foreground activity after obtaining permission to install unknown apps. */
    @Synchronized
    fun installerIntent(context: Context): Intent {
        val state = query(context)
        if (state?.status != DownloadManager.STATUS_SUCCESSFUL) {
            throw IOException("更新包尚未下载完成。")
        }
        val file = downloadedFile(context)
            ?: throw IOException("找不到已下载的更新包，请重新下载。")
        val packages = context.packageManager
        val archive = packages.getPackageArchiveInfo(file.absolutePath, 0)
            ?: throw IOException("无法读取更新包，请重新下载。")
        if (archive.packageName != context.packageName) {
            throw IOException("更新包与当前应用不匹配，无法安装。")
        }
        val installed = packages.getPackageInfo(context.packageName, 0)
        if (archive.versionCodeCompat <= installed.versionCodeCompat) {
            throw IOException("更新包的安装版本号未高于当前版本，请等待新的发行包。")
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.update-files", file)
        return Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, MIME_APK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .apply { clipData = ClipData.newRawUri("application update", uri) }
    }

    private fun downloadedFile(context: Context): File? {
        val name = preferences(context).getString(KEY_FILE, null) ?: return null
        if (!fileNamePattern.matches(name)) return null
        val directory = context.getExternalFilesDir("updates") ?: return null
        return File(directory, name)
    }

    private fun isAlreadyInstalled(context: Context, prefs: SharedPreferences): Boolean {
        val packages = context.packageManager
        val installed = packages.getPackageInfo(context.packageName, 0).versionCodeCompat
        val previous = prefs.getLong(KEY_INSTALLED_CODE, installed)
        if (installed <= previous) return false
        val file = downloadedFile(context) ?: return false
        val archive = packages.getPackageArchiveInfo(file.absolutePath, 0) ?: return false
        return archive.packageName == context.packageName && archive.versionCodeCompat <= installed
    }

    private fun failureMessage(reason: Int): String = when (reason) {
        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "存储空间不足，请清理空间后重新下载。"
        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "下载存储空间不可用，请稍后重试。"
        DownloadManager.ERROR_CANNOT_RESUME -> "下载中断且无法续传，请重新下载。"
        DownloadManager.ERROR_HTTP_DATA_ERROR, DownloadManager.ERROR_UNHANDLED_HTTP_CODE,
        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "下载连接失败，请检查网络后重新下载。"
        else -> "更新包下载失败，请重试。（$reason）"
    }
}
