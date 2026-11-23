package com.example.kotlin_test.feature.alarm.domain.scheduler

import com.example.kotlin_test.feature.alarm.domain.model.Alarm

/** Abstracts Android AlarmManager so the ViewModel does not depend on platform classes. */
interface AlarmScheduler {
    fun schedule(alarm: Alarm): ScheduleResult
    fun scheduleSnooze(alarm: Alarm): ScheduleResult
    fun cancel(alarmId: Int)
    fun cancelSnooze(alarmId: Int)
    fun hasExactAlarmAccess(): Boolean
}

/** A false/permission-required result is surfaced to the user instead of being called exact. */
enum class ScheduleResult {
    EXACT,
    EXACT_ACCESS_REQUIRED
}
