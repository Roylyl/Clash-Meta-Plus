package com.github.kr328.clash.update

import java.math.BigInteger
import java.net.URI
import java.util.Locale

/** Release tags are numeric versions, not strings or publication dates. */
class ReleaseVersion private constructor(private val parts: List<BigInteger>) : Comparable<ReleaseVersion> {
    override fun compareTo(other: ReleaseVersion): Int {
        for (index in 0 until maxOf(parts.size, other.parts.size)) {
            val result = (parts.getOrNull(index) ?: BigInteger.ZERO)
                .compareTo(other.parts.getOrNull(index) ?: BigInteger.ZERO)
            if (result != 0) return result
        }
        return 0
    }

    override fun toString(): String = "V" + parts.joinToString(".")

    companion object {
        private val pattern = Regex("[vV]?[0-9]+(?:\\.[0-9]+)+")

        fun parse(tag: String): ReleaseVersion? {
            if (tag.length > 100 || !pattern.matches(tag)) return null
            return ReleaseVersion(tag.removePrefix("v").removePrefix("V")
                .split('.').map { it.toBigInteger() })
        }

        fun parseInstalled(versionName: String): ReleaseVersion? =
            parse(versionName.removeSuffix(".debug"))
    }
}

data class ReleaseInfo(
    val tagName: String,
    val versionName: String,
    val body: String,
    val assetName: String,
    val assetUrl: String,
    val assetSize: Long,
)

data class ReleaseAsset(val name: String, val url: String, val size: Long)

data class PublishedRelease(
    val tagName: String,
    val body: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val assets: List<ReleaseAsset>,
)

object ReleaseSelection {
    const val REPOSITORY = "Roylyl/Clash-Meta-Plus"
    private val knownAbis = listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")

    fun findUpdate(
        releases: List<PublishedRelease>,
        currentVersion: String,
        supportedAbis: List<String>,
    ): ReleaseInfo? {
        val current = requireNotNull(ReleaseVersion.parseInstalled(currentVersion)) {
            "Unrecognized installed version: $currentVersion"
        }
        return releases.asSequence()
            .filterNot { it.draft || it.prerelease }
            .mapNotNull { release ->
                val version = ReleaseVersion.parse(release.tagName) ?: return@mapNotNull null
                if (version <= current) return@mapNotNull null
                val asset = selectAsset(release.assets, supportedAbis) ?: return@mapNotNull null
                version to ReleaseInfo(release.tagName, version.toString(), release.body,
                    asset.name, asset.url, asset.size)
            }
            .maxByOrNull { it.first }
            ?.second
    }

    fun selectAsset(assets: List<ReleaseAsset>, supportedAbis: List<String>): ReleaseAsset? {
        val candidates = assets.filter { asset ->
            asset.size > 0 && asset.name.endsWith(".apk", ignoreCase = true) &&
                !Regex("(?:^|[-_.])(?:debug|alpha)(?:[-_.]|$)", RegexOption.IGNORE_CASE)
                    .containsMatchIn(asset.name) && isTrustedAssetUrl(asset.url)
        }
        // Device preference order matters, e.g. arm64 before the compatible 32-bit ABI.
        for (abi in supportedAbis) {
            if (abi !in knownAbis) continue
            candidates.firstOrNull { assetAbi(it.name) == abi }?.let { return it }
        }
        return candidates.firstOrNull {
            Regex("(?:^|[-_.])universal(?:[-_.]|$)", RegexOption.IGNORE_CASE).containsMatchIn(it.name)
                && assetAbi(it.name) == null
        }
    }

    fun isTrustedAssetUrl(value: String): Boolean = runCatching {
        val uri = URI(value)
        val prefix = "/$REPOSITORY/releases/download/"
        uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals("github.com", ignoreCase = true) &&
            uri.userInfo == null && uri.port in listOf(-1, 443) &&
            uri.query == null && uri.fragment == null &&
            uri.path.startsWith(prefix) && uri.normalize().path == uri.path &&
            uri.path.removePrefix(prefix).split('/').let { parts ->
                parts.size == 2 && parts.all { it.isNotEmpty() }
            }
    }.getOrDefault(false)

    private fun assetAbi(name: String): String? {
        val lower = name.lowercase(Locale.ROOT)
        // Check x86_64 before x86 so the 64-bit token is never treated as 32-bit.
        return knownAbis.firstOrNull { abi ->
            Regex("(?:^|[-_.])${Regex.escape(abi)}(?:[-_.]|$)").containsMatchIn(lower)
        }
    }
}
