package com.github.kr328.clash

import android.content.pm.PackageManager
import com.github.kr328.clash.common.compat.getDrawableCompat
import com.github.kr328.clash.common.constants.Metadata
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.design.OverrideSettingsDesign
import com.github.kr328.clash.design.model.AppInfo
import com.github.kr328.clash.design.util.toAppInfo
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.util.withClash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

class OverrideSettingsActivity : BaseActivity<OverrideSettingsDesign>() {
    companion object {
        const val EXTRA_LAN_ONLY = "lan_only"
    }

    override suspend fun main() {
        val lanOnly = intent.getBooleanExtra(EXTRA_LAN_ONLY, false)
        if (lanOnly) setTitle(com.github.kr328.clash.design.R.string.lan_sharing)

        val configuration = withClash { queryOverride(Clash.OverrideSlot.Persist) }
        val effective = withClash { queryConfiguration() }
        val addresses = withContext(Dispatchers.IO) {
            java.net.NetworkInterface.getNetworkInterfaces().toList()
                .filter { it.isUp && !it.isLoopback &&
                    !it.name.startsWith("tun") && !it.name.startsWith("rmnet") &&
                    !it.name.startsWith("ccmni") }
                .flatMap { network -> network.inetAddresses.toList()
                    .filterIsInstance<java.net.Inet4Address>()
                    .filter { it.isSiteLocalAddress }
                    .mapNotNull { it.hostAddress } }
                .distinct()
        }

        defer {
            withClash {
                patchOverride(Clash.OverrideSlot.Persist, configuration)
            }
        }

        val design = OverrideSettingsDesign(
            this,
            configuration,
            lanOnly,
            effective,
            addresses
        )

        setContentDesign(design)

        while (isActive) {
            select<Unit> {
                events.onReceive {

                }
                design.requests.onReceive {
                    when (it) {
                        OverrideSettingsDesign.Request.ResetOverride -> {
                            if (design.requestResetConfirm()) {
                                defer {
                                    withClash {
                                        clearOverride(Clash.OverrideSlot.Persist)
                                    }
                                }

                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}