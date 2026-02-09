package com.district37.toastmasters.components.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.ActivityFeedItem
import com.district37.toastmasters.util.Resource

/**
 * Extension function to add activity feed items to a LazyColumn
 * This allows the activity feed to be rendered inline with other content
 */
fun LazyListScope.activityFeedSection(
    activityFeedState: Resource<List<ActivityFeedItem>>,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    sectionHeaderText: String = "Activity",
    emptyStateTitle: String = "No activity yet",
    emptyStateSubtitle: String = "Activity will appear here",
    errorMessage: String = "Could not load activity"
) {
    // Section header
    item(key = "activity_feed_header") {
        Text(
            text = sectionHeaderText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    when (activityFeedState) {
        is Resource.Loading -> {
            item(key = "activity_feed_loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        is Resource.Error -> {
            item(key = "activity_feed_error") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is Resource.Success -> {
            val items = activityFeedState.data
            if (items.isEmpty()) {
                item(key = "activity_feed_empty") {
                    ActivityFeedEmptyState(
                        title = emptyStateTitle,
                        subtitle = emptyStateSubtitle,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                }
            } else {
                items(
                    items = items,
                    key = { "activity_${it.id}" }
                ) { activityItem ->
                    ActivityFeedItemCard(
                        item = activityItem,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    // Trigger load more when near the end (if still loading more, we have more to load)
                    if (activityItem == items.lastOrNull() && !isLoadingMore) {
                        onLoadMore()
                    }
                }

                // Loading more indicator
                if (isLoadingMore) {
                    item(key = "activity_feed_loading_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        is Resource.NotLoading -> {
            item(key = "activity_feed_empty_not_loaded") {
                ActivityFeedEmptyState(
                    title = emptyStateTitle,
                    subtitle = emptyStateSubtitle,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                )
            }
        }
    }
}

/**
 * Empty state when no activity is available
 */
@Composable
internal fun ActivityFeedEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
