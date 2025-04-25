package com.district37.toastmasters.navigation

import EventIcon
import Menu
import Rocket
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerItem(
    val key: NavigationItemKey,
    val title: String,
    val icon: ImageVector,
)

val drawerItems = listOf(
    DrawerItem(
        key = NavigationItemKey.EVENT_LIST,
        title = "Agenda",
        icon = EventIcon,
    ),
    DrawerItem(
        key = NavigationItemKey.MAPS,
        title = "Maps",
        icon = Icons.Default.MailOutline
    ),
    DrawerItem(
        key = NavigationItemKey.NOTIFICATIONS,
        title = "Notifications",
        icon = Icons.Default.Notifications,
    ),
    DrawerItem(
        key = NavigationItemKey.RESOURCES,
        title = "Resources",
        icon = Menu,
    ),
    DrawerItem(
        key = NavigationItemKey.FIRST_TIMER_RESOURCES,
        title = "First Timers",
        icon = Rocket
    ),
) 