package com.github.kr328.clash.design

import android.content.Context
import android.os.Build
import android.view.View
import com.github.kr328.clash.design.databinding.DesignSettingsCommonBinding
import com.github.kr328.clash.design.model.Behavior
import com.github.kr328.clash.design.model.DarkMode
import com.github.kr328.clash.design.preference.*
import com.github.kr328.clash.design.store.UiStore
import com.github.kr328.clash.design.util.applyFrom
import com.github.kr328.clash.design.util.bindAppBarElevation
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.root
import com.github.kr328.clash.service.store.ServiceStore

class AppSettingsDesign(
    context: Context,
    uiStore: UiStore,
    srvStore: ServiceStore,
    behavior: Behavior,
    running: Boolean,
    onHideIconChange: (hide: Boolean) -> Unit,
) : Design<AppSettingsDesign.Request>(context) {
    enum class Request {
        ReCreateAllActivities,
        UpdateRecentTaskVisibility,
        OpenBatteryOptimizationSettings,
        OpenVpnSettings
    }

    private val binding = DesignSettingsCommonBinding
        .inflate(context.layoutInflater, context.root, false)

    override val root: View
        get() = binding.root

    private var batteryPreference: ClickablePreference? = null

    fun updateBatteryOptimizationStatus(exempt: Boolean) {
        batteryPreference?.summary = context.getString(
            if (exempt) R.string.battery_optimization_exempt
            else R.string.battery_optimization_summary
        )
    }

    init {
        binding.surface = surface

        binding.activityBarLayout.applyFrom(context)

        binding.scrollRoot.bindAppBarElevation(binding.activityBarLayout)

        val screen = preferenceScreen(context) {
            category(R.string.behavior)

            switch(
                value = behavior::autoRestart,
                icon = R.drawable.ic_baseline_restore,
                title = R.string.auto_restart,
                summary = R.string.allow_clash_auto_restart,
            )

            switch(
                value = srvStore::aggressiveKeepAlive,
                icon = R.drawable.ic_baseline_restore,
                title = R.string.aggressive_keep_alive_title,
                summary = R.string.aggressive_keep_alive_summary,
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                batteryPreference = clickable(
                    title = R.string.battery_optimization_title,
                    icon = R.drawable.ic_baseline_restore,
                    summary = R.string.battery_optimization_summary,
                ) {
                    clicked { requests.trySend(Request.OpenBatteryOptimizationSettings) }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                clickable(
                    title = R.string.always_on_vpn_title,
                    icon = R.drawable.ic_baseline_settings,
                    summary = R.string.always_on_vpn_summary,
                ) {
                    clicked { requests.trySend(Request.OpenVpnSettings) }
                }
            }

            category(R.string.interface_)

            selectableList(
                value = uiStore::darkMode,
                values = DarkMode.values(),
                valuesText = arrayOf(
                    R.string.follow_system_android_10,
                    R.string.always_light,
                    R.string.always_dark
                ),
                icon = R.drawable.ic_baseline_brightness_4,
                title = R.string.dark_mode
            ) {
                listener = OnChangedListener {
                    requests.trySend(Request.ReCreateAllActivities)
                }
            }

            switch(
                value = uiStore::hideAppIcon,
                icon = R.drawable.ic_baseline_hide,
                title = R.string.hide_app_icon_title,
                summary = R.string.hide_app_icon_desc,
            ) {
                listener = OnChangedListener {
                    onHideIconChange(uiStore::hideAppIcon.get())
                }
            }

            switch(
                value = uiStore::hideFromRecents,
                icon = R.drawable.ic_baseline_stack,
                title = R.string.hide_from_recents_title,
                summary = R.string.hide_from_recents_desc,
            ) {
                listener = OnChangedListener {
                    requests.trySend(Request.UpdateRecentTaskVisibility)
                }
            }

            category(R.string.service)

            switch(
                value = srvStore::dynamicNotification,
                icon = R.drawable.ic_baseline_domain,
                title = R.string.show_traffic,
                summary = R.string.show_traffic_summary
            ) {
                enabled = !running
            }
        }

        binding.content.addView(screen.root)
    }
}