package com.example.kotlin_test.feature.alarm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import com.example.kotlin_test.feature.alarm.domain.scheduler.AlarmScheduler
import com.example.kotlin_test.feature.alarm.domain.scheduler.ScheduleResult
import com.example.kotlin_test.feature.alarm.domain.usecase.DeleteAlarm
import com.example.kotlin_test.feature.alarm.domain.usecase.FindAlarm
import com.example.kotlin_test.feature.alarm.domain.usecase.GetEnabledAlarms
import com.example.kotlin_test.feature.alarm.domain.usecase.ObserveAlarms
import com.example.kotlin_test.feature.alarm.domain.usecase.SaveAlarm
import java.time.Clock
import java.time.LocalTime
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable screen state; Compose renders this instead of owning alarm records itself. */
data class AlarmUiState(
    val alarms: List<Alarm> = emptyList(),
    val currentTime: LocalTime = LocalTime.now(),
    val exactAlarmAccessRequired: Boolean = false
)

/** Coordinates UI actions, domain operations, and alarm scheduling. */
class AlarmViewModel(
    observeAlarms: ObserveAlarms,
    private val findAlarm: FindAlarm,
    private val getEnabledAlarms: GetEnabledAlarms,
    private val saveAlarm: SaveAlarm,
    private val deleteAlarm: DeleteAlarm,
    private val scheduler: AlarmScheduler,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AlarmUiState(currentTime = LocalTime.now(clock)))
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAlarms().collect { alarms ->
                _uiState.update { it.copy(alarms = alarms) }
            }
        }
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(currentTime = LocalTime.now(clock)) }
                delay(CLOCK_REFRESH_MILLIS)
            }
        }
    }

    fun createAlarm(hour: Int, minute: Int, label: String, repeatDays: Set<java.time.DayOfWeek>, soundUri: String, vibrate: Boolean, snoozeMinutes: Int) {
        val id = (UUID.randomUUID().mostSignificantBits.toInt() and Int.MAX_VALUE).coerceAtLeast(1)
        val alarm = Alarm(
            id = id,
            hour = hour,
            minute = minute,
            label = label.trim().ifBlank { "Alarm" },
            repeatDays = repeatDays,
            enabled = true,
            soundUri = soundUri,
            vibrate = vibrate,
            snoozeMinutes = snoozeMinutes
        )
        viewModelScope.launch {
            saveAlarm(alarm)
            showExactAccessPromptIfNeeded(scheduler.schedule(alarm))
        }
    }

    fun updateAlarm(id: Int, hour: Int, minute: Int, label: String, repeatDays: Set<java.time.DayOfWeek>, soundUri: String, vibrate: Boolean, snoozeMinutes: Int) {
        viewModelScope.launch {
            val current = findAlarm(id) ?: return@launch
            val updated = current.copy(
                hour = hour,
                minute = minute,
                label = label.trim().ifBlank { "Alarm" },
                repeatDays = repeatDays,
                soundUri = soundUri,
                vibrate = vibrate,
                snoozeMinutes = snoozeMinutes
            )
            saveAlarm(updated)
            if (updated.enabled) {
                showExactAccessPromptIfNeeded(scheduler.schedule(updated))
            } else {
                scheduler.cancel(id)
            }
        }
    }

    fun setAlarmEnabled(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            val current = findAlarm(id) ?: return@launch
            val updated = current.copy(enabled = enabled)
            saveAlarm(updated)
            if (enabled) {
                showExactAccessPromptIfNeeded(scheduler.schedule(updated))
            } else {
                scheduler.cancel(id)
            }
        }
    }

    fun removeAlarm(id: Int) {
        viewModelScope.launch {
            scheduler.cancel(id)
            deleteAlarm(id)
        }
    }

    /** Re-registers alarms after returning from settings or after process recreation. */
    fun onResume() {
        viewModelScope.launch {
            var accessRequired = false
            getEnabledAlarms().forEach { alarm ->
                if (scheduler.schedule(alarm) == ScheduleResult.EXACT_ACCESS_REQUIRED) {
                    accessRequired = true
                }
            }
            _uiState.update { it.copy(exactAlarmAccessRequired = accessRequired) }
        }
    }

    fun dismissExactAlarmPrompt() {
        _uiState.update { it.copy(exactAlarmAccessRequired = false) }
    }

    private fun showExactAccessPromptIfNeeded(result: ScheduleResult) {
        if (result == ScheduleResult.EXACT_ACCESS_REQUIRED) {
            _uiState.update { it.copy(exactAlarmAccessRequired = true) }
        }
    }

    companion object {
        private const val CLOCK_REFRESH_MILLIS = 1_000L
    }
}
