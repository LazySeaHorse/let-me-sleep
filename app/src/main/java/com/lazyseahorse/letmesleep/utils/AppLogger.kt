package com.lazyseahorse.letmesleep.utils

import android.util.Log
import androidx.compose.runtime.mutableStateListOf

object AppLogger {
    // A mutable list that Compose can observe
    val logs = mutableStateListOf<LogEntry>()

    data class LogEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val tag: String,
        val message: String
    )

    fun log(tag: String, message: String) {
        // 1. Log to standard Android Logcat
        Log.d(tag, message)
        
        // 2. Add to our internal list (capped at 1000 to prevent memory issues)
        if (logs.size > 1000) {
            logs.removeAt(0)
        }
        logs.add(LogEntry(tag = tag, message = message))
    }
    
    fun clear() {
        logs.clear()
    }
}
