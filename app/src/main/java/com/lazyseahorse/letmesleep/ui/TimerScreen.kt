package com.lazyseahorse.letmesleep.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    onStartClick: () -> Unit,
    snackbarHost: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF1A1A1A), // Dark background
        snackbarHost = snackbarHost
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated timer text
                TimerDisplay(time = time, angle = angle)

                Spacer(modifier = Modifier.height(30.dp))

                TimerInput(
                    value = userValue,
                    onValueChange = onUserValueChange
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = onStartClick,
                    modifier = Modifier.size(width = 200.dp, height = 50.dp)
                ) {
                    Text("Start", color = Color.White)
                }
            }
        }
    }
}
