package com.jarves.mh

import android.app.Application

/**
 * Process-wide bootstrap. Installed before activities/services so fatal Java/Kotlin
 * startup exceptions are persisted before Android terminates the process.
 */
class MobileHarnessApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCrashLogger.initialize(this)
        AppCrashLogger.log("Application.onCreate completed")
    }
}
