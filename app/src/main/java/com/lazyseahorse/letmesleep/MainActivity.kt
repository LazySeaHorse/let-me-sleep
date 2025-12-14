package com.lazyseahorse.letmesleep

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.launch
import com.lazyseahorse.letmesleep.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val isAlarmTriggered = mutableStateOf(false)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    private fun checkIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra(com.lazyseahorse.letmesleep.utils.Constants.EXTRA_SHOW_ALARM_UI, false) == true) {
            isAlarmTriggered.value = true
        }
    }

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val vibrationHelper = VibrationHelper(this)
        checkIntent(intent)

        setContent {
            LetMeSleepTheme {
                if (isAlarmTriggered.value) {
                    com.lazyseahorse.letmesleep.ui.AlarmTriggeredScreen(
                        onSnooze = {
                            startService(android.content.Intent(this, com.lazyseahorse.letmesleep.service.TimerService::class.java).apply {
                                action = com.lazyseahorse.letmesleep.utils.Constants.ACTION_SNOOZE_ALARM
                            })
                            isAlarmTriggered.value = false
                        },
                        onDismiss = {
                            startService(android.content.Intent(this, com.lazyseahorse.letmesleep.service.TimerService::class.java).apply {
                                action = com.lazyseahorse.letmesleep.utils.Constants.ACTION_STOP_ALARM
                            })
                            isAlarmTriggered.value = false
                        }
                    )
                } else {
                    var userValue by remember { mutableStateOf("") }
                var ringDuration by remember { mutableStateOf("10") }
                var snoozeDuration by remember { mutableStateOf("5") }
                var snoozeLimit by remember { mutableStateOf("3") }

                val viewModel = viewModel<MainViewModel>()
                val time = viewModel.countdownFlow.collectAsState(initial = 0)
                val isTimerComplete = viewModel.isCountdownComplete.collectAsState()
                val snackBarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (isGranted) {
                             // Retry logic could go here, or just let user click again
                             scope.launch {
                                 snackBarHostState.showSnackbar("Permission granted! Press Start again.")
                             }
                        } else {
                            scope.launch {
                                snackBarHostState.showSnackbar("Notifications needed for alarm UI.")
                            }
                        }
                    }
                )
                
                // --- Debug Feature Flag ---
                val SHOW_DEBUG_TAB = true // Set to FALSE to disable in production
                
                var selectedTab by remember { mutableStateOf(0) } // 0 = Timer, 1 = Debug

                Scaffold(
                    bottomBar = {
                        if (SHOW_DEBUG_TAB) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    label = { Text("Timer") },
                                    icon = {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Home,
                                            contentDescription = "Timer"
                                        ) 
                                    }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    label = { Text("Debug") },
                                    icon = {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Info, // Or List
                                            contentDescription = "Debug"
                                        ) 
                                    }
                                )
                            }
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
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                         if (selectedTab == 0) {
                             LaunchedEffect(isTimerComplete.value) {
                                 if (isTimerComplete.value) {
                                     vibrationHelper.vibrateOnComplete()

                                     snackBarHostState.showSnackbar(
                                         message = "Timer Completed",
                                         duration = SnackbarDuration.Short
                                     )
                                 }
                             }

                             val totalTime by viewModel.totalTimeFlow.collectAsState()
                             val angle = if (totalTime > 0) {
                                 (time.value.toFloat() / totalTime.toFloat()) * 360f
                             } else {
                                 0f
                             }

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
                                         // Permission Check for Android 13+
                                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                             if (ContextCompat.checkSelfPermission(
                                                     this@MainActivity,
                                                     Manifest.permission.POST_NOTIFICATIONS
                                                 ) == PackageManager.PERMISSION_GRANTED
                                             ) {
                                                 viewModel.startCountdown(duration, ring, snooze, limit)
                                             } else {
                                                 // Request Permission (Optimistic: Start timer after grant is handled in launcher, 
                                                 // but for simplicity, we just ask here. User has to click start again or we handle callback better)
                                                 // Ideally, we'd trigger a callback in launcher, but straightforward request is better than crash/silent fail.
                                                 permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                             }
                                         } else {
                                             viewModel.startCountdown(duration, ring, snooze, limit)
                                         }
                                     }
                                 },
                                 snackbarHost = {} 
                             )
                         } else {
                             com.lazyseahorse.letmesleep.ui.DebugScreen()
                         }
                    }
                }
            }
            }
        }
    }
}

