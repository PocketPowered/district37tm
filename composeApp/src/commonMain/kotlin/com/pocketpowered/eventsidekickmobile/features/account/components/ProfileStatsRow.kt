package com.district37.toastmasters.features.account.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.shimmerEffect

/**
 * Profile stats row displaying key counts (Subscribed, Attending, Friends)
 * Twitter/LinkedIn style horizontal layout
 *
 * @param isLoading When true, shows shimmer placeholders instead of counts
 */
@Composable
fun ProfileStatsRow(
    subscribedCount: Int,
    attendingCount: Int,
    friendsCount: Int,
    isLoading: Boolean = false,
    onSubscribedClick: (() -> Unit)? = null,
    onAttendingClick: (() -> Unit)? = null,
    onFriendsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(count = subscribedCount, label = "Subscribed", isLoading = isLoading, onClick = onSubscribedClick)
        VerticalDivider(
            modifier = Modifier.height(32.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        StatItem(count = attendingCount, label = "Attending", isLoading = isLoading, onClick = onAttendingClick)
        VerticalDivider(
            modifier = Modifier.height(32.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        StatItem(count = friendsCount, label = "Friends", isLoading = isLoading, onClick = onFriendsClick)
    }
}

/**
 * Individual stat item with count and label
 *
 * @param isLoading When true, shows a shimmer placeholder instead of the count
 */
@Composable
private fun StatItem(
    count: Int,
    label: String,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickModifier = if (onClick != null && !isLoading) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Column(
        modifier = modifier.then(clickModifier).padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            // Shimmer placeholder for count - sized to match a single digit number
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        } else {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
