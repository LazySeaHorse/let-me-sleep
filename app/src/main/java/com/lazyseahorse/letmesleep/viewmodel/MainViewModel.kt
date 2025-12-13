package com.lazyseahorse.letmesleep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    companion object {
        const val TAG = "MainViewModel"
    }

    private val _countdownFlow = MutableStateFlow(0)
    val countdownFlow = _countdownFlow.asStateFlow()

    private val _isCountdownComplete = MutableStateFlow(false)
    val isCountdownComplete = _isCountdownComplete.asStateFlow()

    private var isCountingDown = false

    fun startCountdown(
        timeInSeconds: Int,
        ringDurationSeconds: Int,
        snoozeDurationSeconds: Int,
        autoSnoozeLimit: Int
    ) {
        if (isCountingDown) return

        viewModelScope.launch {
            isCountingDown = true
            var currentSnoozeCount = 0

            // Initial Countdown
            runCountdownHelper(timeInSeconds)
            _isCountdownComplete.value = true

            while (currentSnoozeCount < autoSnoozeLimit) {
                // Ringing Phase (Wait for ringDuration)
                delay(ringDurationSeconds * 1000L)

                // Start Snooze
                _isCountdownComplete.value = false
                runCountdownHelper(snoozeDurationSeconds)
                
                // End Snooze (Ring again)
                _isCountdownComplete.value = true
                currentSnoozeCount++
            }
            
            // Final Timer Deactivation
            delay(ringDurationSeconds * 1000L) // Wait one last time? Or just stop? Prompt says "deactivate itself".
            // If we just stop here, the "True" state remains (Ringing indefinitely?) or we reset?
            // "Timer will deactivate itself". I assume this means it stops ringing.
            
            isCountingDown = false
            _isCountdownComplete.value = false // Stop ringing
        }
    }

    private suspend fun runCountdownHelper(seconds: Int) {
        _isCountdownComplete.value = false // Ensure we are not ringing while counting
        _countdownFlow.value = seconds
        while (_countdownFlow.value > 0) {
            delay(1000L)
            _countdownFlow.value -= 1
        }
    }
}