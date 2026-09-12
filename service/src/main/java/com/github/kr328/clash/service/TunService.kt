package com.github.kr328.clash.service

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.net.ProxyInfo
import android.net.VpnService
import android.os.Build
import com.github.kr328.clash.common.compat.pendingIntentFlags
import com.github.kr328.clash.common.constants.Components
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.clash.RuntimeSession
import com.github.kr328.clash.service.clash.clashRuntime
import com.github.kr328.clash.service.clash.module.*
import com.github.kr328.clash.service.model.AccessControlMode
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.cancelAndJoinBlocking
import com.github.kr328.clash.service.util.parseCIDR
import com.github.kr328.clash.service.util.sendClashStarted
import com.github.kr328.clash.service.util.sendClashStopped
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

class TunService : VpnService(), CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val self: TunService
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
        val tun = install(TunModule(self))
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
            tun.open()

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
                    network.onEvent { n ->
                        if (Build.VERSION.SDK_INT in 22..28) @TargetApi(22) {
                            setUnderlyingNetworks(n?.let { arrayOf(it) })
                        }

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
                tun.close()

                session.beginStop()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (destroying || (!session.isRunning() && StatusProvider.serviceRunning)) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        // Always-on VPN starts originate from Android, without the app's start helper.
        if (intent?.action == SERVICE_INTERFACE)
            StatusProvider.shouldStartClashOnBoot = true

        if (prepare(this) != null) {
            requestStop(clearRecovery = true)
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
        Log.i("TunService runtime stopped: ${reason ?: "successfully"}")

        val pendingStartId = completed.pendingStartId
        if (allowRestart && pendingStartId != null && StatusProvider.shouldStartClashOnBoot) {
            onStartCommand(null, 0, pendingStartId)
        } else if (allowRestart) {
            // Do not cancel a newer start command that Android has not delivered yet.
            stopSelf(pendingStartId ?: completed.stoppedStartId)
        }
    }

    override fun onRevoke() {
        // The system can retain its VPN binding after revocation; cancel the runtime
        // independently of onDestroy, including when this callback is on a binder thread.
        if (session.isRunning()) {
            requestStop(clearRecovery = true)
            TunModule.requestStop()
        } else if (!StatusProvider.serviceRunning) {
            StatusProvider.shouldStartClashOnBoot = false
        }
        super.onRevoke()
    }

    override fun onDestroy() {
        destroying = true
        if (session.isRunning())
            TunModule.requestStop()
        runtime.stop()
        cancelAndJoinBlocking()
        finishRuntime(allowRestart = false)
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        runtime.requestGc()
    }

    private fun TunModule.open() {
        val store = ServiceStore(self)

        val device = with(Builder()) {
            // Interface address
            addAddress(TUN_GATEWAY, TUN_SUBNET_PREFIX)
            if (store.allowIpv6) {
                addAddress(TUN_GATEWAY6, TUN_SUBNET_PREFIX6)
            }

            // Route
            if (store.bypassPrivateNetwork) {
                resources.getStringArray(R.array.bypass_private_route).map(::parseCIDR).forEach {
                    addRoute(it.ip, it.prefix)
                }
                if (store.allowIpv6) {
                    resources.getStringArray(R.array.bypass_private_route6).map(::parseCIDR).forEach {
                        addRoute(it.ip, it.prefix)
                    }
                }

                // Route of virtual DNS
                addRoute(TUN_DNS, 32)
                if (store.allowIpv6) {
                    addRoute(TUN_DNS6, 128)
                }
            } else {
                addRoute(NET_ANY, 0)
                if (store.allowIpv6) {
                    addRoute(NET_ANY6, 0)
                }
            }

            // Access Control
            when (store.accessControlMode) {
                AccessControlMode.AcceptAll -> Unit
                AccessControlMode.AcceptSelected -> {
                    (store.accessControlPackages + packageName).forEach {
                        runCatching { addAllowedApplication(it) }
                    }
                }
                AccessControlMode.DenySelected -> {
                    (store.accessControlPackages - packageName).forEach {
                        runCatching { addDisallowedApplication(it) }
                    }
                }
            }

            // Blocking
            setBlocking(false)

            // Mtu
            setMtu(TUN_MTU)

            // Session Name
            setSession("Clash")

            // Virtual Dns Server
            addDnsServer(TUN_DNS)
            if (store.allowIpv6) {
                addDnsServer(TUN_DNS6)
            }

            // Open MainActivity
            setConfigureIntent(
                PendingIntent.getActivity(
                    self,
                    R.id.nf_vpn_status,
                    Intent().setComponent(Components.MAIN_ACTIVITY),
                    pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
                )
            )

            // Metered
            if (Build.VERSION.SDK_INT >= 29) {
                setMetered(false)
            }

            // System Proxy
            if (Build.VERSION.SDK_INT >= 29 && store.systemProxy) {
                listenHttp()?.let {
                    setHttpProxy(
                        ProxyInfo.buildDirectProxy(
                            it.address.hostAddress,
                            it.port,
                            HTTP_PROXY_BLACK_LIST + if (store.bypassPrivateNetwork) HTTP_PROXY_LOCAL_LIST else emptyList()
                        )
                    )
                }
            }

            if (store.allowBypass) {
                allowBypass()
            }

            TunModule.TunDevice(
                fd = establish()?.detachFd()
                    ?: throw NullPointerException("Establish VPN rejected by system"),
                stack = store.tunStackMode,
                gateway = "$TUN_GATEWAY/$TUN_SUBNET_PREFIX" + if (store.allowIpv6) ",$TUN_GATEWAY6/$TUN_SUBNET_PREFIX6" else "",
                portal = TUN_PORTAL + if (store.allowIpv6) ",$TUN_PORTAL6" else "",
                dns = if (store.dnsHijacking) NET_ANY else (TUN_DNS + if (store.allowIpv6) ",$TUN_DNS6" else ""),
            )
        }

        attach(device)
    }

    companion object {
        private const val TUN_MTU = 9000
        private const val TUN_SUBNET_PREFIX = 30
        private const val TUN_GATEWAY = "172.19.0.1"
        private const val TUN_SUBNET_PREFIX6 = 126
        private const val TUN_GATEWAY6 = "fdfe:dcba:9876::1"
        private const val TUN_PORTAL = "172.19.0.2"
        private const val TUN_PORTAL6 = "fdfe:dcba:9876::2"
        private const val TUN_DNS = TUN_PORTAL
        private const val TUN_DNS6 = TUN_PORTAL6
        private const val NET_ANY = "0.0.0.0"
        private const val NET_ANY6 = "::"

        private val HTTP_PROXY_LOCAL_LIST: List<String> = listOf(
            "localhost",
            "*.local",
            "127.*",
            "10.*",
            "172.16.*",
            "172.17.*",
            "172.18.*",
            "172.19.*",
            "172.2*",
            "172.30.*",
            "172.31.*",
            "192.168.*"
        )
        private val HTTP_PROXY_BLACK_LIST: List<String> = listOf(
            "*zhihu.com",
            "*zhimg.com",
            "*jd.com",
            "100ime-iat-api.xfyun.cn",
            "*360buyimg.com",
        )
    }
}
