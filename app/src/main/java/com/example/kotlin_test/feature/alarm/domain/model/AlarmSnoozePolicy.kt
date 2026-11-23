package com.example.kotlin_test.feature.alarm.domain.model

import java.time.ZonedDateTime

/** Keeps snooze timing deterministic and independent from Android's alarm APIs. */
object AlarmSnoozePolicy {
    const val DEFAULT_SNOOZE_MINUTES = 5L

    fun nextSnooze(now: ZonedDateTime, minutes: Long = DEFAULT_SNOOZE_MINUTES): ZonedDateTime {
        require(minutes > 0) { "Snooze duration must be positive" }
        return now.plusMinutes(minutes)
    }
}
