package com.github.kr328.clash.update

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.kr328.clash.R
import kotlinx.coroutines.*

class UpdateActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var status: TextView
    private lateinit var details: TextView
    private lateinit var progress: ProgressBar
    private lateinit var action: Button
    private lateinit var check: Button
    private lateinit var cancelDownload: Button
    private var monitor: Job? = null
    private var offeredDownload = -1L
    private var visible = false

    private val installPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT < 26 || packageManager.canRequestPackageInstalls()) {
            openInstaller()
        } else {
            status.setText(R.string.update_install_permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.update_title)
        offeredDownload = savedInstanceState?.getLong("offered_download", -1) ?: -1
        val spacing = (20 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(spacing, spacing, spacing, spacing)
        }
        fun label(text: CharSequence = "") = TextView(this).apply {
            this.text = text
            textSize = 16f
            setPadding(0, spacing / 2, 0, spacing / 2)
            content.addView(this)
        }
        label(getString(R.string.update_current, com.github.kr328.clash.BuildConfig.VERSION_NAME))
        status = label(getString(R.string.update_checking))
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = View.GONE
            content.addView(this)
        }
        action = Button(this).apply { visibility = View.GONE; content.addView(this) }
        cancelDownload = Button(this).apply {
            setText(R.string.update_cancel_download)
            visibility = View.GONE
            content.addView(this)
            setOnClickListener {
                monitor?.cancel()
                launch {
                    try {
                    withContext(Dispatchers.IO) { UpdateDownload.cancel(this@UpdateActivity) }
                    beginCheckOrDownload()
                    } catch (_: Exception) { showFailure(R.string.update_download_failed) }
                }
            }
        }
        check = Button(this).apply {
            setText(com.github.kr328.clash.design.R.string.check_for_updates)
            content.addView(this)
            setOnClickListener {
                monitor?.cancel()
                monitor = launch {
                    try {
                    val state = withContext(Dispatchers.IO) { UpdateDownload.query(this@UpdateActivity) }
                    if (state?.status == DownloadManager.STATUS_FAILED) {
                        withContext(Dispatchers.IO) { UpdateDownload.cancel(this@UpdateActivity) }
                    }
                    checkRelease()
                    } catch (e: CancellationException) { throw e }
                    catch (_: Exception) { showFailure(R.string.update_download_failed) }
                }
            }
        }
        label(getString(R.string.update_schedule_help))
        details = label().apply { setTextIsSelectable(true) }
        setContentView(ScrollView(this).apply { addView(content) })
        UpdateNotifications.dismiss(this)
    }

    override fun onStart() {
        super.onStart()
        visible = true
        beginCheckOrDownload()
    }

    override fun onStop() {
        visible = false
        monitor?.cancel()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("offered_download", offeredDownload)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    private fun beginCheckOrDownload() {
        monitor?.cancel()
        monitor = launch {
            try {
                var state = withContext(Dispatchers.IO) { UpdateDownload.query(this@UpdateActivity) }
                if (state == null) {
                    checkRelease()
                    return@launch
                }
                while (isActive && state != null) {
                    renderDownload(state)
                    if (state.status == DownloadManager.STATUS_SUCCESSFUL ||
                        state.status == DownloadManager.STATUS_FAILED) break
                    delay(1000)
                    state = withContext(Dispatchers.IO) { UpdateDownload.query(this@UpdateActivity) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showFailure(R.string.update_download_failed)
            }
        }
    }

    private suspend fun checkRelease() {
        status.setText(R.string.update_checking)
        check.isEnabled = false
        action.visibility = View.GONE
        cancelDownload.visibility = View.GONE
        progress.visibility = View.GONE
        details.text = ""
        try {
            val found = withContext(Dispatchers.IO) {
                ReleaseRepository.fetchLatest(com.github.kr328.clash.BuildConfig.VERSION_NAME, Build.SUPPORTED_ABIS.toList())
            }
            if (found == null) {
                status.setText(R.string.update_latest)
            } else {
                status.text = getString(R.string.update_available, found.versionName)
                details.text = getString(R.string.update_asset, found.assetName) + "\n\n" + found.body
                action.setText(R.string.update_download)
                action.visibility = View.VISIBLE
                action.isEnabled = true
                action.setOnClickListener { startDownload(found) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            status.setText(R.string.update_check_failed)
        } finally {
            check.isEnabled = true
        }
    }

    private fun startDownload(found: ReleaseInfo) {
        action.isEnabled = false
        check.isEnabled = false
        launch {
            try {
                withContext(Dispatchers.IO) { UpdateDownload.start(this@UpdateActivity, found) }
                offeredDownload = -1
                beginCheckOrDownload()
            } catch (_: Exception) {
                action.isEnabled = true
                showFailure(R.string.update_download_failed)
            }
        }
    }

    private fun renderDownload(state: DownloadState) {
        when (state.status) {
            DownloadManager.STATUS_SUCCESSFUL -> {
                status.setText(R.string.update_download_complete)
                progress.visibility = View.GONE
                cancelDownload.visibility = View.GONE
                check.isEnabled = true
                action.setText(R.string.update_install)
                action.visibility = View.VISIBLE
                action.isEnabled = true
                action.setOnClickListener { requestInstall() }
                if (visible && offeredDownload != state.id) {
                    offeredDownload = state.id
                    requestInstall()
                }
            }
            DownloadManager.STATUS_FAILED -> showFailure(R.string.update_download_failed)
            else -> {
                val percent = if (state.totalBytes > 0) (state.downloadedBytes * 100 / state.totalBytes).toInt() else 0
                status.text = if (state.totalBytes > 0) getString(R.string.update_downloading, state.tag, percent)
                    else getString(R.string.update_download_waiting)
                progress.visibility = View.VISIBLE
                progress.isIndeterminate = state.totalBytes <= 0
                progress.progress = percent
                action.visibility = View.GONE
                cancelDownload.visibility = View.VISIBLE
                check.isEnabled = false
            }
        }
    }

    private fun requestInstall() {
        if (Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
            AlertDialog.Builder(this)
                .setMessage(R.string.update_install_permission)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        installPermission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:$packageName")))
                    } catch (e: Exception) {
                        status.text = getString(R.string.update_install_failed, e.localizedMessage.orEmpty())
                    }
                }.setNegativeButton(android.R.string.cancel, null).show()
        } else {
            openInstaller()
        }
    }

    private fun openInstaller() {
        launch {
            try {
                val intent = withContext(Dispatchers.IO) { UpdateDownload.installerIntent(this@UpdateActivity) }
                if (visible) {
                    UpdateNotifications.dismiss(this@UpdateActivity)
                    startActivity(intent)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                status.text = getString(R.string.update_install_failed, e.localizedMessage.orEmpty())
            }
        }
    }

    private fun showFailure(message: Int) {
        status.setText(message)
        progress.visibility = View.GONE
        cancelDownload.visibility = View.GONE
        action.visibility = View.GONE
        check.isEnabled = true
    }
}

