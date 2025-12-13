package com.lazyseahorse.letmesleep.utils

object Constants {
    const val ACTION_ALARM_TRIGGERED = "com.lazyseahorse.letmesleep.ACTION_ALARM_TRIGGERED"
    const val ACTION_START_ALARM = "com.lazyseahorse.letmesleep.ACTION_START_ALARM"
    const val ACTION_STOP_ALARM = "com.lazyseahorse.letmesleep.ACTION_STOP_ALARM" // User manually stops
    const val ACTION_SNOOZE_ALARM = "com.lazyseahorse.letmesleep.ACTION_SNOOZE_ALARM" // User manually snoozes

    
    // Extras
    const val EXTRA_SNOOZE_COUNT = "EXTRA_SNOOZE_COUNT"
    const val EXTRA_RING_DURATION = "EXTRA_RING_DURATION"
    const val EXTRA_SNOOZE_DURATION = "EXTRA_SNOOZE_DURATION"
    const val EXTRA_AUTO_SNOOZE_LIMIT = "EXTRA_AUTO_SNOOZE_LIMIT"
    const val EXTRA_SHOW_ALARM_UI = "EXTRA_SHOW_ALARM_UI"
}
