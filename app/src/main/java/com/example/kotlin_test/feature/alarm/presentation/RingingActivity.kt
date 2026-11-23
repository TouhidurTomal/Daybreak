package com.example.kotlin_test.feature.alarm.presentation

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.kotlin_test.feature.alarm.platform.AndroidAlarmScheduler
import com.example.kotlin_test.ui.theme.Kotlin_testTheme

class RingingActivity : ComponentActivity() {
    private lateinit var ringingViewModel: RingingViewModel
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        val appContainer = (application as com.example.kotlin_test.DaybreakApplication).container
        ringingViewModel = ViewModelProvider(this, appContainer.ringingViewModelFactory)
            .get(RingingViewModel::class.java)

        val alarmId = intent.getIntExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1)
        if (alarmId != -1) {
            ringingViewModel.loadAlarm(alarmId)
        }

        setContent {
            Kotlin_testTheme {
                RingingRoute(
                    viewModel = ringingViewModel,
                    alarmId = alarmId,
                    onStartRinging = { soundUri, vibrate ->
                        startRinging(soundUri, vibrate)
                    },
                    onClose = {
                        stopRinging()
                        finish()
                    }
                )
            }
        }
    }

    private fun startRinging(soundUriStr: String, vibrate: Boolean) {
        if (mediaPlayer != null) return

        try {
            val soundUri = if (soundUriStr.isNotBlank()) Uri.parse(soundUriStr) else android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(applicationContext, soundUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(applicationContext, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (_: Exception) {}
        }

        if (vibrate) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }
    }

    private fun stopRinging() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        vibrator = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
    }
}
