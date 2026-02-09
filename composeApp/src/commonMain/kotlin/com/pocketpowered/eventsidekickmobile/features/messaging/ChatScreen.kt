package com.district37.toastmasters.features.messaging

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.models.Message
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.toImageBitmap
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

// iMessage-style blue color (used for sent messages - stays constant)
private val iMessageBlue = Color(0xFF007AFF)

// Only show timestamp headers when there's been this many minutes since the last message
private const val TIME_GAP_THRESHOLD_MINUTES = 10

@Composable
fun ChatScreen(
    conversationId: Int,
    initialDisplayName: String? = null,
    initialAvatarUrl: String? = null,
    viewModel: ChatViewModel = koinViewModel { parametersOf(conversationId) }
) {
    val conversationState by viewModel.conversation.collectAsState()
    val messagesState by viewModel.messages.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val editingMessageId by viewModel.editingMessageId.collectAsState()
    val hasOlderMessages by viewModel.hasOlderMessages.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val pendingImageBitmap by viewModel.pendingImageBitmap.collectAsState()
    val currentUserId = viewModel.currentUserId ?: ""
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val topBarInsets = LocalTopAppBarInsets.current

    // Image picker launcher using FileKit
    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { platformFile ->
        platformFile?.let { file ->
            scope.launch {
                // Read bytes using FileKit extension
                val bytes = file.readBytes()
                val bitmap = bytes.toImageBitmap()
                bitmap?.let {
                    viewModel.onImageSelected(bytes, it)
                }
            }
        }
    }

    // Get conversation details for header - use initial values until loaded
    val conversationName = when (val state = conversationState) {
        is Resource.Success -> state.data?.getDisplayName(currentUserId) ?: initialDisplayName ?: "Chat"
        else -> initialDisplayName ?: "Chat"
    }
    val avatarUrl = when (val state = conversationState) {
        is Resource.Success -> state.data?.getDisplayImageUrl(currentUserId) ?: initialAvatarUrl
        else -> initialAvatarUrl
    }

    ConfigureTopAppBar(
        config = AppBarConfigs.chatScreen(
            displayName = conversationName,
            avatarUrl = avatarUrl
        ),
        onBackClick = { navController.popBackStack() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = topBarInsets.recommendedContentPadding)
            .imePadding()
    ) {
        // Messages list
        Box(modifier = Modifier.weight(1f)) {
            when (val state = messagesState) {
                is Resource.NotLoading -> {
                    // do nothing
                }

                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = iMessageBlue)
                    }
                }

                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error loading messages",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = state.message ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red
                            )
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is Resource.Success -> {
                    val messages = state.data
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No messages yet\nSend the first message!",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Group messages for iMessage-style display
                        // Messages are pre-sorted by timestamp in ViewModel
                        val groupedMessages = remember(messages) {
                            groupMessagesForDisplay(messages, currentUserId)
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            reverseLayout = true
                        ) {
                            items(groupedMessages) { item ->
                                when (item) {
                                    is MessageDisplayItem.DateHeader -> {
                                        DateHeaderView(item.dateText)
                                    }

                                    is MessageDisplayItem.MessageItem -> {
                                        MessageBubble(
                                            message = item.message,
                                            isOwnMessage = item.isOwnMessage,
                                            isFirstInGroup = item.isFirstInGroup,
                                            isLastInGroup = item.isLastInGroup
                                        )
                                    }
                                }
                            }

                            if (hasOlderMessages) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLoadingMore) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = iMessageBlue
                                            )
                                        } else {
                                            Button(
                                                onClick = { viewModel.loadOlderMessages() },
                                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                    containerColor = iMessageBlue
                                                )
                                            ) {
                                                Text("Load Earlier Messages")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Scroll to bottom when new messages arrive
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(0)
                        }
                    }
                }
            }
        }

        // Message input - iMessage style
        MessageInputBar(
            messageText = messageText,
            onMessageTextChange = { viewModel.updateMessageText(it) },
            onSendClick = { viewModel.sendMessage() },
            isSending = isSending,
            isEditing = editingMessageId != null,
            onCancelEditing = { viewModel.cancelEditing() },
            pendingImageBitmap = pendingImageBitmap,
            onOpenGallery = { galleryLauncher.launch() },
            onRemovePendingImage = { viewModel.removePendingImage() }
        )
    }
}

