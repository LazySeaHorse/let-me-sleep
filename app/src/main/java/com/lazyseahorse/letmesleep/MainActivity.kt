package com.lazyseahorse.letmesleep

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lazyseahorse.letmesleep.ui.TimerScreen
import com.lazyseahorse.letmesleep.ui.theme.LetMeSleepTheme
import com.lazyseahorse.letmesleep.utils.VibrationHelper
import com.lazyseahorse.letmesleep.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val vibrationHelper = VibrationHelper(this)

        setContent {
            LetMeSleepTheme {
                var userValue by remember { mutableStateOf("") }
                var ringDuration by remember { mutableStateOf("10") }
                var snoozeDuration by remember { mutableStateOf("5") }
                var snoozeLimit by remember { mutableStateOf("3") }

                val viewModel = viewModel<MainViewModel>()
                val time = viewModel.countdownFlow.collectAsState(initial = 0)
                val isTimerComplete = viewModel.isCountdownComplete.collectAsState()
                val snackBarHostState = remember { SnackbarHostState() }

                LaunchedEffect(isTimerComplete.value) {
                    if (isTimerComplete.value) {
                        vibrationHelper.vibrateOnComplete()

                        snackBarHostState.showSnackbar(
                            message = "Timer Completed",
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                //Animation State
                val rotation = rememberInfiniteTransition()
                val angle by rotation.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                TimerScreen(
                    time = time.value,
                    angle = angle,
                    userValue = userValue,
                    onUserValueChange = { userValue = it },
                    ringDuration = ringDuration,
                    onRingDurationChange = { ringDuration = it },
                    snoozeDuration = snoozeDuration,
                    onSnoozeDurationChange = { snoozeDuration = it },
                    snoozeLimit = snoozeLimit,
                    onSnoozeLimitChange = { snoozeLimit = it },
                    onStartClick = {
                        val duration = userValue.toIntOrNull()
                        val ring = ringDuration.toIntOrNull() ?: 10
                        val snooze = snoozeDuration.toIntOrNull() ?: 5
                        val limit = snoozeLimit.toIntOrNull() ?: 3

                        if (duration != null) {
                            viewModel.startCountdown(duration, ring, snooze, limit)
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackBarHostState,
                            snackbar = { snackBarData ->
                                Snackbar(
                                    snackbarData = snackBarData,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    actionColor = MaterialTheme.colorScheme.primary,
                                    dismissActionContentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            })
                    }
                )
            }
        }
    }
}
