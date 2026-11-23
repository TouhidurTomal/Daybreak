package com.example.kotlin_test.feature.alarm.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

/** Pure alarm timing rules; keeping Android APIs out makes repeat behavior easy to test. */
object AlarmSchedulePolicy {
    fun nextOccurrence(alarm: Alarm, now: ZonedDateTime): ZonedDateTime {
        val time = LocalTime.of(alarm.hour, alarm.minute)
        val startDate = now.toLocalDate()

        for (offset in 0..7) {
            val date = startDate.plusDays(offset.toLong())
            val isScheduledDay = alarm.repeatDays.isEmpty() || date.dayOfWeek in alarm.repeatDays
            if (!isScheduledDay) continue

            // atZone resolves daylight-saving gaps using the zone's next valid local time.
            val candidate = LocalDateTime.of(date, time).atZone(now.zone)
            if (candidate.isAfter(now)) return candidate
        }

        // A valid weekly schedule always has an occurrence within seven days.
        error("Could not determine the next alarm occurrence")
    }

    fun repeatSummary(days: Set<DayOfWeek>): String {
        if (days.isEmpty()) return "Once"
        val weekdays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
        val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        return when (days) {
            weekdays -> "Weekdays"
            weekdays + weekend -> "Every day"
            weekend -> "Weekends"
            else -> days.sortedBy { it.value }.joinToString(" · ") {
                it.name.take(1) + it.name.substring(1, 3).lowercase()
            }
        }
    }
}
