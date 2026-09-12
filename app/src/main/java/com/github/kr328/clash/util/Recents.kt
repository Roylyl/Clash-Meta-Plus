package com.github.kr328.clash.util

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.core.content.getSystemService
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.design.store.UiStore

/** Updates existing tasks without closing screens or interrupting the VPN. */
fun Context.applyRecentTaskVisibility(hidden: Boolean = UiStore(this).hideFromRecents) {
    val manager = getSystemService<ActivityManager>() ?: return
    val tasks = try {
        manager.appTasks
    } catch (e: SecurityException) {
        Log.w("Unable to read recent app tasks", e)
        return
    }

    tasks.forEach { task ->
        try {
            val component = task.taskInfo.baseIntent.component
            // Transient control activities must remain excluded even when the switch is off.
            @Suppress("DEPRECATION")
            val alwaysExcluded = component?.let {
                packageManager.getActivityInfo(it, PackageManager.GET_DISABLED_COMPONENTS).flags and
                    ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS != 0
            } ?: false
            task.setExcludeFromRecents(hidden || alwaysExcluded)
        } catch (e: IllegalArgumentException) {
            // A task can disappear between enumeration and updating its root intent.
            Log.d("Recent app task no longer exists", e)
        } catch (e: SecurityException) {
            Log.w("Unable to update recent app task", e)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("Recent app task activity no longer exists", e)
        }
    }
}
