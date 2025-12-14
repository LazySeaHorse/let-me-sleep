package com.lazyseahorse.letmesleep.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lazyseahorse.letmesleep.R

object NotificationHelper {
    const val CHANNEL_ID = "timer_channel"
    private const val CHANNEL_NAME = "Timer"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows active timer and alarm"
                setSound(null, null) // Application controls sound via Service/MediaPlayer
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun buildTimerRunningNotification(context: Context, endTimeMillis: Long): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Fallback if no specific icon
            .setContentTitle("Timer Running")
            .setContentText("Tap to open")
            .setUsesChronometer(true)
            .setWhen(endTimeMillis)
            .setShowWhen(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority for running timer to avoid constant buzzing
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setChronometerCountDown(true)
        }
        
        return builder
    }


    fun buildAlarmFiringNotification(context: Context): NotificationCompat.Builder {
        val fullScreenIntent = android.content.Intent(context, com.lazyseahorse.letmesleep.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.EXTRA_SHOW_ALARM_UI, true)
        }
        
        val fullScreenPendingIntent = android.app.PendingIntent.getActivity(
            context,
            123,
            fullScreenIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time's Up!")
            .setContentText("Tap to dismiss or wait for auto-snooze")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(false)
            .setOngoing(true)
    }
}
