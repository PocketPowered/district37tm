package com.district37.toastmasters.models

import kotlinx.datetime.Instant

/**
 * Represents a conversation (direct message or group chat)
 */
data class Conversation(
    val id: Int,
    val name: String?,
    val conversationType: ConversationType,
    val createdBy: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val participants: List<User>,
    val latestMessage: Message?,
    val hasUnread: Boolean = false
) {
    /**
     * Display name for the conversation.
     * For direct chats, shows the other participant's name.
     * For group chats, shows the group name or participant names.
     */
    fun getDisplayName(currentUserId: String): String {
        return when {
            !name.isNullOrBlank() -> name
            conversationType == ConversationType.DIRECT -> {
                participants.firstOrNull { it.id != currentUserId }?.effectiveDisplayName ?: "Direct Message"
            }
            else -> participants.take(3).joinToString(", ") { it.effectiveDisplayName }
        }
    }

    /**
     * Get profile image URL for the conversation.
     * For direct chats, shows the other participant's avatar.
     */
    fun getDisplayImageUrl(currentUserId: String): String? {
        return when (conversationType) {
            ConversationType.DIRECT -> participants.firstOrNull { it.id != currentUserId }?.profileImageUrl
            ConversationType.GROUP -> null // Could show a group icon or first participant
        }
    }
}

enum class ConversationType {
    DIRECT,
    GROUP
}

/**
 * Represents a message in a conversation
 */
data class Message(
    val id: Int,
    val conversationId: Int,
    val senderId: String,
    val content: String,
    val imageUrl: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val isEdited: Boolean,
    val sender: User?,
    val reactions: List<MessageReaction>
)

/**
 * Represents an emoji reaction on a message
 */
data class MessageReaction(
    val id: Int,
    val messageId: Int,
    val userId: String,
    val emoji: String,
    val createdAt: Instant?,
    val user: User?
)

/**
 * Paginated connection of conversations
 */
data class ConversationConnection(
    val conversations: List<Conversation>,
    val hasNextPage: Boolean,
    val endCursor: String?,
    val totalCount: Int
)

/**
 * Paginated connection of messages
 */
data class MessageConnection(
    val messages: List<Message>,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
    val startCursor: String?,
    val endCursor: String?,
    val totalCount: Int
)

/**
 * Result of marking a conversation as read
 */
data class ReadReceipt(
    val conversationId: Int,
    val userId: String,
    val lastReadAt: Instant?,
    val lastReadMessageId: Int?
)

/**
 * Update to the unread status of a conversation.
 * Used for the unreadStatusChanged subscription.
 */
data class UnreadStatusUpdate(
    val conversationId: Int,
    val hasUnread: Boolean
)
