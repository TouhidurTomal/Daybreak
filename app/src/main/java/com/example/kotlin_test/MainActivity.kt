package com.example.kotlin_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.kotlin_test.feature.alarm.presentation.AlarmRoute
import com.example.kotlin_test.feature.alarm.presentation.AlarmViewModel
import com.example.kotlin_test.ui.theme.Kotlin_testTheme

class MainActivity : ComponentActivity() {
    private lateinit var alarmViewModel: AlarmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as DaybreakApplication).container
        alarmViewModel = ViewModelProvider(this, appContainer.alarmViewModelFactory)
            .get(AlarmViewModel::class.java)

        setContent {
            Kotlin_testTheme {
                AlarmRoute(alarmViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::alarmViewModel.isInitialized) alarmViewModel.onResume()
    }
}
