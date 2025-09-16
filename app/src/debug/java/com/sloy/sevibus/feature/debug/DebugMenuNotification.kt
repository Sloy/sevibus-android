package com.sloy.sevibus.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sloy.debugmenu.launcher.DebugMenuActivity
import com.sloy.sevibus.R

object DebugMenuNotification {
    private const val CHANNEL_ID = "debug_menu_channel"
    private const val CHANNEL_NAME = "Debug Menu"
    private const val NOTIFICATION_ID = 1001

    fun show(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, DebugMenuActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Debug Menu")
            .setContentText("Tap to open Debug Menu")
            .setSmallIcon(com.sloy.debugmenu.R.drawable.outline_developer_mode_24)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
