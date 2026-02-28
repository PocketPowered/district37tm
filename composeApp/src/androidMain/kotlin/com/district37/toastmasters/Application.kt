package com.district37.toastmasters

import android.app.Application
import com.district37.toastmasters.di.initializeKoin
import org.koin.core.component.KoinComponent

class Application : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        initializeKoin(this)
    }
}
