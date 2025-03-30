package com.district37.toastmasters

import android.app.Application
import com.district37.toastmasters.di.initializeKoin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.crashlytics
import dev.gitlive.firebase.initialize
import org.koin.core.component.KoinComponent

class Application : Application(), KoinComponent {

    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        initializeKoin(this)
        initializeFirebase()
    }

    private fun initializeFirebase() {
        Firebase.initialize(context = this)
        analytics = Firebase.analytics
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
    }
}