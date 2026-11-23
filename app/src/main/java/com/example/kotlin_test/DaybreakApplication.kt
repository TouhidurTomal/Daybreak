package com.example.kotlin_test

import android.app.Application
import com.example.kotlin_test.core.di.AppContainer

class DaybreakApplication : Application() {
    // Create app-scoped dependencies once and share them with the Activity and broadcast receivers.
    val container: AppContainer by lazy { AppContainer(this) }
}
