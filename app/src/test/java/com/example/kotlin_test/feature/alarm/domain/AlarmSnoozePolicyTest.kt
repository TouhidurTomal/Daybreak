package com.example.kotlin_test.feature.alarm.domain

import com.example.kotlin_test.feature.alarm.domain.model.AlarmSnoozePolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmSnoozePolicyTest {
    @Test
    fun snoozeUsesFiveMinutesAndCrossesMidnightCorrectly() {
        val zone = ZoneId.of("Asia/Dhaka")
        val now = LocalDateTime.of(2026, 3, 16, 23, 58).atZone(zone)

        val next = AlarmSnoozePolicy.nextSnooze(now)

        assertEquals(LocalDateTime.of(2026, 3, 17, 0, 3).atZone(zone), next)
    }
}
