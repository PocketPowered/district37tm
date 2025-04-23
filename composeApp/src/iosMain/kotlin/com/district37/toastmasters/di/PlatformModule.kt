package com.district37.toastmasters.di

import com.district37.toastmasters.notifications.NotificationPermissions
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(context: Any?): Module = module {
    single { NotificationPermissions() }
} 