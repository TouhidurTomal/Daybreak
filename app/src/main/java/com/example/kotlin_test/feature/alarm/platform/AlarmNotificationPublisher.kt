package com.example.kotlin_test.feature.alarm.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.net.Uri
import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import com.example.kotlin_test.feature.alarm.presentation.RingingActivity

/** Owns notification-channel creation and alarm notification details. */
class AlarmNotificationPublisher(context: Context) {
    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun publish(alarm: Alarm) {
        val channelId = "daybreak_alarm"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(channelId, "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifications when a Daybreak alarm goes off"
                    setSound(
                        android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI,
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 500)
                }
            )
        }

        val ringScreen = PendingIntent.getActivity(
            appContext,
            alarm.id,
            Intent(appContext, RingingActivity::class.java)
                .setData(Uri.parse("daybreak://ring/${alarm.id}"))
                .putExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID, alarm.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeAction = AlarmActionReceiver.pendingIntent(
            appContext, alarm.id, AlarmActionReceiver.ACTION_SNOOZE
        )
        val dismissAction = AlarmActionReceiver.pendingIntent(
            appContext, alarm.id, AlarmActionReceiver.ACTION_DISMISS
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(appContext, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(appContext)
        }
        notificationManager.notify(
            alarm.id,
            builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(alarm.label.ifBlank { "Alarm" })
                .setContentText("Your alarm is ringing")
                .setCategory(Notification.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(ringScreen)
                .setFullScreenIntent(ringScreen, true)
                .addAction(android.R.drawable.ic_media_pause, "Snooze", snoozeAction)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissAction)
                .build()
        )
    }

    fun cancel(alarmId: Int) = notificationManager.cancel(alarmId)
}
