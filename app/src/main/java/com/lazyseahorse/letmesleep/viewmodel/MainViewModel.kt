package com.lazyseahorse.letmesleep.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lazyseahorse.letmesleep.receiver.AlarmReceiver
import com.lazyseahorse.letmesleep.utils.Constants
import com.lazyseahorse.letmesleep.utils.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "MainViewModel"
    }

    private val _countdownFlow = MutableStateFlow(0)
    val countdownFlow = _countdownFlow.asStateFlow()

    private val _isCountdownComplete = MutableStateFlow(false)
    val isCountdownComplete = _isCountdownComplete.asStateFlow()

    private var countdownJob: Job? = null
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        NotificationHelper.createNotificationChannel(application)
    }

    fun startCountdown(
        timeInSeconds: Int,
        ringDurationSeconds: Int,
        snoozeDurationSeconds: Int,
        autoSnoozeLimit: Int
    ) {
        // Cancel any existing job/alarm
        stopTimer()

        val context = getApplication<Application>()
        val triggerTime = System.currentTimeMillis() + (timeInSeconds * 1000L)

        // 1. Schedule Alarm
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = Constants.ACTION_ALARM_TRIGGERED
            putExtra(Constants.EXTRA_RING_DURATION, ringDurationSeconds)
            putExtra(Constants.EXTRA_SNOOZE_DURATION, snoozeDurationSeconds)
            putExtra(Constants.EXTRA_AUTO_SNOOZE_LIMIT, autoSnoozeLimit)
            putExtra(Constants.EXTRA_SNOOZE_COUNT, 0)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use precise alarm
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                 alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                 alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
             alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }

        // 2. Show Timer Notification
        val notification = NotificationHelper.buildTimerRunningNotification(context, triggerTime).build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)

        // 3. Start UI Update Loop
        _isCountdownComplete.value = false
        countdownJob = viewModelScope.launch {
            while (true) {
                val remainingMillis = triggerTime - System.currentTimeMillis()
                if (remainingMillis <= 0) {
                    _countdownFlow.value = 0
                    _isCountdownComplete.value = true // UI can react if open
                    break
                }
                _countdownFlow.value = (remainingMillis / 1000).toInt()
                delay(100) // Update frequency
            }
        }
    }

    fun stopTimer() {
        countdownJob?.cancel()
        
        val context = getApplication<Application>()
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = Constants.ACTION_ALARM_TRIGGERED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(NotificationHelper.NOTIFICATION_ID)
        
        // Also stop any ringing service
        val stopIntent = Intent(context, com.lazyseahorse.letmesleep.service.TimerService::class.java).apply {
            action = Constants.ACTION_STOP_ALARM
        }
        context.startService(stopIntent)

        _countdownFlow.value = 0
        _isCountdownComplete.value = false
    }
}