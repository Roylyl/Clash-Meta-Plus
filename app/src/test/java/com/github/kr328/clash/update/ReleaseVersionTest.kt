package com.github.kr328.clash.update

import org.junit.Assert.*
import org.junit.Test

class ReleaseVersionTest {
    @Test
    fun versionsUseNumericComponentsIncludingTheRequestedVersion() {
        assertTrue(ReleaseVersion.parse("V170.10")!! > ReleaseVersion.parse("V170.9")!!)
        assertTrue(ReleaseVersion.parse("V1702.0")!! > ReleaseVersion.parse("V172.0")!!)
        assertEquals(0, ReleaseVersion.parse("v170.1")!!
            .compareTo(ReleaseVersion.parse("V170.1.0")!!))
        assertEquals("V1702.0", ReleaseVersion.parseInstalled("V1702.0.debug").toString())
        assertNull(ReleaseVersion.parse("V1702.0.debug"))
        assertNull(ReleaseVersion.parse("V1702.0-beta.1"))
        assertNull(ReleaseVersion.parse("latest"))
    }

    @Test
    fun highestNewerCompatibleReleaseWinsRegardlessOfPublicationOrder() {
        val releases = listOf(release("V170.2"), release("V172.0"), release("V170.10"),
            release("V9999.0", prerelease = true), release("V8888.0", draft = true),
            release("latest"), release("V173.0", abi = "x86"))
        val update = ReleaseSelection.findUpdate(releases, "V170.1", listOf("arm64-v8a"))
        assertEquals("V172.0", update?.tagName)
        assertNull(ReleaseSelection.findUpdate(releases, "V1702.0", listOf("arm64-v8a")))
    }

    @Test
    fun architectureSelectionHonorsDevicePreferenceAndUniversalFallback() {
        val assets = listOf(asset("armeabi-v7a"), asset("x86_64"), asset("universal"), asset("arm64-v8a"))
        assertEquals("app-arm64-v8a.apk", ReleaseSelection.selectAsset(assets,
            listOf("arm64-v8a", "armeabi-v7a"))?.name)
        assertEquals("app-universal.apk", ReleaseSelection.selectAsset(assets, listOf("x86"))?.name)
        assertNull(ReleaseSelection.selectAsset(listOf(asset("x86_64")), listOf("x86")))
        assertNull(ReleaseSelection.selectAsset(listOf(asset("arm64-v8a")), listOf("x86_64")))
    }

    @Test
    fun rejectsUntrustedOrNonReleaseDownloadLocations() {
        val official = asset("arm64-v8a")
        assertTrue(ReleaseSelection.isTrustedAssetUrl(official.url))
        val invalidUrls = listOf(
            official.url.replace("https://", "http://"),
            official.url.replace("github.com", "github.com.example.org"),
            official.url.replace("Roylyl/", "another-owner/"),
            official.url.replace("/releases/download/", "/releases/tag/"),
            official.url + "?redirect=example.org",
            official.url.replace("/V172.0/", "/../"),
        )
        invalidUrls.forEach { assertFalse(it, ReleaseSelection.isTrustedAssetUrl(it)) }
        assertNull(ReleaseSelection.selectAsset(listOf(
            official.copy(name = "app-arm64-v8a-debug.apk"),
            official.copy(size = 0),
            official.copy(name = "app-arm64-v8a.apk.zip"),
        ), listOf("arm64-v8a")))
    }

    private fun release(
        tag: String,
        abi: String = "arm64-v8a",
        draft: Boolean = false,
        prerelease: Boolean = false,
    ) = PublishedRelease(tag, "Changes", draft, prerelease, listOf(asset(abi)))

    private fun asset(abi: String) = ReleaseAsset("app-$abi.apk",
        "https://github.com/Roylyl/Clash-Meta-Plus/releases/download/V172.0/app-$abi.apk", 1024)
}
