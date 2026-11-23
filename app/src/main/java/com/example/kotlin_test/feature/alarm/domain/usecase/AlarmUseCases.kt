package com.example.kotlin_test.feature.alarm.domain.usecase

import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import com.example.kotlin_test.feature.alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow

class ObserveAlarms(private val repository: AlarmRepository) {
    operator fun invoke(): Flow<List<Alarm>> = repository.observeAlarms()
}

class FindAlarm(private val repository: AlarmRepository) {
    suspend operator fun invoke(id: Int): Alarm? = repository.findAlarm(id)
}

class GetEnabledAlarms(private val repository: AlarmRepository) {
    suspend operator fun invoke(): List<Alarm> = repository.enabledAlarms()
}

class SaveAlarm(private val repository: AlarmRepository) {
    suspend operator fun invoke(alarm: Alarm) = repository.saveAlarm(alarm)
}

class DeleteAlarm(private val repository: AlarmRepository) {
    suspend operator fun invoke(id: Int) = repository.deleteAlarm(id)
}
