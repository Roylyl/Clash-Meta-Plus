package com.github.kr328.clash.service.clash.module

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.PowerManager
import androidx.core.content.getSystemService
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.PreferenceProvider
import com.github.kr328.clash.service.store.ServiceStore
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class SuspendModule(service: Service) : Module<Unit>(service) {
    // Continuous network forwarding is user-controlled and has no fixed duration. The
    // lock is owned by this module and released on setting changes and every exit path.
    @SuppressLint("WakelockTimeout")
    override suspend fun run() {
        val powerManager = service.getSystemService<PowerManager>()
        val preferences = PreferenceProvider.createSharedPreferencesFromContext(service)
        val preferencesChanged = Channel<Unit>(Channel.CONFLATED)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key == ServiceStore.KEY_AGGRESSIVE_KEEP_ALIVE)
                preferencesChanged.trySend(Unit)
        }
        var wakeLock: PowerManager.WakeLock? = null

        val screenToggle = receiveBroadcast(false, Channel.CONFLATED) {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)
        try {
            while (true) {
                val keepAlive = preferences.getBoolean(ServiceStore.KEY_AGGRESSIVE_KEEP_ALIVE, true)
                if (keepAlive) {
                    if (wakeLock == null) {
                        wakeLock = powerManager?.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK,
                            "ClashMeta:network-forwarding"
                        )?.apply { setReferenceCounted(false) }
                    }
                    if (wakeLock?.isHeld == false) {
                        try {
                            wakeLock?.acquire()
                        } catch (e: RuntimeException) {
                            Log.w("Unable to acquire network wake lock: ${e.message}", e)
                        }
                    }
                } else if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }

                // Enhanced mode also keeps core maintenance active with the screen off.
                // Disabling it restores the original screen-dependent suspend policy.
                val suspended = !keepAlive && powerManager?.isInteractive == false
                Clash.suspendCore(suspended)
                Log.d("Background policy: keepAlive=$keepAlive, suspended=$suspended")

                select<Unit> {
                    screenToggle.onReceive { }
                    preferencesChanged.onReceive { }
                }
            }
        } finally {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
            preferencesChanged.close()
            if (wakeLock?.isHeld == true)
                wakeLock?.release()
            withContext(NonCancellable) {
                Clash.suspendCore(false)
            }
        }
    }
}
