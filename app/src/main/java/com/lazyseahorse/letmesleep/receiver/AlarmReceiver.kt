package com.lazyseahorse.letmesleep.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lazyseahorse.letmesleep.service.TimerService
import com.lazyseahorse.letmesleep.utils.Constants

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Constants.ACTION_ALARM_TRIGGERED) {
            // Acquire a brief WakeLock to ensure CPU continues running for the Service start
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "LetMeSleep:AlarmReceiverLock")
            wakeLock.acquire(3000L) // 3 seconds timeout

            val startIntent = Intent(context, TimerService::class.java).apply {
                this.action = Constants.ACTION_START_ALARM
                putExtras(intent)
            }
            // Use ContextCompat to start foreground service safely checks
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
        }
    }
}
