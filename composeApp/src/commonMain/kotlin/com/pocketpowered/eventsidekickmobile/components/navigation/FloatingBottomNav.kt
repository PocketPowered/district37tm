package com.district37.toastmasters.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.navigation.BottomNavTab
import com.district37.toastmasters.navigation.bottomNavItems
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Floating bottom navigation with pill-shaped design.
 * Icon-only navigation with 3 tabs.
 *
 * Design specs:
 * - Rounded pill shape (28dp corner radius)
 * - Semi-transparent surface with 95% opacity
 * - Shadow elevation: 12dp
 * - Padding: 24dp horizontal, 16dp vertical margins
 * - 56dp touch targets for each tab
 * - Selected state: primaryContainer background with 16dp corner radius
 */
@Composable
fun FloatingBottomNav(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    isAuthenticated: Boolean,
    userAvatarUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = selectedTab == item.tab
                val enabled = item.tab !is BottomNavTab.Create || isAuthenticated

                FloatingNavItem(
                    selected = selected,
                    enabled = enabled,
                    item = item,
                    onClick = { if (enabled) onTabSelected(item.tab) },
                    avatarUrl = if (item.tab is BottomNavTab.Account) userAvatarUrl else null
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    selected: Boolean,
    enabled: Boolean,
    item: com.district37.toastmasters.navigation.BottomNavItem,
    onClick: () -> Unit,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    // Use onSurface for icons - this is light in dark mode, dark in light mode
    val iconColor = MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            Box(contentAlignment = Alignment.Center) {
                // Show avatar for Account tab when authenticated
                val isAccountTab = item.tab is BottomNavTab.Account
                if (isAccountTab && avatarUrl != null && avatarUrl.isNotBlank()) {
                    CoilImage(
                        imageModel = { avatarUrl },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                        failure = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        loading = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        tint = LocalContentColor.current.copy(alpha = if (enabled) 1f else 0.38f)
                    )
                }
            }
        }
    }
}
