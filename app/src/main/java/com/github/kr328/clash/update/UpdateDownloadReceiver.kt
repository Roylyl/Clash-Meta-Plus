package com.github.kr328.clash.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.common.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Background completion only notifies; the foreground update screen opens the installer. */
class UpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (completedId < 0) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val state = UpdateDownload.query(context)
                if (state?.id == completedId && state.status == DownloadManager.STATUS_SUCCESSFUL) {
                    UpdateNotifications.downloadReady(context)
                }
            } catch (e: Exception) {
                Log.w("Unable to handle application update download", e)
            } finally {
                pending.finish()
            }
        }
    }
}
