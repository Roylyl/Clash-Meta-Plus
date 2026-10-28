package com.github.kr328.clash.update

import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Blocking network API: call from an IO dispatcher or a background worker. */
object ReleaseRepository {
    private const val PAGE_SIZE = 100
    private const val MAX_PAGES = 10
    private const val MAX_RESPONSE_BYTES = 8 * 1024 * 1024

    @Throws(IOException::class)
    fun fetchLatest(currentVersion: String, supportedAbis: List<String>): ReleaseInfo? {
        if (ReleaseVersion.parseInstalled(currentVersion) == null) {
            throw IOException("Unrecognized installed version: $currentVersion")
        }
        val releases = mutableListOf<PublishedRelease>()
        for (page in 1..MAX_PAGES) {
            val values = fetchPage(page)
            try {
                for (index in 0 until values.length()) {
                    val release = values.getJSONObject(index)
                    val assets = release.optJSONArray("assets") ?: JSONArray()
                    releases += PublishedRelease(
                        tagName = release.getString("tag_name"),
                        body = if (release.isNull("body")) "" else release.optString("body", ""),
                        draft = release.optBoolean("draft", false),
                        prerelease = release.optBoolean("prerelease", false),
                        assets = (0 until assets.length()).map { assetIndex ->
                            val asset = assets.getJSONObject(assetIndex)
                            ReleaseAsset(asset.getString("name"),
                                asset.getString("browser_download_url"), asset.optLong("size", 0))
                        },
                    )
                }
            } catch (error: JSONException) {
                throw IOException("Invalid GitHub release metadata", error)
            }
            if (values.length() < PAGE_SIZE) {
                return ReleaseSelection.findUpdate(releases, currentVersion, supportedAbis)
            }
        }
        // Do not misreport up-to-date when an unusually large release history was truncated.
        throw IOException("Too many release pages to determine the newest version")
    }

    private fun fetchPage(page: Int): JSONArray {
        val connection = URL("https://api.github.com/repos/${ReleaseSelection.REPOSITORY}" +
            "/releases?per_page=$PAGE_SIZE&page=$page").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            connection.setRequestProperty("User-Agent", "Clash-Meta-Plus-UpdateChecker")
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("GitHub release check failed (HTTP $status)")
            }
            val body = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count == -1) break
                    if (output.size() + count > MAX_RESPONSE_BYTES) {
                        throw IOException("GitHub release response is too large")
                    }
                    output.write(buffer, 0, count)
                }
                output.toString("UTF-8")
            }
            return try {
                JSONArray(body)
            } catch (error: JSONException) {
                throw IOException("Invalid GitHub release response", error)
            }
        } finally {
            connection.disconnect()
        }
    }
}
