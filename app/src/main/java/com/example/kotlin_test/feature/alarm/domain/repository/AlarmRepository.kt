package com.example.kotlin_test.feature.alarm.domain.repository

import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

/** The domain-facing contract keeps storage details out of ViewModels and use cases. */
interface AlarmRepository {
    fun observeAlarms(): Flow<List<Alarm>>
    suspend fun findAlarm(id: Int): Alarm?
    suspend fun enabledAlarms(): List<Alarm>
    suspend fun saveAlarm(alarm: Alarm)
    suspend fun deleteAlarm(id: Int)
}
