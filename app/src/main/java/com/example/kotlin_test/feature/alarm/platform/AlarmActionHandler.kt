package com.example.kotlin_test.feature.alarm.platform

import com.example.kotlin_test.feature.alarm.domain.scheduler.AlarmScheduler
import com.example.kotlin_test.feature.alarm.domain.usecase.FindAlarm
import com.example.kotlin_test.feature.alarm.domain.usecase.SaveAlarm

/** Applies ring-screen and notification actions consistently, regardless of their entry point. */
class AlarmActionHandler(
    private val findAlarm: FindAlarm,
    private val saveAlarm: SaveAlarm,
    private val scheduler: AlarmScheduler,
    private val notifications: AlarmNotificationPublisher
) {
    suspend fun snooze(alarmId: Int) {
        val alarm = findAlarm(alarmId) ?: return
        if (!alarm.enabled) return
        scheduler.scheduleSnooze(alarm)
        notifications.cancel(alarmId)
    }

    suspend fun dismiss(alarmId: Int) {
        val alarm = findAlarm(alarmId) ?: return
        scheduler.cancelSnooze(alarmId)
        if (alarm.repeatDays.isEmpty()) {
            scheduler.cancel(alarmId)
            saveAlarm(alarm.copy(enabled = false))
        }
        notifications.cancel(alarmId)
    }
}
