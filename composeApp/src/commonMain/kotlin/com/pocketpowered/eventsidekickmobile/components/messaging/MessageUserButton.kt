package com.district37.toastmasters.components.messaging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.data.repository.MessagingRepository
import com.district37.toastmasters.navigation.ChatRoute
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Reusable button component that starts or navigates to a direct conversation with a user.
 *
 * This component handles the complete flow of:
 * 1. Calling the getOrCreateDirectConversation mutation (which checks if a conversation
 *    exists between the current user and the target user, creating one if needed)
 * 2. Managing loading state during the async operation
 * 3. Navigating to the chat screen on success via direct NavController navigation
 * 4. Displaying error messages if the operation fails
 *
 * @param userId The ID of the user to start a conversation with
 * @param displayName The display name of the user (used for chat screen title)
 * @param avatarUrl The avatar URL of the user (used for chat screen header)
 * @param modifier Optional modifier for the button
 * @param messagingRepository Repository for messaging operations (injected via Koin)
 */
@Composable
fun MessageUserButton(
    userId: String,
    displayName: String?,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    messagingRepository: MessagingRepository = koinInject()
) {
    val navController = LocalNavController.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null

                    when (val result = messagingRepository.getOrCreateDirectConversation(userId)) {
                        is Resource.Success -> {
                            val conversation = result.data
                            navController.navigate(
                                ChatRoute(
                                    conversationId = conversation.id,
                                    displayName = displayName,
                                    avatarUrl = avatarUrl
                                )
                            )
                        }
                        is Resource.Error -> {
                            errorMessage = result.message ?: "Failed to start conversation"
                        }
                        else -> {}
                    }

                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Message")
        }

        // Show error message if present
        errorMessage?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
