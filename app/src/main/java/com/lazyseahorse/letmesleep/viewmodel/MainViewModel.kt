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

    private val _totalTimeFlow = MutableStateFlow(0)
    val totalTimeFlow = _totalTimeFlow.asStateFlow()

    private val _isCountdownComplete = MutableStateFlow(false)
    val isCountdownComplete = _isCountdownComplete.asStateFlow()

    private var countdownJob: Job? = null
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val timerPreferences = com.lazyseahorse.letmesleep.utils.TimerPreferences(application)

    init {
        NotificationHelper.createNotificationChannel(application)
        restoreTimerState()
    }

    fun startCountdown(
        timeInSeconds: Int,
        ringDurationSeconds: Int,
        snoozeDurationSeconds: Int,
        autoSnoozeLimit: Int
    ) {
        // Cancel any existing job/alarm
        stopTimer()
        
        com.lazyseahorse.letmesleep.utils.AppLogger.log("MainViewModel", "Timer Started: ${timeInSeconds}s")

        _totalTimeFlow.value = timeInSeconds

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

        // 2. Save State
        timerPreferences.saveTimerState(
            triggerTime = triggerTime,
            originalDuration = timeInSeconds,
            ringDuration = ringDurationSeconds,
            snoozeDuration = snoozeDurationSeconds,
            autoSnoozeLimit = autoSnoozeLimit
        )

        // 3. Show Timer Notification
        val notification = NotificationHelper.buildTimerRunningNotification(context, triggerTime).build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)

        // 4. Start UI Update Loop
        _isCountdownComplete.value = false
        startCountdownLoop(triggerTime)
    }

    private fun startCountdownLoop(triggerTime: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val remainingMillis = triggerTime - System.currentTimeMillis()
                if (remainingMillis <= 0) {
                    _countdownFlow.value = 0
                    _isCountdownComplete.value = true
                    // We don't clear prefs here immediately to allow UI to show "Finished" state if needed,
                    // or we can clear it. Usually better to keep it until user dismisses/stops.
                    // But for now, let's keep it simple.
                    break
                }
                _countdownFlow.value = (remainingMillis / 1000).toInt()
                delay(100)
            }
        }
    }

    private fun restoreTimerState() {
        val state = timerPreferences.getTimerState() ?: return
        
        val triggerTime = state.triggerTime
        if (System.currentTimeMillis() < triggerTime) {
            // Timer is still running
            com.lazyseahorse.letmesleep.utils.AppLogger.log("MainViewModel", "Restoring timer: trigger=$triggerTime")
            _totalTimeFlow.value = state.originalDuration
            _isCountdownComplete.value = false
            startCountdownLoop(triggerTime)
        } else {
            // Timer expired while we were gone
            com.lazyseahorse.letmesleep.utils.AppLogger.log("MainViewModel", "Timer expired while inactive")
            _totalTimeFlow.value = state.originalDuration
            _countdownFlow.value = 0
            _isCountdownComplete.value = true
            // We could clear prefs here, but maybe we want to show the "Times Up" state.
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
        _totalTimeFlow.value = 0
        _isCountdownComplete.value = false
        
        timerPreferences.clearTimerState()
    }
}