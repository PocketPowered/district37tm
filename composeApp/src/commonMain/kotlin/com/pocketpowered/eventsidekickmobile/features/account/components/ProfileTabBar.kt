package com.district37.toastmasters.features.account.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Profile content tabs
 */
enum class ProfileTab(val title: String) {
    EVENTS("Events"),
    ACTIVITY("Activity"),
    REQUESTS("Requests")
}

/**
 * Tab bar for profile content navigation
 * Shows badge on Requests tab when there are pending requests
 */
@Composable
fun ProfileTabBar(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    requestsBadgeCount: Int,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = ProfileTab.entries.indexOf(selectedTab),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        ProfileTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    if (tab == ProfileTab.REQUESTS && requestsBadgeCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tab.title)
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge {
                                Text(requestsBadgeCount.toString())
                            }
                        }
                    } else {
                        Text(tab.title)
                    }
                }
            )
        }
    }
}
