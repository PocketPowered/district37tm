package com.district37.toastmasters.features.messaging

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.data.repository.MessagingRepository
import com.district37.toastmasters.messaging.UnreadMessagesManager
import com.district37.toastmasters.models.Conversation
import com.district37.toastmasters.models.Message
import com.district37.toastmasters.util.AppConstants
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.LoggingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for a single chat/conversation screen.
 *
 * Features:
 * - Real-time message updates via WebSocket subscription
 * - Debounced mark-as-read to batch multiple rapid reads
 * - Image upload with retry support
 * - Message editing and deletion
 * - Reaction management
 */
@OptIn(FlowPreview::class)
class ChatViewModel(
    private val messagingRepository: MessagingRepository,
    private val authRepository: AuthRepository,
    private val imageUploadRepository: ImageUploadRepository,
    private val unreadMessagesManager: UnreadMessagesManager,
    private val conversationId: Int
) : LoggingViewModel() {

    private val _conversation = MutableStateFlow<Resource<Conversation?>>(Resource.Loading)
    val conversation: StateFlow<Resource<Conversation?>> = _conversation.asStateFlow()

    private val _messages = MutableStateFlow<Resource<List<Message>>>(Resource.Loading)
    val messages: StateFlow<Resource<List<Message>>> = _messages.asStateFlow()

    private val _hasOlderMessages = MutableStateFlow(false)
    val hasOlderMessages: StateFlow<Boolean> = _hasOlderMessages.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _editingMessageId = MutableStateFlow<Int?>(null)
    val editingMessageId: StateFlow<Int?> = _editingMessageId.asStateFlow()

    // Pending image state for image messages
    private val _pendingImageBytes = MutableStateFlow<ByteArray?>(null)
    val pendingImageBytes: StateFlow<ByteArray?> = _pendingImageBytes.asStateFlow()

    private val _pendingImageBitmap = MutableStateFlow<ImageBitmap?>(null)
    val pendingImageBitmap: StateFlow<ImageBitmap?> = _pendingImageBitmap.asStateFlow()

    private var _startCursor: String? = null
    private var _endCursor: String? = null

    // Subscription job for real-time message updates
    private var subscriptionJob: Job? = null

    // Debounced mark-as-read to batch multiple rapid message reads
    private val markAsReadTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val currentUserId: String?
        get() = (authRepository.authState.value as? AuthState.Authenticated)?.user?.id

    init {
        loadConversation()
        loadMessages()
        startMessageSubscription()
        setupDebouncedMarkAsRead()
    }

    /**
     * Set up debounced mark-as-read handling.
     * This batches multiple mark-as-read triggers within the debounce window
     * into a single API call, reducing network overhead.
     */
    private fun setupDebouncedMarkAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            markAsReadTrigger
                .debounce(AppConstants.Debounce.MARK_AS_READ)
                .collect {
                    performMarkAsRead()
                }
        }
    }

    /**
     * Start listening for new messages via WebSocket subscription.
     * Messages received will be automatically added to the messages list.
     */
    private fun startMessageSubscription() {
        subscriptionJob?.cancel()
        subscriptionJob = viewModelScope.launch(Dispatchers.IO) {
            Logger.d("ChatViewModel", "Starting message subscription for conversation $conversationId")
            messagingRepository.subscribeToMessages(conversationId)
                .catch { error ->
                    Logger.e("ChatViewModel", "Message subscription error: ${error.message}")
                    // Continue without real-time updates - manual refresh still works
                }
                .collect { newMessage ->
                    Logger.d("ChatViewModel", "Received new message via subscription: ${newMessage.id}")
                    val currentMessages = (_messages.value as? Resource.Success)?.data ?: emptyList()

                    // Only add if not already present (avoid duplicates from own sends)
                    if (currentMessages.none { it.id == newMessage.id }) {
                        val updated = (currentMessages + newMessage).sortedAndDeduped()
                        _messages.update { Resource.Success(updated) }

                        // Auto-mark as read if message is from another user
                        if (newMessage.senderId != currentUserId) {
                            markAsRead()
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscriptionJob?.cancel()
        Logger.d("ChatViewModel", "Cancelled message subscription for conversation $conversationId")
    }

    private fun loadConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = messagingRepository.getConversation(conversationId)
            _conversation.update { result }
        }
    }

    fun loadMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            _messages.update { Resource.Loading }

            // Always fetch from network to ensure latest messages are shown
            // Use 'first' to get the N newest messages (server orders by created_at DESC)
            val result = messagingRepository.getMessagesInConversationFresh(
                conversationId = conversationId,
                first = AppConstants.Pagination.MESSAGES_PAGE_SIZE
            )
            _messages.update { result.map { it.messages.sortedAndDeduped() } }

            if (result is Resource.Success) {
                // hasNextPage indicates there are more (older) messages to load
                _hasOlderMessages.update { result.data.hasNextPage }
                _startCursor = result.data.startCursor
                _endCursor = result.data.endCursor
            }

            // Mark conversation as read when loading messages
            markAsRead()
        }
    }

    fun loadOlderMessages() {
        // Use endCursor to paginate to older messages (server orders by created_at DESC)
        val cursor = _endCursor ?: return
        if (_isLoadingMore.value || !_hasOlderMessages.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingMore.update { true }
            val result = messagingRepository.getMessagesInConversation(
                conversationId = conversationId,
                first = AppConstants.Pagination.MESSAGES_PAGE_SIZE,
                after = cursor
            )

            if (result is Resource.Success) {
                val currentMessages = (_messages.value as? Resource.Success)?.data ?: emptyList()
                // Merge and sort by timestamp - deduplication handles any overlap
                val merged = (currentMessages + result.data.messages).sortedAndDeduped()
                _messages.update { Resource.Success(merged) }
                _hasOlderMessages.update { result.data.hasNextPage }
                _endCursor = result.data.endCursor
            }
            _isLoadingMore.update { false }
        }
    }

    fun updateMessageText(text: String) {
        _messageText.update { text }
    }

    /**
     * Called when user selects an image from the gallery
     */
    fun onImageSelected(bytes: ByteArray, bitmap: ImageBitmap) {
        _pendingImageBytes.update { bytes }
        _pendingImageBitmap.update { bitmap }
    }

    /**
     * Remove the pending image (user cancels image attachment)
     */
    fun removePendingImage() {
        _pendingImageBytes.update { null }
        _pendingImageBitmap.update { null }
    }

    fun sendMessage() {
        val content = _messageText.value.trim()
        val imageBytes = _pendingImageBytes.value

        // Allow send if there's text OR image (or both)
        if (content.isEmpty() && imageBytes == null) return
        if (_isSending.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isSending.update { true }

            val editingId = _editingMessageId.value

            // If editing, don't support image changes for now
            if (editingId != null) {
                val result = messagingRepository.editMessage(editingId, content)
                if (result is Resource.Success) {
                    _messageText.update { "" }
                    _editingMessageId.update { null }
                    val currentMessages = (_messages.value as? Resource.Success)?.data ?: emptyList()
                    _messages.update {
                        Resource.Success(currentMessages.map { msg ->
                            if (msg.id == editingId) result.data else msg
                        })
                    }
                }
                _isSending.update { false }
                return@launch
            }

            // Upload image first if present (with retry for reliability)
            var imageUrl: String? = null
            if (imageBytes != null) {
                val uploadResult = imageUploadRepository.uploadImageWithRetry(
                    imageBytes = imageBytes,
                    entityType = "message",
                    entityId = null,
                    filename = "message_image_${kotlin.time.Clock.System.now().toEpochMilliseconds()}.jpg",
                    contentType = "image/jpeg"
                )

                when (uploadResult) {
                    is Resource.Success -> {
                        imageUrl = uploadResult.data
                        Logger.d("ChatViewModel", "Image uploaded successfully: $imageUrl")
                    }
                    is Resource.Error -> {
                        Logger.e("ChatViewModel", "Image upload failed: ${uploadResult.message}")
                        _isSending.update { false }
                        return@launch
                    }
                    else -> {
                        _isSending.update { false }
                        return@launch
                    }
                }
            }

            // Send message with optional image URL
            val result = messagingRepository.sendMessage(
                conversationId = conversationId,
                content = content,
                imageUrl = imageUrl
            )

            if (result is Resource.Success) {
                _messageText.update { "" }
                _pendingImageBytes.update { null }
                _pendingImageBitmap.update { null }

                // Add new message and sort by timestamp
                val currentMessages = (_messages.value as? Resource.Success)?.data ?: emptyList()
                val updated = (currentMessages + result.data).sortedAndDeduped()
                _messages.update { Resource.Success(updated) }
            }
            _isSending.update { false }
        }
    }

    fun startEditingMessage(message: Message) {
        if (message.senderId != currentUserId) return
        _editingMessageId.update { message.id }
        _messageText.update { message.content }
    }

    fun cancelEditing() {
        _editingMessageId.update { null }
        _messageText.update { "" }
    }

    fun deleteMessage(messageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = messagingRepository.deleteMessage(messageId)
            if (result is Resource.Success && result.data) {
                val currentMessages = (_messages.value as? Resource.Success)?.data ?: emptyList()
                _messages.update {
                    Resource.Success(currentMessages.filter { it.id != messageId })
                }
            }
        }
    }

    fun addReaction(messageId: Int, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = messagingRepository.addReaction(messageId, emoji)
            if (result is Resource.Success) {
                // Update the message with the new reaction
                val currentMessages = (_messages.value as? Resource.Success)?.data ?: emptyList()
                _messages.update {
                    Resource.Success(currentMessages.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(reactions = msg.reactions + result.data)
                        } else msg
                    })
                }
            }
        }
    }

    fun removeReaction(messageId: Int, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = messagingRepository.removeReaction(messageId, emoji)
            if (result is Resource.Success && result.data) {
                val userId = currentUserId ?: return@launch
                val currentMessages = (_messages.value as? Resource.Success)?.data ?: emptyList()
                _messages.update {
                    Resource.Success(currentMessages.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(reactions = msg.reactions.filter {
                                !(it.emoji == emoji && it.userId == userId)
                            })
                        } else msg
                    })
                }
            }
        }
    }

    /**
     * Trigger a debounced mark-as-read operation.
     * Multiple calls within the debounce window will be batched.
     */
    private fun markAsRead() {
        markAsReadTrigger.tryEmit(Unit)
    }

    /**
     * Perform the actual mark-as-read API call.
     * Called by the debounced flow collector.
     */
    private suspend fun performMarkAsRead() {
        // Get the latest message ID if available
        val latestMessageId = (_messages.value as? Resource.Success)?.data?.lastOrNull()?.id
        messagingRepository.markConversationAsRead(conversationId, latestMessageId)
        // Update global unread state
        unreadMessagesManager.markConversationAsRead(conversationId)
    }

    fun refresh() {
        loadConversation()
        loadMessages()
    }

    /**
     * Sort messages chronologically and remove duplicates.
     * This ensures consistent ordering regardless of how messages are added.
     */
    private fun List<Message>.sortedAndDeduped(): List<Message> {
        return this.distinctBy { it.id }.sortedBy { it.createdAt }
    }
}
