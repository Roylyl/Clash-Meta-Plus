package com.github.kr328.clash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.StatusProvider
import com.github.kr328.clash.util.startClashService

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (StatusProvider.shouldStartClashOnBoot) {
                    try {
                        // A revoked VPN needs fresh user consent; never restart it in a loop.
                        if (context.startClashService() != null)
                            StatusProvider.shouldStartClashOnBoot = false
                    } catch (e: RuntimeException) {
                        // Android/OEM background restrictions may reject boot starts.
                        Log.w("Clash restart denied: ${e.message}", e)
                    }
                }
            }
        }
    }
}