package com.example.kotlin_test.feature.alarm.domain

import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import com.example.kotlin_test.feature.alarm.domain.model.AlarmSchedulePolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmSchedulePolicyTest {
    private val zone = ZoneId.of("Asia/Dhaka")

    @Test
    fun oneShotAlarmWhoseTimeHasPassedMovesToTomorrow() {
        val now = at(2026, 3, 16, 8, 0)
        val alarm = alarm(hour = 7, minute = 30)

        val next = AlarmSchedulePolicy.nextOccurrence(alarm, now)

        assertEquals(at(2026, 3, 17, 7, 30), next)
    }

    @Test
    fun weekdayAlarmSkipsTheWeekend() {
        val now = at(2026, 3, 20, 8, 0) // Friday
        val alarm = alarm(
            hour = 7,
            minute = 15,
            repeatDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )
        )

        val next = AlarmSchedulePolicy.nextOccurrence(alarm, now)

        assertEquals(at(2026, 3, 23, 7, 15), next)
    }

    @Test
    fun repeatingAlarmUsesTodayWhenItsTimeIsStillAhead() {
        val now = at(2026, 3, 16, 6, 0) // Monday
        val alarm = alarm(hour = 7, minute = 0, repeatDays = setOf(DayOfWeek.MONDAY))

        val next = AlarmSchedulePolicy.nextOccurrence(alarm, now)

        assertEquals(at(2026, 3, 16, 7, 0), next)
    }

    private fun alarm(
        hour: Int,
        minute: Int,
        repeatDays: Set<DayOfWeek> = emptySet()
    ) = Alarm(
        id = 1,
        hour = hour,
        minute = minute,
        label = "Test alarm",
        repeatDays = repeatDays,
        enabled = true
    )

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): ZonedDateTime =
        LocalDateTime.of(year, month, day, hour, minute).atZone(zone)
}
