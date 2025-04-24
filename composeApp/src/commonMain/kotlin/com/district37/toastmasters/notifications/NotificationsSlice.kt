package com.district37.toastmasters.notifications

import com.district37.toastmasters.database.NotificationRepository
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationsSlice : ViewModelSlice(), KoinComponent {
    private val notificationRepository: NotificationRepository by inject()
    private val notificationPermissions: NotificationPermissions by inject()

    private val _notificationsFlow = MutableStateFlow<List<Notification>>(emptyList())
    val notificationsFlow = _notificationsFlow

    private val _unseenNotificationCount = MutableStateFlow(0)
    val unseenNotificationCount = _unseenNotificationCount

    val notificationPermissionState: StateFlow<NotificationPermissionState> =
        notificationPermissions.permissionState

    override fun afterInit() {
        super.afterInit()

        // Collect all notifications
        sliceScope.launch(Dispatchers.IO) {
            notificationRepository.getAllNotifications()
                .collect { notifications ->
                    _notificationsFlow.value = notifications.map { dbNotification ->
                        Notification(
                            id = dbNotification.id,
                            header = dbNotification.header_,
                            description = dbNotification.description,
                            seen = dbNotification.seen == 1L,
                            timeReceived = kotlinx.datetime.Instant.fromEpochMilliseconds(
                                dbNotification.time_received
                            )
                        )
                    }
                }
        }

        // Collect unseen notifications count
        sliceScope.launch(Dispatchers.IO) {
            notificationRepository.getUnseenNotifications()
                .collect { notifications ->
                    _unseenNotificationCount.value = notifications.size
                }
        }
    }

    fun markAllNotificationsAsSeen() {
        notificationRepository.markAllNotificationsAsSeen()
    }

    fun clearAllNotifications() {
        notificationRepository.clearAllNotifications()
    }

    fun markNotificationAsSeen(id: Long) {
        notificationRepository.markNotificationAsSeen(id)
    }

    fun deleteNotification(id: Long) {
        sliceScope.launch(Dispatchers.IO) {
            notificationRepository.deleteNotification(id)
        }
    }

    suspend fun requestNotificationPermission() {
        notificationPermissions.requestPermission()
    }

    fun openNotificationSettings() {
        notificationPermissions.openNotificationSettings()
    }
}