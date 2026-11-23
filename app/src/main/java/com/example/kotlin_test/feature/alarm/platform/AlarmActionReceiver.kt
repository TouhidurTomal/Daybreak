package com.example.kotlin_test.feature.alarm.platform

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.kotlin_test.DaybreakApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Handles notification-button actions even when the app's Activity is not visible. */
class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1)
        if (alarmId < 0) return

        val pendingResult = goAsync()
        val app = context.applicationContext as DaybreakApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val actions = app.container.alarmActionHandler
                when (intent.action) {
                    ACTION_SNOOZE -> actions.snooze(alarmId)
                    ACTION_DISMISS -> actions.dismiss(alarmId)
                    else -> return@launch
                }
            } catch (exception: Exception) {
                Log.e("Daybreak", "Could not process alarm action", exception)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SNOOZE = "com.example.kotlin_test.action.SNOOZE"
        const val ACTION_DISMISS = "com.example.kotlin_test.action.DISMISS"

        fun pendingIntent(context: Context, alarmId: Int, action: String): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarmId,
                Intent(context, AlarmActionReceiver::class.java)
                    .setAction(action)
                    .setData(Uri.parse("daybreak://action/$action/$alarmId"))
                    .putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, alarmId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }
}
