package com.district37.toastmasters.messaging

import com.district37.toastmasters.data.repository.MessagingRepository
import com.district37.toastmasters.models.Conversation
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Singleton manager for tracking unread message state across the app.
 * Used to show unread indicators on:
 * - The messaging icon in the TopAppBar (any unread = show dot)
 * - Individual conversations in the list (per-conversation unread status)
 */
class UnreadMessagesManager(
    private val messagingRepository: MessagingRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _hasAnyUnread = MutableStateFlow(false)
    val hasAnyUnread: StateFlow<Boolean> = _hasAnyUnread.asStateFlow()

    private val _conversationUnreadStatus = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val conversationUnreadStatus: StateFlow<Map<Int, Boolean>> = _conversationUnreadStatus.asStateFlow()

    private var subscriptionJob: Job? = null
    private var isStarted = false

    /**
     * Start listening for unread status updates.
     * Should be called when user logs in.
     */
    fun start() {
        if (isStarted) {
            Logger.d(TAG, "UnreadMessagesManager already started")
            return
        }
        isStarted = true
        Logger.d(TAG, "Starting UnreadMessagesManager")

        subscriptionJob?.cancel()
        subscriptionJob = scope.launch {
            // Initial load
            refreshUnreadStatus()

            // Subscribe to real-time updates
            messagingRepository.subscribeToUnreadStatusChanges()
                .catch { error ->
                    Logger.e(TAG, "Subscription error: ${error.message}")
                }
                .collect { update ->
                    Logger.d(TAG, "Received unread status update: conversation=${update.conversationId}, hasUnread=${update.hasUnread}")
                    _conversationUnreadStatus.update { current ->
                        current + (update.conversationId to update.hasUnread)
                    }
                    updateHasAnyUnread()
                }
        }
    }

    /**
     * Stop listening and reset state.
     * Should be called when user logs out.
     */
    fun stop() {
        Logger.d(TAG, "Stopping UnreadMessagesManager")
        isStarted = false
        subscriptionJob?.cancel()
        subscriptionJob = null
        _hasAnyUnread.value = false
        _conversationUnreadStatus.value = emptyMap()
    }

    /**
     * Refresh unread status from server.
     * Call this when refreshing the conversations list.
     */
    suspend fun refreshUnreadStatus() {
        Logger.d(TAG, "Refreshing unread status")
        val result = messagingRepository.hasAnyUnreadConversations()
        if (result is Resource.Success) {
            _hasAnyUnread.value = result.data
            Logger.d(TAG, "Refreshed hasAnyUnread: ${result.data}")
        } else if (result is Resource.Error) {
            Logger.e(TAG, "Failed to refresh unread status: ${result.message}")
        }
    }

    /**
     * Mark a conversation as read locally (optimistic update).
     * Call this when opening a conversation.
     */
    fun markConversationAsRead(conversationId: Int) {
        Logger.d(TAG, "Marking conversation $conversationId as read locally")
        _conversationUnreadStatus.update { current ->
            current + (conversationId to false)
        }
        updateHasAnyUnread()
    }

    /**
     * Update conversations' unread status from a loaded list.
     * Call this after loading conversations from the server.
     */
    fun updateFromConversations(conversations: List<Conversation>) {
        Logger.d(TAG, "Updating from ${conversations.size} conversations")
        _conversationUnreadStatus.update { current ->
            current + conversations.associate { it.id to it.hasUnread }
        }
        updateHasAnyUnread()
    }

    /**
     * Check if a specific conversation has unread messages.
     * Uses the locally cached state.
     */
    fun hasUnread(conversationId: Int): Boolean {
        return _conversationUnreadStatus.value[conversationId] ?: false
    }

    private fun updateHasAnyUnread() {
        val anyUnread = _conversationUnreadStatus.value.values.any { it }
        _hasAnyUnread.value = anyUnread
        Logger.d(TAG, "Updated hasAnyUnread: $anyUnread")
    }

    companion object {
        private const val TAG = "UnreadMessagesManager"
    }
}
