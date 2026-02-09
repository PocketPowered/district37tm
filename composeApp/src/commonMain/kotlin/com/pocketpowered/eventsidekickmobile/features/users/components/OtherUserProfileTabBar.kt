package com.district37.toastmasters.features.users.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Profile content tabs for viewing another user's profile
 */
enum class OtherUserProfileTab(val title: String) {
    EVENTS("Events"),
    ACTIVITY("Activity")
}

/**
 * Tab bar for other user profile content navigation
 * Simplified version without badge (unlike own profile's Requests tab)
 */
@Composable
fun OtherUserProfileTabBar(
    selectedTab: OtherUserProfileTab,
    onTabSelected: (OtherUserProfileTab) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = OtherUserProfileTab.entries.indexOf(selectedTab),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        OtherUserProfileTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.title) }
            )
        }
    }
}
