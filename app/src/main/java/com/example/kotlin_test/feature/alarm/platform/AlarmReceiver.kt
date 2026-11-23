package com.example.kotlin_test.feature.alarm.platform

import com.example.kotlin_test.DaybreakApplication
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, -1)
        if (alarmId < 0) return

        // Room is accessed off the receiver thread; goAsync keeps the process alive until it finishes.
        val pendingResult = goAsync()
        val app = context.applicationContext as DaybreakApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val container = app.container
                val alarm = container.findAlarm(alarmId) ?: return@launch
                if (!alarm.enabled) return@launch

                val isSnooze = intent.getBooleanExtra(AndroidAlarmScheduler.EXTRA_IS_SNOOZE, false)
                // Repeating alarms schedule their next normal occurrence before ringing starts.
                if (!isSnooze && alarm.repeatDays.isNotEmpty()) {
                    container.scheduler.schedule(alarm)
                }
                container.notifications.publish(alarm)

                // Start the RingingActivity to show on screen (including lock screen)
                val ringingIntent = Intent(context, com.example.kotlin_test.feature.alarm.presentation.RingingActivity::class.java).apply {
                    putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                            Intent.FLAG_ACTIVITY_NO_USER_ACTION
                }
                context.startActivity(ringingIntent)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
