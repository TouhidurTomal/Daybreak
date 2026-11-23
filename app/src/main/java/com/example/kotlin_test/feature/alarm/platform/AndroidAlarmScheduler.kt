package com.example.kotlin_test.feature.alarm.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.kotlin_test.MainActivity
import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import com.example.kotlin_test.feature.alarm.domain.model.AlarmSchedulePolicy
import com.example.kotlin_test.feature.alarm.domain.model.AlarmSnoozePolicy
import com.example.kotlin_test.feature.alarm.domain.scheduler.AlarmScheduler
import com.example.kotlin_test.feature.alarm.domain.scheduler.ScheduleResult
import java.time.ZonedDateTime

class AndroidAlarmScheduler(context: Context) : AlarmScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm): ScheduleResult {
        val triggerAt = AlarmSchedulePolicy.nextOccurrence(alarm, ZonedDateTime.now()).toInstant().toEpochMilli()
        return scheduleAt(triggerAt, alarmPendingIntent(alarm.id))
    }

    override fun scheduleSnooze(alarm: Alarm): ScheduleResult {
        val triggerAt = AlarmSnoozePolicy.nextSnooze(ZonedDateTime.now(), alarm.snoozeMinutes.toLong()).toInstant().toEpochMilli()
        return scheduleAt(triggerAt, alarmPendingIntent(alarm.id, isSnooze = true))
    }

    override fun cancel(alarmId: Int) = cancelPendingIntent(alarmPendingIntent(alarmId))

    override fun cancelSnooze(alarmId: Int) = cancelPendingIntent(alarmPendingIntent(alarmId, isSnooze = true))

    private fun cancelPendingIntent(operation: PendingIntent) {
        alarmManager.cancel(operation)
        operation.cancel()
    }

    private fun scheduleAt(triggerAt: Long, operation: PendingIntent): ScheduleResult {
        if (!hasExactAlarmAccess()) {
            // The inexact fallback is reported to the UI; it is never described as precise.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
            return ScheduleResult.EXACT_ACCESS_REQUIRED
        }
        return try {
            val showIntent = PendingIntent.getActivity(
                appContext,
                0,
                Intent(appContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, showIntent), operation)
            ScheduleResult.EXACT
        } catch (_: SecurityException) {
            // Special access can be revoked between the check and the API call.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
            ScheduleResult.EXACT_ACCESS_REQUIRED
        }
    }

    override fun hasExactAlarmAccess(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun alarmPendingIntent(alarmId: Int, isSnooze: Boolean = false): PendingIntent = PendingIntent.getBroadcast(
        appContext,
        if (isSnooze) alarmId xor Int.MIN_VALUE else alarmId,
        Intent(appContext, AlarmReceiver::class.java)
            .setData(Uri.parse(if (isSnooze) "daybreak://snooze/$alarmId" else "daybreak://alarm/$alarmId"))
            .putExtra(EXTRA_ALARM_ID, alarmId)
            .putExtra(EXTRA_IS_SNOOZE, isSnooze),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val EXTRA_ALARM_ID = "com.example.kotlin_test.ALARM_ID"
        const val EXTRA_IS_SNOOZE = "com.example.kotlin_test.IS_SNOOZE"
    }
}
