package com.github.kr328.clash.util

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.github.kr328.clash.common.compat.startForegroundServiceCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.service.ClashService
import com.github.kr328.clash.service.StatusProvider
import com.github.kr328.clash.service.TunService
import com.github.kr328.clash.service.util.sendBroadcastSelf

fun Context.startClashService(): Intent? {
    val startTun = UiStore(this).enableVpn

    if (startTun) {
        val vpnRequest = VpnService.prepare(this)
        if (vpnRequest != null)
            return vpnRequest
    }

    val wasRequested = StatusProvider.shouldStartClashOnBoot
    StatusProvider.shouldStartClashOnBoot = true
    try {
        startForegroundServiceCompat(
            if (startTun) TunService::class.intent else ClashService::class.intent
        )
    } catch (e: RuntimeException) {
        StatusProvider.shouldStartClashOnBoot = wasRequested
        throw e
    }

    return null
}

fun Context.stopClashService() {
    // Clear recovery before stopping so an already queued sticky restart cannot revive it.
    StatusProvider.shouldStartClashOnBoot = false
    // A VPN can remain bound by Android after stopService, so explicitly close its
    // runtime too. The receiver ignores this request if a newer Start supersedes it.
    sendBroadcastSelf(Intent(Intents.ACTION_CLASH_REQUEST_STOP))
    // Also cancel early starts that have not created their runtime receiver yet.
    stopService(TunService::class.intent)
    stopService(ClashService::class.intent)
}