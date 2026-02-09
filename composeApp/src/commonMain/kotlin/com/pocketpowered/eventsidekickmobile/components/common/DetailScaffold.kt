package com.district37.toastmasters.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalBottomNavInsets
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.util.Resource

/**
 * Reusable content component for detail screens with built-in resource state handling
 *
 * This component provides a unified structure for detail pages including:
 * - TopAppBar configuration with back button (via root Scaffold)
 * - Pull-to-refresh functionality
 * - Automatic handling of Loading, Error, and Success states
 * - Primary content section for the main data display
 * - Additional content section for related items (schedules, events, etc.)
 *
 * @param T The type of data being displayed
 * @param resourceState The Resource state containing the data or loading/error state
 * @param onBackClick Callback when the back button is clicked
 * @param onRetry Callback when the retry button is clicked (shown on error) or pull-to-refresh is triggered
 * @param errorMessage Default error message to display if Resource.Error message is null
 * @param actions Optional composable for TopAppBar actions (edit, delete buttons, etc.)
 * @param primaryContent Composable that displays the main data (receives the success data)
 * @param additionalContent Optional LazyListScope receiver for adding more items below the primary content
 */
@Composable
fun <T> DetailScaffold(
    resourceState: Resource<T>,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    errorMessage: String = "Failed to load data",
    actions: (@Composable RowScope.() -> Unit)? = null,
    primaryContent: @Composable (T) -> Unit,
    additionalContent: LazyListScope.(T) -> Unit = {}
) {
    // Configure the root TopAppBar for detail screens with scroll-collapsing behavior
    ConfigureTopAppBar(
        config = AppBarConfigs.detailScreen(actions = actions),
        onBackClick = onBackClick
    )

    val topBarInsets = LocalTopAppBarInsets.current
    val bottomNavInsets = LocalBottomNavInsets.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        RefreshableContent(
            onRefresh = { onRetry() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    top = topBarInsets.recommendedContentPadding,
                    bottom = bottomNavInsets.recommendedContentPadding
                )
            ) {
                // Primary content with resource state handling
                item {
                    when (val state = resourceState) {
                        is Resource.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is Resource.Error -> {
                            Box(modifier = Modifier.padding(16.dp)) {
                                ErrorCard(
                                    message = state.message ?: errorMessage,
                                    onRetry = onRetry
                                )
                            }
                        }

                        is Resource.Success -> {
                            primaryContent(state.data)
                        }

                        else -> {}
                    }
                }

                // Additional content only shown on success
                if (resourceState is Resource.Success) {
                    additionalContent.invoke(this, resourceState.data)
                }
            }
        }
    }
}
