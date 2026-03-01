package com.district37.toastmasters.navigation

import EventIcon
import Menu
import androidx.compose.ui.graphics.vector.ImageVector
import com.district37.toastmasters.locations.Map

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
        icon = Map
    ),
    DrawerItem(
        key = NavigationItemKey.RESOURCES,
        title = "Resources",
        icon = Menu,
    ),
) 
