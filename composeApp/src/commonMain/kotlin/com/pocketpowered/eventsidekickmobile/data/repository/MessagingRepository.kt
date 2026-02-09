package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.district37.toastmasters.graphql.*
import com.district37.toastmasters.graphql.fragment.ConversationPreview
import com.district37.toastmasters.graphql.fragment.MessagePreview
import com.district37.toastmasters.graphql.fragment.MessageReactionPreview
import com.district37.toastmasters.graphql.fragment.UserPreview
import com.district37.toastmasters.graphql.type.AddReactionInput
import com.district37.toastmasters.graphql.type.CreateConversationInput
import com.district37.toastmasters.graphql.type.EditMessageInput
import com.district37.toastmasters.graphql.type.MarkAsReadInput
import com.district37.toastmasters.graphql.type.RemoveReactionInput
import com.district37.toastmasters.graphql.type.SendMessageInput
import com.district37.toastmasters.models.*
import com.district37.toastmasters.util.ErrorType
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Instant

/**
 * Repository for messaging-related operations
 */
class MessagingRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "MessagingRepository"

    // ========== Conversation Operations ==========

    /**
     * Get the current user's conversations
     */
    suspend fun getMyConversations(
        first: Int = 20,
        after: String? = null
    ): Resource<ConversationConnection> {
        return executeQuery(
            queryName = "getMyConversations(first=$first, after=$after)",
            query = {
                apolloClient.query(
                    GetMyConversationsQuery(
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after)
                    )
                ).execute()
            },
            transform = { data ->
                val conversations = data.myConversations.edges.mapNotNull { edge ->
                    edge.node.conversationPreview.toConversation()
                }
                ConversationConnection(
                    conversations = conversations,
                    hasNextPage = data.myConversations.pageInfo.hasNextPage,
                    endCursor = data.myConversations.pageInfo.endCursor,
                    totalCount = data.myConversations.totalCount
                )
            }
        )
    }

    /**
     * Get a specific conversation by ID
     */
    suspend fun getConversation(id: Int): Resource<Conversation?> {
        return executeQuery(
            queryName = "getConversation(id=$id)",
            query = {
                apolloClient.query(GetConversationQuery(id = id)).execute()
            },
            transform = { data ->
                data.conversation?.conversationPreview?.toConversation()
            }
        )
    }

    /**
     * Create a new conversation (direct or group)
     */
    suspend fun createConversation(
        participantIds: List<String>,
        name: String? = null,
        initialMessage: String? = null
    ): Resource<Conversation> {
        return executeMutation(
            mutationName = "createConversation",
            mutation = {
                apolloClient.mutation(
                    CreateConversationMutation(
                        input = CreateConversationInput(
                            participantIds = participantIds,
                            name = Optional.presentIfNotNull(name),
                            initialMessage = Optional.presentIfNotNull(initialMessage)
                        )
                    )
                ).execute()
            },
            transform = { data ->
                data.createConversation.conversationPreview.toConversation()
            }
        )
    }

    /**
     * Get or create a direct conversation with another user
     */
    suspend fun getOrCreateDirectConversation(otherUserId: String): Resource<Conversation> {
        return executeMutation(
            mutationName = "getOrCreateDirectConversation",
            mutation = {
                apolloClient.mutation(
                    GetOrCreateDirectConversationMutation(otherUserId = otherUserId)
                ).execute()
            },
            transform = { data ->
                data.getOrCreateDirectConversation.conversationPreview.toConversation()
            }
        )
    }

    // ========== Message Operations ==========

    /**
     * Get messages in a conversation (paginated)
     */
    suspend fun getMessagesInConversation(
        conversationId: Int,
        first: Int? = null,
        after: String? = null,
        last: Int? = null,
        before: String? = null
    ): Resource<MessageConnection> {
        return executeQuery(
            queryName = "getMessagesInConversation(conversationId=$conversationId)",
            query = {
                apolloClient.query(
                    GetMessagesInConversationQuery(
                        conversationId = conversationId,
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after),
                        last = Optional.presentIfNotNull(last),
                        before = Optional.presentIfNotNull(before)
                    )
                ).execute()
            },
            transform = { data ->
                val messages = data.messagesInConversation.edges.mapNotNull { edge ->
                    edge.node.messagePreview.toMessage()
                }
                MessageConnection(
                    messages = messages,
                    hasNextPage = data.messagesInConversation.pageInfo.hasNextPage,
                    hasPreviousPage = data.messagesInConversation.pageInfo.hasPreviousPage,
                    startCursor = data.messagesInConversation.pageInfo.startCursor,
                    endCursor = data.messagesInConversation.pageInfo.endCursor,
                    totalCount = data.messagesInConversation.totalCount
                )
            }
        )
    }

    /**
     * Get messages with network-first strategy for fresh initial loads
     * Always fetches from network to ensure latest messages are shown
     */
    suspend fun getMessagesInConversationFresh(
        conversationId: Int,
        first: Int? = null,
        after: String? = null,
        last: Int? = null,
        before: String? = null
    ): Resource<MessageConnection> {
        return executeQuery(
            queryName = "getMessagesInConversationFresh(conversationId=$conversationId)",
            query = {
                apolloClient.query(
                    GetMessagesInConversationQuery(
                        conversationId = conversationId,
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(after),
                        last = Optional.presentIfNotNull(last),
                        before = Optional.presentIfNotNull(before)
                    )
                ).fetchPolicy(FetchPolicy.NetworkOnly)
                 .execute()
            },
            transform = { data ->
                val messages = data.messagesInConversation.edges.mapNotNull { edge ->
                    edge.node.messagePreview.toMessage()
                }
                MessageConnection(
                    messages = messages,
                    hasNextPage = data.messagesInConversation.pageInfo.hasNextPage,
                    hasPreviousPage = data.messagesInConversation.pageInfo.hasPreviousPage,
                    startCursor = data.messagesInConversation.pageInfo.startCursor,
                    endCursor = data.messagesInConversation.pageInfo.endCursor,
                    totalCount = data.messagesInConversation.totalCount
                )
            }
        )
    }

    /**
     * Send a message to a conversation
     */
    suspend fun sendMessage(
        conversationId: Int,
        content: String,
        imageUrl: String? = null
    ): Resource<Message> {
        return executeMutation(
            mutationName = "sendMessage",
            mutation = {
                apolloClient.mutation(
                    SendMessageMutation(
                        input = SendMessageInput(
                            conversationId = conversationId,
                            content = content,
                            imageUrl = Optional.presentIfNotNull(imageUrl)
                        )
                    )
                ).execute()
            },
            transform = { data ->
                data.sendMessage.messagePreview.toMessage()
            }
        )
    }

    /**
     * Edit an existing message
     */
    suspend fun editMessage(messageId: Int, content: String): Resource<Message> {
        return executeMutation(
            mutationName = "editMessage",
            mutation = {
                apolloClient.mutation(
                    EditMessageMutation(
                        input = EditMessageInput(
                            messageId = messageId,
                            content = content
                        )
                    )
                ).execute()
            },
            transform = { data ->
                data.editMessage.messagePreview.toMessage()
            }
        )
    }

    /**
     * Delete a message
     */
    suspend fun deleteMessage(messageId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "deleteMessage",
            mutation = {
                apolloClient.mutation(DeleteMessageMutation(messageId = messageId)).execute()
            },
            transform = { data -> data.deleteMessage }
        )
    }

    // ========== Reaction Operations ==========

    /**
     * Add a reaction to a message
     */
    suspend fun addReaction(messageId: Int, emoji: String): Resource<MessageReaction> {
        return executeMutation(
            mutationName = "addReaction",
            mutation = {
                apolloClient.mutation(
                    AddReactionMutation(
                        input = AddReactionInput(
                            messageId = messageId,
                            emoji = emoji
                        )
                    )
                ).execute()
            },
            transform = { data ->
                data.addReaction.messageReactionPreview.toReaction()
            }
        )
    }

    /**
     * Remove a reaction from a message
     */
    suspend fun removeReaction(messageId: Int, emoji: String): Resource<Boolean> {
        return executeMutation(
            mutationName = "removeReaction",
            mutation = {
                apolloClient.mutation(
                    RemoveReactionMutation(
                        input = RemoveReactionInput(
                            messageId = messageId,
                            emoji = emoji
                        )
                    )
                ).execute()
            },
            transform = { data -> data.removeReaction }
        )
    }

    // ========== Read Receipt Operations ==========

    /**
     * Mark a conversation as read
     */
    suspend fun markConversationAsRead(
        conversationId: Int,
        messageId: Int? = null
    ): Resource<ReadReceipt> {
        return executeMutation(
            mutationName = "markConversationAsRead",
            mutation = {
                apolloClient.mutation(
                    MarkConversationAsReadMutation(
                        input = MarkAsReadInput(
                            conversationId = conversationId,
                            messageId = Optional.presentIfNotNull(messageId)
                        )
                    )
                ).execute()
            },
            transform = { data ->
                ReadReceipt(
                    conversationId = data.markConversationAsRead.conversationId,
                    userId = data.markConversationAsRead.userId,
                    lastReadAt = data.markConversationAsRead.lastReadAt,
                    lastReadMessageId = data.markConversationAsRead.lastReadMessageId
                )
            }
        )
    }

    // ========== Subscription Operations ==========

    /**
     * Subscribe to new messages in a conversation.
     * Returns a Flow that emits new messages as they arrive.
     */
    fun subscribeToMessages(conversationId: Int): Flow<Message> {
        Logger.d(tag, "Subscribing to messages in conversation $conversationId")
        return apolloClient.subscription(MessageReceivedSubscription(conversationId = conversationId))
            .toFlow()
            .mapNotNull { response ->
                response.data?.messageReceived?.messagePreview?.toMessage()
            }
            .catch { error ->
                Logger.e(tag, "Subscription error for conversation $conversationId: ${error.message}")
                // Don't emit anything on error, just log it
            }
    }

    // ========== Unread Status Operations ==========

    /**
     * Check if the current user has any unread conversations
     */
    suspend fun hasAnyUnreadConversations(): Resource<Boolean> {
        return executeQuery(
            queryName = "hasAnyUnreadConversations",
            query = {
                apolloClient.query(GetHasAnyUnreadConversationsQuery()).execute()
            },
            transform = { data ->
                data.hasAnyUnreadConversations
            }
        )
    }

    /**
     * Subscribe to unread status changes across all user's conversations.
     * Returns a Flow that emits updates when new messages arrive.
     */
    fun subscribeToUnreadStatusChanges(): Flow<UnreadStatusUpdate> {
        Logger.d(tag, "Subscribing to unread status changes")
        return apolloClient.subscription(UnreadStatusChangedSubscription())
            .toFlow()
            .mapNotNull { response ->
                response.data?.unreadStatusChanged?.let {
                    UnreadStatusUpdate(
                        conversationId = it.conversationId,
                        hasUnread = it.hasUnread
                    )
                }
            }
            .catch { error ->
                Logger.e(tag, "Unread status subscription error: ${error.message}")
                // Don't emit anything on error, just log it
            }
    }

    // ========== Helper Extension Functions ==========

    private fun ConversationPreview.toConversation(): Conversation {
        return Conversation(
            id = id,
            name = name,
            conversationType = when (conversationType.name) {
                "DIRECT" -> ConversationType.DIRECT
                "GROUP" -> ConversationType.GROUP
                else -> ConversationType.DIRECT
            },
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
            participants = participants.mapNotNull { it.userPreview?.toUser() },
            latestMessage = latestMessage?.let { msg ->
                Message(
                    id = msg.id,
                    conversationId = id,
                    senderId = msg.senderId,
                    content = msg.content,
                    imageUrl = null, // Latest message preview doesn't include image URL
                    createdAt = msg.createdAt,
                    updatedAt = null,
                    isEdited = false,
                    sender = msg.sender?.userPreview?.toUser(),
                    reactions = emptyList()
                )
            },
            hasUnread = hasUnread
        )
    }

    private fun MessagePreview.toMessage(): Message {
        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            imageUrl = imageUrl,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isEdited = isEdited,
            sender = sender?.userPreview?.toUser(),
            reactions = reactions.mapNotNull { it.messageReactionPreview?.toReaction() }
        )
    }

    private fun MessageReactionPreview.toReaction(): MessageReaction {
        return MessageReaction(
            id = id,
            messageId = messageId,
            userId = userId,
            emoji = emoji,
            createdAt = createdAt,
            user = user?.userPreview?.toUser()
        )
    }

    private fun UserPreview.toUser(): User {
        return User(
            id = id,
            email = "",
            username = null,
            displayName = displayName,
            profileImageUrl = profileImageUrl
        )
    }
}
