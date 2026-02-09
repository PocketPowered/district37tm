package com.district37.toastmasters.components.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Represents the sync state of a calendar item.
 */
enum class CalendarSyncState {
    /** Item not synced to calendar */
    NOT_SYNCED,
    /** Calendar matches server, all good */
    SYNCED,
    /** Sync operation in progress */
    PENDING,
    /** Local calendar is out of date */
    NEEDS_UPDATE,
    /** Sync failed, needs attention */
    ERROR
}

/**
 * A small indicator showing the sync status of a calendar item.
 *
 * @param state The current sync state
 * @param size The size of the indicator icon
 * @param modifier Modifier for the container
 */
@Composable
fun CalendarSyncStatusIndicator(
    state: CalendarSyncState,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    val syncedColor = Color(0xFF4CAF50) // Green
    val pendingColor = Color(0xFFFFC107) // Amber
    val needsUpdateColor = Color(0xFFFF9800) // Orange
    val errorColor = Color(0xFFF44336) // Red

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            CalendarSyncState.NOT_SYNCED -> {
                // No indicator for not synced
            }
            CalendarSyncState.SYNCED -> {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Synced to calendar",
                    tint = syncedColor,
                    modifier = Modifier.size(size)
                )
            }
            CalendarSyncState.PENDING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(size),
                    strokeWidth = 2.dp,
                    color = pendingColor
                )
            }
            CalendarSyncState.NEEDS_UPDATE -> {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Calendar needs update",
                    tint = needsUpdateColor,
                    modifier = Modifier.size(size)
                )
            }
            CalendarSyncState.ERROR -> {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Calendar sync error",
                    tint = errorColor,
                    modifier = Modifier.size(size)
                )
            }
        }
    }
}

/**
 * A compact version of the sync indicator for use in list items or cards.
 * Shows a small dot or badge-style indicator.
 *
 * @param state The current sync state
 * @param modifier Modifier for the indicator
 */
@Composable
fun CalendarSyncBadge(
    state: CalendarSyncState,
    modifier: Modifier = Modifier
) {
    CalendarSyncStatusIndicator(
        state = state,
        size = 12.dp,
        modifier = modifier
    )
}
