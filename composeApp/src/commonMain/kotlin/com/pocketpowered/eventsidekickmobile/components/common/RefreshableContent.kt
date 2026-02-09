package com.district37.toastmasters.components.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A reusable pull-to-refresh wrapper that handles refresh state internally.
 *
 * @param onRefresh Suspend function called when user triggers a refresh
 * @param modifier Modifier for the container
 * @param minRefreshDuration Minimum time to show the refresh indicator (for visual feedback)
 * @param content The content to display inside the refreshable container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableContent(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    minRefreshDuration: Long = 500L,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                onRefresh()
                delay(minRefreshDuration)
            }
        },
        modifier = modifier
    ) {
        content()
    }
}
