package com.district37.toastmasters.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class NotificationRepository(private val database: NotificationDatabase) {
    
    fun getAllNotifications(): Flow<List<Notification>> {
        return database.notificationDatabaseQueries.getAllNotifications()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    fun getUnseenNotifications(): Flow<List<Notification>> {
        return database.notificationDatabaseQueries.getUnseenNotifications()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    fun insertNotification(header: String, description: String) {
        database.notificationDatabaseQueries.insertNotification(
            header = header,
            description = description,
            seen = 0,
            time_received = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    fun markNotificationAsSeen(id: Long) {
        database.notificationDatabaseQueries.markNotificationAsSeen(id)
    }
    
    fun deleteNotification(id: Long) {
        database.notificationDatabaseQueries.deleteNotification(id)
    }

    fun markAllNotificationsAsSeen() {
        database.notificationDatabaseQueries.markAllNotificationsAsSeen()
    }

    fun clearAllNotifications() {
        database.notificationDatabaseQueries.clearAllNotifications()
    }
} 