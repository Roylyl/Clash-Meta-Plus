package com.github.kr328.clash.service

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Binder
import android.os.IBinder
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.clash.RuntimeSession
import com.github.kr328.clash.service.clash.clashRuntime
import com.github.kr328.clash.service.clash.module.*
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.cancelAndJoinBlocking
import com.github.kr328.clash.service.util.sendClashStarted
import com.github.kr328.clash.service.util.sendClashStopped
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class ClashService : BaseService() {
    private val self: ClashService
        get() = this

    @Volatile
    private var reason: String? = null
    @Volatile
    private var destroying = false
    private val session = RuntimeSession()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val runtime = clashRuntime {
        val store = ServiceStore(self)

        val close = install(CloseModule(self))
        val config = install(ConfigurationModule(self))
        val network = install(NetworkObserveModule(self))

        if (store.dynamicNotification)
            install(DynamicNotificationModule(self))
        else
            install(StaticNotificationModule(self))

        install(AppListCacheModule(self))
        install(TimeZoneModule(self))
        install(SuspendModule(self))

        try {
            while (isActive) {
                val quit = select<Boolean> {
                    close.onEvent {
                        session.closeIfNotRequested { StatusProvider.shouldStartClashOnBoot }
                    }
                    config.onEvent {
                        reason = it.message
                        disableRecovery()
                        true
                    }
                    network.onEvent {
                        false
                    }
                }

                if (quit) break
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Create clash runtime: ${e.message}", e)

            reason = e.message
            disableRecovery()
        } finally {
            withContext(NonCancellable) {
                session.beginStop()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (destroying || (!session.isRunning() && StatusProvider.serviceRunning)) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        // A stale sticky restart must not revive an explicitly stopped service.
        if (!StatusProvider.shouldStartClashOnBoot) {
            requestStop(clearRecovery = false)
            stopSelf(startId)
            return START_NOT_STICKY
        }

        when (session.start(startId)) {
            RuntimeSession.Start.AlreadyRunning,
            RuntimeSession.Start.WaitForCleanup -> return START_STICKY
            RuntimeSession.Start.Launch -> Unit
        }

        reason = null
        StatusProvider.serviceRunning = true
        try {
            StaticNotificationModule.createNotificationChannel(this)
            StaticNotificationModule.notifyLoadingNotification(this)
            runtime.launch(
                onFailure = { error ->
                    reason = error.message
                    Log.e("Clash runtime failed: ${error.message}", error)
                    disableRecovery()
                },
                onStopped = {
                    // This runs after native/module/wake-lock cleanup even while Android
                    // keeps the Service bound. Serialize state changes with new starts.
                    mainHandler.post {
                        if (!destroying)
                            finishRuntime(allowRestart = true)
                    }
                }
            )
            sendClashStarted()
        } catch (e: RuntimeException) {
            reason = e.message
            Log.e("Unable to start foreground service: ${e.message}", e)
            disableRecovery()
            finishRuntime(allowRestart = false)
            stopSelf(startId)
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun disableRecovery() {
        // Atomically preserve a newer Start already received during old-runtime cleanup.
        session.disableRecovery {
            StatusProvider.shouldStartClashOnBoot = false
        }
    }

    private fun requestStop(clearRecovery: Boolean) {
        session.beginStop()
        if (clearRecovery)
            StatusProvider.shouldStartClashOnBoot = false
        runtime.stop()
    }

    @Suppress("DEPRECATION")
    private fun finishRuntime(allowRestart: Boolean) {
        val completed = session.finish() ?: return
        StatusProvider.serviceRunning = false
        stopForeground(true)
        sendClashStopped(reason)
        Log.i("ClashService runtime stopped: ${reason ?: "successfully"}")

        val pendingStartId = completed.pendingStartId
        if (allowRestart && pendingStartId != null && StatusProvider.shouldStartClashOnBoot) {
            onStartCommand(null, 0, pendingStartId)
        } else if (allowRestart) {
            // Do not cancel a newer start command that Android has not delivered yet.
            stopSelf(pendingStartId ?: completed.stoppedStartId)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onDestroy() {
        destroying = true
        runtime.stop()
        cancelAndJoinBlocking()
        finishRuntime(allowRestart = false)
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        runtime.requestGc()
    }
}