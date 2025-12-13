package com.lazyseahorse.letmesleep.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import com.lazyseahorse.letmesleep.receiver.AlarmReceiver
import com.lazyseahorse.letmesleep.utils.Constants
import com.lazyseahorse.letmesleep.utils.NotificationHelper
import kotlinx.coroutines.*

class TimerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var ringingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_ALARM -> {
                val snoozeCount = intent.getIntExtra(Constants.EXTRA_SNOOZE_COUNT, 0)
                val ringDuration = intent.getIntExtra(Constants.EXTRA_RING_DURATION, 30)
                val snoozeDuration = intent.getIntExtra(Constants.EXTRA_SNOOZE_DURATION, 60)
                val autoSnoozeLimit = intent.getIntExtra(Constants.EXTRA_AUTO_SNOOZE_LIMIT, 3)

                startRinging(snoozeCount, ringDuration, snoozeDuration, autoSnoozeLimit)
            }
            Constants.ACTION_STOP_ALARM -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startRinging(
        currentSnoozeCount: Int,
        ringDurationSeconds: Int,
        snoozeDurationSeconds: Int,
        autoSnoozeLimit: Int
    ) {
        // 1. Show Foreground Notification
        val notification = NotificationHelper.buildAlarmFiringNotification(this).build()
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)

        // 2. Play Sound
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) 
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Schedule Auto-Stop (Auto Snooze)
        ringingJob?.cancel()
        ringingJob = serviceScope.launch {
            delay(ringDurationSeconds * 1000L)
            
            // Time's up for ringing!
            if (currentSnoozeCount < autoSnoozeLimit) {
                // Schedule next Snooze
                scheduleSnooze(currentSnoozeCount + 1, snoozeDurationSeconds, ringDurationSeconds, autoSnoozeLimit)
            }
            // Stop this service (silence)
            stopSelf()
        }
    }

    private fun scheduleSnooze(
        nextSnoozeCount: Int,
        snoozeDurationSeconds: Int,
        ringDurationSeconds: Int,
        autoSnoozeLimit: Int
    ) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (snoozeDurationSeconds * 1000L)

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = Constants.ACTION_ALARM_TRIGGERED
            putExtra(Constants.EXTRA_SNOOZE_COUNT, nextSnoozeCount)
            putExtra(Constants.EXTRA_RING_DURATION, ringDurationSeconds)
            putExtra(Constants.EXTRA_SNOOZE_DURATION, snoozeDurationSeconds)
            putExtra(Constants.EXTRA_AUTO_SNOOZE_LIMIT, autoSnoozeLimit)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use precise alarm for snooze
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                 alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                 alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
             alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        ringingJob?.cancel()
    }
}
