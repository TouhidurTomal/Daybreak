package com.example.kotlin_test.core.di

import android.content.Context
import androidx.room.Room
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kotlin_test.feature.alarm.data.local.AlarmDatabase
import com.example.kotlin_test.feature.alarm.data.local.MIGRATION_1_2
import com.example.kotlin_test.feature.alarm.data.local.MIGRATION_2_3
import com.example.kotlin_test.feature.alarm.data.local.migrateLegacyAlarmPreferences
import com.example.kotlin_test.feature.alarm.data.repository.AlarmRepositoryImpl
import com.example.kotlin_test.feature.alarm.domain.usecase.DeleteAlarm
import com.example.kotlin_test.feature.alarm.domain.usecase.FindAlarm
import com.example.kotlin_test.feature.alarm.domain.usecase.GetEnabledAlarms
import com.example.kotlin_test.feature.alarm.domain.usecase.ObserveAlarms
import com.example.kotlin_test.feature.alarm.domain.usecase.SaveAlarm
import com.example.kotlin_test.feature.alarm.platform.AlarmActionHandler
import com.example.kotlin_test.feature.alarm.platform.AlarmNotificationPublisher
import com.example.kotlin_test.feature.alarm.platform.AndroidAlarmScheduler
import com.example.kotlin_test.feature.alarm.presentation.AlarmViewModel
import com.example.kotlin_test.feature.alarm.presentation.RingingViewModel

/** Manual dependency container keeps the small app explicit without adding a DI framework. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database by lazy {
        Room.databaseBuilder(appContext, AlarmDatabase::class.java, DATABASE_NAME)
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Preserve records created by the first prototype when Room is introduced.
                    migrateLegacyAlarmPreferences(appContext, db)
                }
            })
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    private val repository by lazy { AlarmRepositoryImpl(database.alarmDao()) }
    val scheduler = AndroidAlarmScheduler(appContext)
    val notifications = AlarmNotificationPublisher(appContext)
    val alarmActionHandler by lazy {
        AlarmActionHandler(findAlarmUseCase, saveAlarmUseCase, scheduler, notifications)
    }

    private val observeAlarms by lazy { ObserveAlarms(repository) }
    private val findAlarmUseCase by lazy { FindAlarm(repository) }
    private val getEnabledAlarms by lazy { GetEnabledAlarms(repository) }
    private val saveAlarmUseCase by lazy { SaveAlarm(repository) }
    private val deleteAlarmUseCase by lazy { DeleteAlarm(repository) }

    val alarmViewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (!modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
                    error("Unsupported ViewModel: ${modelClass.name}")
                }
                return AlarmViewModel(
                    observeAlarms = observeAlarms,
                    findAlarm = findAlarmUseCase,
                    getEnabledAlarms = getEnabledAlarms,
                    saveAlarm = saveAlarmUseCase,
                    deleteAlarm = deleteAlarmUseCase,
                    scheduler = scheduler
                ) as T
            }
        }
    }

    val ringingViewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (!modelClass.isAssignableFrom(RingingViewModel::class.java)) {
                    error("Unsupported ViewModel: ${modelClass.name}")
                }
                return RingingViewModel(findAlarmUseCase, alarmActionHandler) as T
            }
        }
    }

    suspend fun findAlarm(id: Int) = findAlarmUseCase(id)
    suspend fun saveAlarm(alarm: com.example.kotlin_test.feature.alarm.domain.model.Alarm) = saveAlarmUseCase(alarm)

    suspend fun rescheduleEnabledAlarmsNow() {
        getEnabledAlarms().forEach { scheduler.schedule(it) }
    }

    private companion object {
        const val DATABASE_NAME = "daybreak.db"
    }
}
