package com.lazyseahorse.letmesleep.utils

import android.content.Context
import android.content.SharedPreferences

class TimerPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "let_me_sleep_prefs"
        private const val KEY_TRIGGER_TIME = "trigger_time"
        private const val KEY_ORIGINAL_DURATION = "original_duration"
        private const val KEY_RING_DURATION = "ring_duration"
        private const val KEY_SNOOZE_DURATION = "snooze_duration"
        private const val KEY_AUTO_SNOOZE_LIMIT = "auto_snooze_limit"
        private const val KEY_TIMER_RUNNING = "timer_running"
    }

    fun saveTimerState(
        triggerTime: Long,
        originalDuration: Int,
        ringDuration: Int,
        snoozeDuration: Int,
        autoSnoozeLimit: Int
    ) {
        prefs.edit().apply {
            putLong(KEY_TRIGGER_TIME, triggerTime)
            putInt(KEY_ORIGINAL_DURATION, originalDuration)
            putInt(KEY_RING_DURATION, ringDuration)
            putInt(KEY_SNOOZE_DURATION, snoozeDuration)
            putInt(KEY_AUTO_SNOOZE_LIMIT, autoSnoozeLimit)
            putBoolean(KEY_TIMER_RUNNING, true)
            apply()
        }
    }

    fun clearTimerState() {
        prefs.edit().apply {
            putBoolean(KEY_TIMER_RUNNING, false)
            remove(KEY_TRIGGER_TIME)
            // We can keep others as defaults or remove them, but removing trigger time implies no active timer.
            // Setting boolean to false is safer.
            apply()
        }
    }

    fun isTimerRunning(): Boolean {
        return prefs.getBoolean(KEY_TIMER_RUNNING, false)
    }

    fun getTimerState(): TimerState? {
        if (!isTimerRunning()) return null
        
        return TimerState(
            triggerTime = prefs.getLong(KEY_TRIGGER_TIME, 0L),
            originalDuration = prefs.getInt(KEY_ORIGINAL_DURATION, 0),
            ringDuration = prefs.getInt(KEY_RING_DURATION, 0),
            snoozeDuration = prefs.getInt(KEY_SNOOZE_DURATION, 0),
            autoSnoozeLimit = prefs.getInt(KEY_AUTO_SNOOZE_LIMIT, 0)
        )
    }

    data class TimerState(
        val triggerTime: Long,
        val originalDuration: Int,
        val ringDuration: Int,
        val snoozeDuration: Int,
        val autoSnoozeLimit: Int
    )
}
