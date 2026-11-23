package com.example.kotlin_test.feature.alarm.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.kotlin_test.DaybreakApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val app = context.applicationContext as DaybreakApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.container.rescheduleEnabledAlarmsNow()
            } catch (exception: Exception) {
                Log.e("Daybreak", "Could not restore alarms after reboot", exception)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
