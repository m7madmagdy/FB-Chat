package com.example.socialmedia

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }
}