@Composable
private fun DateHeaderView(dateText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean,
    isEditing: Boolean,
    onCancelEditing: () -> Unit,
    pendingImageBitmap: ImageBitmap?,
    onOpenGallery: () -> Unit,
    onRemovePendingImage: () -> Unit
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Determine if send button should show (text OR image present)
    val canSend = messageText.isNotBlank() || pendingImageBitmap != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceVariant)
            .navigationBarsPadding()
    ) {
        // Editing indicator
        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editing message",
                    style = MaterialTheme.typography.labelMedium,
                    color = iMessageBlue,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onCancelEditing,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel editing",
                        modifier = Modifier.size(16.dp),
                        tint = onSurfaceVariant
                    )
                }
            }
        }

        // Pending image preview
        if (pendingImageBitmap != null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .size(80.dp)
            ) {
                Image(
                    bitmap = pendingImageBitmap,
                    contentDescription = "Pending image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                // Remove button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { onRemovePendingImage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery button (don't show when editing)
            if (!isEditing) {
                IconButton(
                    onClick = onOpenGallery,
                    enabled = !isSending,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Add image",
                        tint = if (isSending) onSurfaceVariant.copy(alpha = 0.5f) else iMessageBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Text input field with iMessage styling
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (messageText.isEmpty()) {
                    Text(
                        text = "Message",
                        color = onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    textStyle = TextStyle(
                        color = onSurface,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(iMessageBlue),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Send button - iMessage style (show when text OR image present)
            if (canSend) {
                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isSending) iMessageBlue.copy(alpha = 0.6f) else iMessageBlue)
                        .clickable(enabled = !isSending) { onSendClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean
) {
    // Sent messages use iMessage blue, received messages use theme-aware surface color
    val receivedBubbleColor = MaterialTheme.colorScheme.surfaceVariant
    val bubbleColor = if (isOwnMessage) iMessageBlue else receivedBubbleColor
    val textColor = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    // Determine corner radii for bubble tail effect
    val cornerRadius = 18.dp
    val smallCorner = 4.dp

    val shape = if (isOwnMessage) {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = if (isFirstInGroup) cornerRadius else smallCorner,
            bottomStart = cornerRadius,
            bottomEnd = if (isLastInGroup) cornerRadius else smallCorner
        )
    } else {
        RoundedCornerShape(
            topStart = if (isFirstInGroup) cornerRadius else smallCorner,
            topEnd = cornerRadius,
            bottomStart = if (isLastInGroup) cornerRadius else smallCorner,
            bottomEnd = cornerRadius
        )
    }

    // Spacing between messages
    val topPadding = if (isFirstInGroup) 8.dp else 2.dp

    val hasImage = !message.imageUrl.isNullOrBlank()
    val hasText = message.content.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(
                    start = if (isOwnMessage) 60.dp else 0.dp,
                    end = if (isOwnMessage) 0.dp else 60.dp
                )
        ) {
            // Display image if present
            if (hasImage) {
                CoilImage(
                    imageModel = { message.imageUrl },
                    modifier = Modifier
                        .widthIn(max = 200.dp)
                        .heightIn(max = 300.dp)
                        .clip(shape),
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Fit
                    )
                )

                // Add spacing between image and text if both present
                if (hasText) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Display text content if present
            if (hasText) {
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(bubbleColor)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Show reactions if any
        if (message.reactions.isNotEmpty()) {
            val reactionBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(
                    start = if (!isOwnMessage) 8.dp else 0.dp,
                    end = if (isOwnMessage) 8.dp else 0.dp,
                    top = 2.dp
                )
            ) {
                val groupedReactions = message.reactions.groupBy { it.emoji }
                groupedReactions.forEach { (emoji, reactions) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(reactionBackgroundColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (reactions.size > 1) "$emoji ${reactions.size}" else emoji,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// Data class for grouped message display
private sealed class MessageDisplayItem {
    data class DateHeader(val dateText: String) : MessageDisplayItem()
    data class MessageItem(
        val message: Message,
        val isOwnMessage: Boolean,
        val isFirstInGroup: Boolean,
        val isLastInGroup: Boolean
    ) : MessageDisplayItem()
}

// Group messages by sender and time gap for iMessage-style display
@OptIn(ExperimentalTime::class)
private fun groupMessagesForDisplay(
    messages: List<Message>,
    currentUserId: String
): List<MessageDisplayItem> {
    if (messages.isEmpty()) return emptyList()

    val result = mutableListOf<MessageDisplayItem>()
    var lastTimestamp: Instant? = null
    var lastSenderId: String? = null

    messages.forEachIndexed { index, message ->
        val isOwnMessage = message.senderId == currentUserId
        val currentTimestamp = message.createdAt

        // Determine if we should show a timestamp header
        val shouldShowTimestamp = shouldShowTimestampHeader(lastTimestamp, currentTimestamp)

        if (shouldShowTimestamp && currentTimestamp != null) {
            result.add(MessageDisplayItem.DateHeader(formatDateHeader(currentTimestamp)))
            lastSenderId = null // Reset grouping on timestamp change
        }

        // Determine if this is first/last in a group of consecutive messages from same sender
        val isFirstInGroup = lastSenderId != message.senderId
        val nextMessage = messages.getOrNull(index + 1)
        val isLastInGroup = nextMessage == null ||
                nextMessage.senderId != message.senderId ||
                shouldShowTimestampHeader(currentTimestamp, nextMessage.createdAt)

        result.add(
            MessageDisplayItem.MessageItem(
                message = message,
                isOwnMessage = isOwnMessage,
                isFirstInGroup = isFirstInGroup,
                isLastInGroup = isLastInGroup
            )
        )

        lastTimestamp = currentTimestamp
        lastSenderId = message.senderId
    }

    return result.reversed() // Reverse for reverse layout
}

// Determine if a timestamp header should be shown between two messages
@OptIn(ExperimentalTime::class)
private fun shouldShowTimestampHeader(previousTimestamp: Instant?, currentTimestamp: Instant?): Boolean {
    // Show timestamp for first message
    if (previousTimestamp == null) return true
    if (currentTimestamp == null) return false

    val previousLocal = previousTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val currentLocal = currentTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())

    // Show timestamp if date changed
    if (previousLocal.date != currentLocal.date) return true

    // Show timestamp if time gap exceeds threshold
    val gapSeconds = currentTimestamp.epochSeconds - previousTimestamp.epochSeconds
    return gapSeconds >= TIME_GAP_THRESHOLD_MINUTES * 60
}

@OptIn(ExperimentalTime::class)
private fun formatDateHeader(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val now = Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
        .toLocalDateTime(TimeZone.currentSystemDefault())

    return when {
        localDateTime.date == now.date -> {
            // Today - show time
            val hour = localDateTime.hour
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (hour < 12) "AM" else "PM"
            val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            "Today $hour12:$minute $amPm"
        }

        localDateTime.date.toEpochDays() == now.date.toEpochDays() - 1 -> {
            "Yesterday"
        }

        else -> {
            // Show date
            val month =
                localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            val day = localDateTime.dayOfMonth
            "$month $day"
        }
    }
}
