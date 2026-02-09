package com.district37.toastmasters.features.notifications

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.data.repository.NotificationRepository
import com.district37.toastmasters.infra.PushNotificationService
import com.district37.toastmasters.models.Notification
import com.district37.toastmasters.models.Platform
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State for the notification center
 */
data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val pushPermissionGranted: Boolean = false,
    val deviceTokenRegistered: Boolean = false
)

/**
 * ViewModel for managing notifications
 */
class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val pushNotificationService: PushNotificationService,
    private val platform: Platform
) : LoggingViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var currentCursor: String? = null

    init {
        // Don't auto-load - wait for explicit trigger when user is authenticated
        // Loading is triggered by:
        // 1. ExploreScreen when user becomes authenticated
        // 2. NotificationCenterScreen when it's displayed
        checkPushPermissions()
    }

    /**
     * Load initial notifications
     */
    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            currentCursor = null

            when (val result = notificationRepository.getNotifications()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            notifications = result.data.items,
                            hasMore = result.data.hasNextPage,
                            isLoading = false
                        )
                    }
                    currentCursor = result.data.endCursor
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load notifications"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * Load more notifications (pagination)
     */
    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            when (val result = notificationRepository.getNotifications(cursor = currentCursor)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            notifications = it.notifications + result.data.items,
                            hasMore = result.data.hasNextPage,
                            isLoadingMore = false
                        )
                    }
                    currentCursor = result.data.endCursor
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * Refresh notifications list
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            currentCursor = null

            when (val result = notificationRepository.getNotifications()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            notifications = result.data.items,
                            hasMore = result.data.hasNextPage,
                            isRefreshing = false
                        )
                    }
                    currentCursor = result.data.endCursor
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }

            // Also refresh unread count
            loadUnreadCount()
        }
    }

    /**
     * Load unread notification count
     */
    fun loadUnreadCount() {
        viewModelScope.launch {
            when (val result = notificationRepository.getUnreadCount()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(unreadCount = result.data) }
                }
                else -> {}
            }
        }
    }

    /**
     * Mark a notification as read
     */
    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            when (val result = notificationRepository.markAsRead(notificationId)) {
                is Resource.Success -> {
                    // Update the notification in the list
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { notification ->
                                if (notification.id == notificationId) {
                                    notification.copy(isRead = true)
                                } else {
                                    notification
                                }
                            },
                            unreadCount = (state.unreadCount - 1).coerceAtLeast(0)
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            when (val result = notificationRepository.markAllAsRead()) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(isRead = true) },
                            unreadCount = 0
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * Check if push notifications are enabled
     */
    private fun checkPushPermissions() {
        viewModelScope.launch {
            val enabled = pushNotificationService.areNotificationsEnabled()
            _uiState.update { it.copy(pushPermissionGranted = enabled) }
        }
    }

    /**
     * Request push notification permission and register device token
     */
    fun enablePushNotifications() {
        viewModelScope.launch {
            val granted = pushNotificationService.requestNotificationPermission()
            _uiState.update { it.copy(pushPermissionGranted = granted) }

            if (granted) {
                registerDeviceToken()
            }
        }
    }

    /**
     * Handle the result of a notification permission request from the UI
     */
    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(pushPermissionGranted = granted) }
        if (granted) {
            registerDeviceToken()
        }
    }

    /**
     * Register the device token with the server
     */
    fun registerDeviceToken() {
        viewModelScope.launch {
            val fcmToken = pushNotificationService.getFcmToken()
            if (fcmToken != null) {
                when (notificationRepository.registerDeviceToken(
                    fcmToken = fcmToken,
                    platform = platform,
                    deviceName = null
                )) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(deviceTokenRegistered = true) }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Unregister device token (call on logout)
     */
    suspend fun unregisterDeviceToken() {
        val fcmToken = pushNotificationService.getFcmToken()
        if (fcmToken != null) {
            notificationRepository.unregisterDeviceToken(fcmToken)
            pushNotificationService.deleteFcmToken()
        }
        _uiState.update { it.copy(deviceTokenRegistered = false) }
    }
}
