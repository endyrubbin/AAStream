package com.garage.aastream.handlers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import com.garage.aastream.R
import com.garage.aastream.activities.CarDebugActivity

/**
 * Created by Eddy Gorbunov on 07.06.2019 11:29.
 * For project: AAStream
 */
class NotificationHandler(val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    /**
     * Show notification when car activity is started to safely exit the app and restore previous state
     */
    @Suppress("DEPRECATION")
    fun showNotification() {
        createChannel(context)
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
        }

        val title = context.getString(R.string.app_name)
        val message = context.getString(R.string.txt_app_running_notification)

        val notifyIntent = Intent(context, CarDebugActivity::class.java)
        notifyIntent.putExtra(ACTION_EXIT, true)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        notifyIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

        val pendingIntent = PendingIntent.getActivity(context, 0,
            notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = builder
            .setSmallIcon(R.drawable.ic_small_icon)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setPriority(Notification.PRIORITY_MAX)
            .setContentTitle(title)
            .setWhen(0)
            .setShowWhen(true)
            .setStyle(Notification.BigTextStyle().bigText(message))
            .setContentText(message)
            .addAction(Notification
                .Action(0, context.getString(R.string.txt_notification_exit), pendingIntent))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Clear ingoing notification when app is destroyed
     */
    fun clearNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Create notification channel for android O and up
     */
    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)

            notificationChannel.enableVibration(true)
            notificationChannel.setShowBadge(true)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.parseColor("#e8334a")
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1337
        const val CHANNEL_ID = "AAStream Channel ID"
        const val CHANNEL_NAME = "AAStream Notification Name"
        const val ACTION_EXIT = "Action Exit"
    }
}