package com.example.kotlin_test.feature.alarm.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray
import java.util.Calendar
import java.time.DayOfWeek

/** Copies alarms from the first prototype's SharedPreferences into Room on first database creation. */
internal fun migrateLegacyAlarmPreferences(context: Context, database: SupportSQLiteDatabase) {
    val preferences = context.getSharedPreferences("daybreak_alarms", Context.MODE_PRIVATE)
    val raw = preferences.getString("items", null) ?: return
    val alarms = runCatching { JSONArray(raw) }.getOrNull() ?: return

    for (index in 0 until alarms.length()) {
        val item = alarms.optJSONObject(index) ?: continue
        val oldDays = item.optJSONArray("days") ?: JSONArray()
        val isoDays = (0 until oldDays.length()).mapNotNull { dayIndex ->
            when (oldDays.optInt(dayIndex)) {
                Calendar.SUNDAY -> DayOfWeek.SUNDAY.value
                in Calendar.MONDAY..Calendar.SATURDAY -> oldDays.optInt(dayIndex) - 1
                else -> null
            }
        }.distinct().sorted().joinToString(",")

        database.execSQL(
            """INSERT OR REPLACE INTO alarms
                (id, hour, minute, label, repeatDaysCsv, enabled) VALUES (?, ?, ?, ?, ?, ?)""",
            arrayOf(
                item.optInt("id"),
                item.optInt("hour").coerceIn(0, 23),
                item.optInt("minute").coerceIn(0, 59),
                item.optString("label", "Alarm"),
                isoDays,
                if (item.optBoolean("enabled", true)) 1 else 0
            )
        )
    }

    // Keep the old preferences until every row has been copied successfully.
    preferences.edit().remove("items").apply()
}
