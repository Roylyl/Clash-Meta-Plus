package com.github.kr328.clash.remote

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.net.Uri
import com.github.kr328.clash.common.constants.Authorities
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.service.RemoteService
import com.github.kr328.clash.service.StatusProvider
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.remote.IRemoteService
import com.github.kr328.clash.service.remote.unwrap
import com.github.kr328.clash.util.unbindServiceSilent
import com.github.kr328.clash.util.startClashService
import java.util.concurrent.TimeUnit

class Service(private val context: Application, val crashed: () -> Unit) {
    val remote = Resource<IRemoteService>()
    private var bound = false

    private val connection = object : ServiceConnection {
        private var lastCrashed: Long = -1

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            if (!bound)
                return
            remote.set(service.unwrap(IRemoteService::class))
            recoverRequestedRuntime()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            if (!bound)
                return
            remote.set(null)

            if (System.currentTimeMillis() - lastCrashed < TOGGLE_CRASHED_INTERVAL) {
                unbind()

                crashed()
            }

            lastCrashed = System.currentTimeMillis()
            Log.w("RemoteService killed or crashed")
        }
    }

    fun bind() {
        if (bound)
            return
        try {
            bound = context.bindService(
                RemoteService::class.intent, connection, Context.BIND_AUTO_CREATE
            )
        } catch (e: Exception) {
            unbind()

            crashed()
        }
    }

    fun unbind() {
        if (bound) {
            bound = false
            context.unbindServiceSilent(connection)
        }
        remote.set(null)
    }

    private fun recoverRequestedRuntime() {
        // UI visibility owns this binding. Make one attempt on connection/reconnection,
        // including after Android loses a sticky VPN during interface-removal cleanup.
        // This is not a timer or an invisible-background restart loop.
        try {
            if (!bound || !ServiceStore(context).aggressiveKeepAlive ||
                !StatusProvider.shouldStartClashOnBoot)
                return

            val status = context.contentResolver.call(
                Uri.parse("content://${Authorities.STATUS_PROVIDER}"),
                StatusProvider.METHOD_CURRENT_PROFILE,
                null,
                null
            )
            // A non-null bundle means a runtime exists, even while its profile is loading.
            if (status != null || !StatusProvider.shouldStartClashOnBoot)
                return

            if (context.startClashService() != null) {
                StatusProvider.shouldStartClashOnBoot = false
                Log.w("Service recovery needs renewed VPN permission")
            } else {
                Log.i("Requested service recovery after UI connection")
            }
        } catch (e: RuntimeException) {
            // The system may deny foreground-service starts; leave retries to a later
            // user-visible connection, never spin or bypass the platform restriction.
            Log.w("Service recovery unavailable: ${e.message}", e)
        }
    }

    companion object {
        private val TOGGLE_CRASHED_INTERVAL = TimeUnit.SECONDS.toMillis(10)
    }
}