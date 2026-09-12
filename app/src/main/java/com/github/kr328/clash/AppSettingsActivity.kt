package com.github.kr328.clash

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.getSystemService
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.design.AppSettingsDesign
import com.github.kr328.clash.design.model.Behavior
import com.github.kr328.clash.design.store.UiStore.Companion.mainActivityAlias
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.util.ApplicationObserver
import com.github.kr328.clash.util.applyRecentTaskVisibility
import com.github.kr328.clash.design.R
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

class AppSettingsActivity : BaseActivity<AppSettingsDesign>(), Behavior {
    override suspend fun main() {
        val design = AppSettingsDesign(
            this,
            uiStore,
            ServiceStore(this),
            this,
            clashRunning,
            ::onHideIconChange,
        )

        setContentDesign(design)
        refreshBatteryStatus(design)

        while (isActive) {
            select<Unit> {
                events.onReceive {
                    when (it) {
                        Event.ClashStart, Event.ClashStop, Event.ServiceRecreated ->
                            recreate()
                        Event.ActivityStart -> refreshBatteryStatus(design)
                        else -> Unit
                    }
                }
                design.requests.onReceive {
                    when (it) {
                        AppSettingsDesign.Request.ReCreateAllActivities ->
                            ApplicationObserver.createdActivities.toList().forEach { activity ->
                                activity.recreate()
                            }
                        AppSettingsDesign.Request.UpdateRecentTaskVisibility ->
                            applyRecentTaskVisibility(uiStore.hideFromRecents)
                        AppSettingsDesign.Request.OpenBatteryOptimizationSettings ->
                            openSystemSettings(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        AppSettingsDesign.Request.OpenVpnSettings ->
                            openSystemSettings(Settings.ACTION_VPN_SETTINGS)
                    }
                }
            }
        }
    }

    private fun refreshBatteryStatus(design: AppSettingsDesign) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            design.updateBatteryOptimizationStatus(
                getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(packageName) == true
            )
        }
    }

    private fun openSystemSettings(action: String) {
        val intents = listOf(
            Intent(action),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")),
            Intent(Settings.ACTION_SETTINGS),
        )
        for (intent in intents) {
            try {
                startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
                // Some vendor ROMs omit individual system settings screens.
            } catch (_: SecurityException) {
                // Fall back to the next supported settings screen.
            }
        }
        Toast.makeText(this, R.string.system_settings_unavailable, Toast.LENGTH_LONG).show()
    }

    override var autoRestart: Boolean
        get() {
            val status = packageManager.getComponentEnabledSetting(
                RestartReceiver::class.componentName
            )

            return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        set(value) {
            val status = if (value)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED

            packageManager.setComponentEnabledSetting(
                RestartReceiver::class.componentName,
                status,
                PackageManager.DONT_KILL_APP,
            )
        }

    private fun onHideIconChange(hide: Boolean) {
        val newState = if (hide) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        packageManager.setComponentEnabledSetting(
            mainActivityAlias,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}
