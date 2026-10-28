package com.github.kr328.clash.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.kr328.clash.R

object UpdateNotifications {
    private const val CHANNEL = "app_releases"
    private const val AVAILABLE = 1702001
    private const val DOWNLOADED = 1702002

    fun available(context: Context, version: String): Boolean = show(
        context, AVAILABLE, context.getString(R.string.update_available, version),
        context.getString(R.string.update_notification_body),
    )

    fun downloadReady(context: Context) {
        show(context, DOWNLOADED, context.getString(R.string.update_download_complete),
            context.getString(R.string.update_install_notification))
    }

    fun dismiss(context: Context) {
        NotificationManagerCompat.from(context).cancel(AVAILABLE)
        NotificationManagerCompat.from(context).cancel(DOWNLOADED)
    }

    private fun show(context: Context, id: Int, title: String, message: String): Boolean {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(NotificationChannelCompat.Builder(
            CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT,
        ).setName(context.getString(R.string.update_title)).build())
        if (!manager.areNotificationsEnabled()) return false
        val intent = PendingIntent.getActivity(context, id,
            Intent(context, UpdateActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0,
        )
        return try {
            manager.notify(id, NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(com.github.kr328.clash.service.R.drawable.ic_logo_service)
                .setContentTitle(title).setContentText(message)
                .setContentIntent(intent).setAutoCancel(true).build())
            true
        } catch (_: SecurityException) {
            false
        }
    }
}

