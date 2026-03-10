package com.district37.toastmasters.navigation

import EventIcon
import Menu
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.district37.toastmasters.locations.Map
import androidx.compose.material.icons.automirrored.outlined.List

data class DrawerItem(
    val key: NavigationItemKey,
    val title: String,
    val icon: ImageVector,
)

val drawerItems = listOf(
    DrawerItem(
        key = NavigationItemKey.EVENT_LIST,
        title = "Event List",
        icon = Icons.AutoMirrored.Outlined.List,
    ),
    DrawerItem(
        key = NavigationItemKey.EVENT_CALENDAR,
        title = "Schedule",
        icon = EventIcon,
    ),
    DrawerItem(
        key = NavigationItemKey.MAPS,
        title = "Maps",
        icon = Map
    ),
    DrawerItem(
        key = NavigationItemKey.RESOURCES,
        title = "Resources",
        icon = Menu,
    ),
) 
