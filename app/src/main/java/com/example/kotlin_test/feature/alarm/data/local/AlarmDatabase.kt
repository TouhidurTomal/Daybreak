package com.example.kotlin_test.feature.alarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE alarms ADD COLUMN soundUri TEXT NOT NULL DEFAULT '${android.provider.Settings.System.DEFAULT_NOTIFICATION_URI}'"
        )
        database.execSQL(
            "ALTER TABLE alarms ADD COLUMN vibrate INTEGER NOT NULL DEFAULT 1"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE alarms ADD COLUMN snoozeMinutes INTEGER NOT NULL DEFAULT 5"
        )
    }
}

@Database(entities = [AlarmEntity::class], version = 3, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
