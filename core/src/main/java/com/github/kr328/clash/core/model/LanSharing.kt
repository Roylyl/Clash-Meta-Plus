package com.github.kr328.clash.core.model

/** Capture the current listeners in the persistent override instead of following profiles. */
fun ConfigurationOverride.pinLanSharing(effective: UiConfiguration) {
    val fixedPort = sequenceOf(mixedPort, effective.mixedPort,
        httpPort ?: effective.httpPort, socksPort ?: effective.socksPort)
        .filterNotNull()
        .firstOrNull { it in 1..65535 } ?: 7890

    mixedPort = fixedPort
    // Pin the other listeners too: a new profile must not claim the mixed listener's port.
    httpPort = (httpPort ?: effective.httpPort).takeUnless { it == fixedPort } ?: 0
    socksPort = (socksPort ?: effective.socksPort).takeUnless { it == fixedPort } ?: 0
    allowLan = allowLan ?: effective.allowLan
    bindAddress = bindAddress ?: effective.bindAddress.ifBlank { "0.0.0.0" }
}
