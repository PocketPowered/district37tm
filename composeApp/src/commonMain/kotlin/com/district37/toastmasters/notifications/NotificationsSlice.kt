package com.district37.toastmasters.notifications

import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsSlice : ViewModelSlice() {

    private val _notificationsFlow = MutableStateFlow<List<Notification>>(emptyList())
    val notificationsFlow = _notificationsFlow

    private val _unseenNotificationCount = MutableStateFlow(0)
    val unseenNotificationCount = _unseenNotificationCount

    override fun afterInit() {
        super.afterInit()
        sliceScope.launch {
            _notificationsFlow.collect { notifications ->
                _unseenNotificationCount.value = notifications.count { !it.seen }
            }
        }
        _notificationsFlow.update {
            listOf(
                Notification(
                    "Registration is happening now!",
                    "Head to the Near Queens Ballroom"
                ),
                Notification(
                    "Event moved",
                    "Event moved to somewhere else"
                )
            )
        }
    }
}