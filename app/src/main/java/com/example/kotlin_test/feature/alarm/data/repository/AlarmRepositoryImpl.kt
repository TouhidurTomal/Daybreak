package com.example.kotlin_test.feature.alarm.data.repository

import com.example.kotlin_test.feature.alarm.data.local.AlarmDao
import com.example.kotlin_test.feature.alarm.data.local.toDomain
import com.example.kotlin_test.feature.alarm.data.local.toEntity
import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import com.example.kotlin_test.feature.alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Converts the Room representation to the domain model at the repository boundary. */
class AlarmRepositoryImpl(private val alarmDao: AlarmDao) : AlarmRepository {
    override fun observeAlarms(): Flow<List<Alarm>> =
        alarmDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun findAlarm(id: Int): Alarm? = alarmDao.findById(id)?.toDomain()

    override suspend fun enabledAlarms(): List<Alarm> =
        alarmDao.findEnabled().map { it.toDomain() }

    override suspend fun saveAlarm(alarm: Alarm) {
        alarmDao.upsert(alarm.toEntity())
    }

    override suspend fun deleteAlarm(id: Int) {
        alarmDao.deleteById(id)
    }
}
