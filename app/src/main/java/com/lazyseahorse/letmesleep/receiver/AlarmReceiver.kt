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
            val startIntent = Intent(context, TimerService::class.java).apply {
                this.action = Constants.ACTION_START_ALARM
                // Pass along any extras if needed, e.g. snooze counts
                putExtras(intent)
            }
            context.startForegroundService(startIntent)
        }
    }
}
