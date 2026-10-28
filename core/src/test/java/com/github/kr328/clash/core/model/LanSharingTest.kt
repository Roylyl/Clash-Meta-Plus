package com.github.kr328.clash.core.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class LanSharingTest {
    @Test
    fun savedPortsSurviveRestartAndDifferentProfile() {
        val override = ConfigurationOverride()
        override.pinLanSharing(UiConfiguration(
            mixedPort = 7890, httpPort = 7891, socksPort = 7892,
            allowLan = true, bindAddress = "0.0.0.0",
        ))

        // Use the same persistence format as Clash, including omission of null defaults.
        val json = Json { encodeDefaults = false }
        val saved = json.encodeToString(ConfigurationOverride.serializer(), override)
        val restored = json.decodeFromString(ConfigurationOverride.serializer(), saved)
        restored.pinLanSharing(UiConfiguration(
            mixedPort = 1080, httpPort = 7890, socksPort = 1081,
            allowLan = false, bindAddress = "127.0.0.1",
        ))

        assertEquals(7890, restored.mixedPort)
        assertEquals(7891, restored.httpPort)
        assertEquals(7892, restored.socksPort)
        assertEquals(true, restored.allowLan)
        assertEquals("0.0.0.0", restored.bindAddress)
    }

    @Test
    fun httpOnlyProfileKeepsItsPortWithoutDuplicateListener() {
        val override = ConfigurationOverride()
        override.pinLanSharing(UiConfiguration(httpPort = 8080, socksPort = 1080))
        assertEquals(8080, override.mixedPort)
        assertEquals(0, override.httpPort)
        assertEquals(1080, override.socksPort)

        override.mixedPort = 1080
        override.pinLanSharing(UiConfiguration(httpPort = 8080, socksPort = 1080))
        assertEquals(1080, override.mixedPort)
        assertEquals(0, override.httpPort)
        assertEquals(0, override.socksPort)
    }

    @Test
    fun disabledListenersStayExplicitlyDisabledAfterPersistence() {
        val override = ConfigurationOverride()
        override.pinLanSharing(UiConfiguration())
        assertEquals(7890, override.mixedPort)
        assertEquals(false, override.allowLan)

        val saved = Json.encodeToString(ConfigurationOverride.serializer(), override)
        val restored = Json.decodeFromString(ConfigurationOverride.serializer(), saved)
        restored.pinLanSharing(UiConfiguration(httpPort = 7890, socksPort = 7890))
        assertEquals(0, restored.httpPort)
        assertEquals(0, restored.socksPort)
        assertEquals("0.0.0.0", restored.bindAddress)
    }

    @Test
    fun explicitOverrideWinsOverActiveProfile() {
        val override = ConfigurationOverride(mixedPort = 8888, allowLan = false)
        override.pinLanSharing(UiConfiguration(mixedPort = 7890, allowLan = true))
        assertEquals(8888, override.mixedPort)
        assertEquals(false, override.allowLan)
    }
}
