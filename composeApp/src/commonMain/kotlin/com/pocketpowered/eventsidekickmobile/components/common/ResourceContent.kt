package com.district37.toastmasters.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.util.Resource

/**
 * A composable that handles rendering different states of a [Resource].
 *
 * This eliminates the common pattern of:
 * ```kotlin
 * when (resourceState) {
 *     is Resource.Loading -> { /* loading UI */ }
 *     is Resource.Error -> { /* error UI */ }
 *     is Resource.Success -> { /* success UI with resourceState.data */ }
 *     is Resource.NotLoading -> { /* not loaded UI */ }
 * }
 * ```
 *
 * Instead, use:
 * ```kotlin
 * ResourceContent(
 *     resource = resourceState,
 *     onSuccess = { data -> /* success UI */ }
 * )
 * ```
 *
 * @param resource The resource state to render
 * @param modifier Modifier for the container
 * @param onLoading Optional custom loading composable. Defaults to centered CircularProgressIndicator.
 * @param onError Optional custom error composable. Defaults to centered error message text.
 * @param onNotLoading Optional custom not-loaded composable. Defaults to empty box.
 * @param onSuccess Composable to render when resource is in Success state
 */
@Composable
fun <T> ResourceContent(
    resource: Resource<T>,
    modifier: Modifier = Modifier,
    onLoading: @Composable () -> Unit = { DefaultLoadingContent() },
    onError: @Composable (message: String?) -> Unit = { message -> DefaultErrorContent(message) },
    onNotLoading: @Composable () -> Unit = {},
    onSuccess: @Composable (data: T) -> Unit
) {
    Box(modifier = modifier) {
        when (resource) {
            is Resource.Loading -> onLoading()
            is Resource.Error -> onError(resource.message)
            is Resource.NotLoading -> onNotLoading()
            is Resource.Success -> onSuccess(resource.data)
        }
    }
}

/**
 * A variant of ResourceContent that fills the available space.
 * Useful for full-screen loading/error states.
 */
@Composable
fun <T> ResourceContentFillMax(
    resource: Resource<T>,
    modifier: Modifier = Modifier,
    onLoading: @Composable () -> Unit = { DefaultLoadingContent(Modifier.fillMaxSize()) },
    onError: @Composable (message: String?) -> Unit = { message -> DefaultErrorContent(message, Modifier.fillMaxSize()) },
    onNotLoading: @Composable () -> Unit = {},
    onSuccess: @Composable (data: T) -> Unit
) {
    ResourceContent(
        resource = resource,
        modifier = modifier.fillMaxSize(),
        onLoading = onLoading,
        onError = onError,
        onNotLoading = onNotLoading,
        onSuccess = onSuccess
    )
}

/**
 * Default loading content - a centered circular progress indicator.
 */
@Composable
fun DefaultLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

/**
 * Default error content - a centered error message.
 */
@Composable
fun DefaultErrorContent(
    message: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Shimmer-based loading content for use with ResourceContent.
 * Provides a placeholder with shimmer animation.
 *
 * @param content The placeholder content to show with shimmer effect
 */
@Composable
fun ShimmerLoadingContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.shimmerEffect()) {
        content()
    }
}
