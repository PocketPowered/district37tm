package com.district37.toastmasters.di

import com.district37.toastmasters.database.NotificationRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object KoinBridge: KoinComponent {

    fun getNotificationsRepository(): NotificationRepository {
        return get()
    }
}