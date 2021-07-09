package com.annhienktuit.piggykeeper.activity

import com.annhienktuit.piggykeeper.R
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationIntent =
            Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(NotificationActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val pendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = Notification.Builder(context)
        val notification: Notification = builder.setContentTitle("Piggy Keeper Notification")
            .setContentText("Didn't you add today transaction?")
            .setTicker("Add now!")
            .setSmallIcon(R.drawable.ic_piggy_bank)
            .setContentIntent(pendingIntent).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID)
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NotificationDemo",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notification)
    }

    companion object {
        private const val CHANNEL_ID = "this.is.my.piggykeeper"
    }
}