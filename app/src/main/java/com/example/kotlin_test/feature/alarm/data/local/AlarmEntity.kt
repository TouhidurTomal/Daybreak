package com.example.kotlin_test.feature.alarm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import java.time.DayOfWeek

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    /** ISO weekday values (Monday=1 through Sunday=7), stored as a compact CSV string. */
    val repeatDaysCsv: String,
    val enabled: Boolean,
    val soundUri: String = runCatching { android.provider.Settings.System.DEFAULT_NOTIFICATION_URI?.toString() }.getOrNull() ?: "",
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 5
)

internal fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    repeatDays = repeatDaysCsv.split(',')
        .mapNotNull { it.toIntOrNull()?.let { value -> runCatching { DayOfWeek.of(value) }.getOrNull() } }
        .toSet(),
    enabled = enabled,
    soundUri = soundUri,
    vibrate = vibrate,
    snoozeMinutes = snoozeMinutes
)

internal fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    repeatDaysCsv = repeatDays.map { it.value }.sorted().joinToString(","),
    enabled = enabled,
    soundUri = soundUri,
    vibrate = vibrate,
    snoozeMinutes = snoozeMinutes
)
