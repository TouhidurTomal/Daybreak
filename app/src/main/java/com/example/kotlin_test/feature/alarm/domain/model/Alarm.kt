package com.example.kotlin_test.feature.alarm.domain.model

import java.time.DayOfWeek

/** Platform-independent alarm data used by the UI, repository, and scheduler. */
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: Set<DayOfWeek>,
    val enabled: Boolean,
    val soundUri: String = runCatching { android.provider.Settings.System.DEFAULT_NOTIFICATION_URI?.toString() }.getOrNull() ?: "",
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 5
) {
    init {
        require(hour in 0..23) { "Alarm hour must be between 0 and 23" }
        require(minute in 0..59) { "Alarm minute must be between 0 and 59" }
        require(snoozeMinutes > 0) { "Snooze minutes must be positive" }
    }
}
