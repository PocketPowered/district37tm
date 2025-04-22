package com.district37.toastmasters.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class NotificationRepository(database: TMDatabase) {

    private val notifcationQueries = database.notificationsQueries

    fun getAllNotifications(): Flow<List<Notification>> {
        return notifcationQueries.getAllNotifications()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    fun getUnseenNotifications(): Flow<List<Notification>> {
        return notifcationQueries.getUnseenNotifications()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    fun insertNotification(header: String, description: String) {
        notifcationQueries.insertNotification(
            header = header,
            description = description,
            seen = 0,
            time_received = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun markNotificationAsSeen(id: Long) {
        notifcationQueries.markNotificationAsSeen(id)
    }

    fun deleteNotification(id: Long) {
        notifcationQueries.deleteNotification(id)
    }

    fun markAllNotificationsAsSeen() {
        notifcationQueries.markAllNotificationsAsSeen()
    }

    fun clearAllNotifications() {
        notifcationQueries.clearAllNotifications()
    }
} 