package com.example.kotlin_test.feature.alarm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import com.example.kotlin_test.feature.alarm.domain.usecase.FindAlarm
import com.example.kotlin_test.feature.alarm.platform.AlarmActionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RingingUiState(
    val alarm: Alarm? = null,
    val isClosed: Boolean = false
)

/** Loads the ringing alarm and turns snooze/dismiss taps into app actions. */
class RingingViewModel(
    private val findAlarm: FindAlarm,
    private val actions: AlarmActionHandler
) : ViewModel() {
    private val _uiState = MutableStateFlow(RingingUiState())
    val uiState: StateFlow<RingingUiState> = _uiState.asStateFlow()

    fun loadAlarm(alarmId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(alarm = findAlarm(alarmId)) }
        }
    }

    fun snooze(alarmId: Int) {
        viewModelScope.launch {
            actions.snooze(alarmId)
            _uiState.update { it.copy(isClosed = true) }
        }
    }

    fun dismiss(alarmId: Int) {
        viewModelScope.launch {
            actions.dismiss(alarmId)
            _uiState.update { it.copy(isClosed = true) }
        }
    }
}
