package com.lazyseahorse.letmesleep.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lazyseahorse.letmesleep.ui.components.TimerDisplay
import com.lazyseahorse.letmesleep.ui.components.TimerInput

@Composable
fun TimerScreen(
    time: Int,
    angle: Float,
    userValue: String,
    onUserValueChange: (String) -> Unit,
    ringDuration: String,
    onRingDurationChange: (String) -> Unit,
    snoozeDuration: String,
    onSnoozeDurationChange: (String) -> Unit,
    snoozeLimit: String,
    onSnoozeLimitChange: (String) -> Unit,
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated timer text
            TimerDisplay(time = time, angle = angle)

            Spacer(modifier = Modifier.height(30.dp))

            TimerInput(
                value = userValue,
                onValueChange = onUserValueChange,
                label = "Timer Duration (s)"
            )

            TimerInput(
                value = ringDuration,
                onValueChange = onRingDurationChange,
                label = "Ring Duration (s)"
            )

            TimerInput(
                value = snoozeDuration,
                onValueChange = onSnoozeDurationChange,
                label = "Snooze Duration (s)"
            )

            TimerInput(
                value = snoozeLimit,
                onValueChange = onSnoozeLimitChange,
                label = "Auto Snooze Limit"
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onStartClick,
                modifier = Modifier.size(width = 200.dp, height = 50.dp)
            ) {
                Text("Start")
            }
        }
    }
}
