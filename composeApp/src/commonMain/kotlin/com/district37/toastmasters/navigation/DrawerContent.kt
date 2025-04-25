package com.district37.toastmasters.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.notifications.NotificationBadge
import com.wongislandd.nexus.navigation.LocalNavHostController

@Composable
fun DrawerContent(
    onItemClick: (NavigationItemKey) -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "District 37 Toastmasters",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.primary
            )
        }

        Divider()

        // Drawer Items
        drawerItems.forEach { item ->
            if (item.key == NavigationItemKey.NOTIFICATIONS) {
                NotificationEntryDrawerItem(
                    item = item,
                    isSelected = false
                )
            } else {
                DrawerItem(
                    item = item,
                    isSelected = false,
                    onItemClick = {
                        onItemClick(item.key)
                        onCloseDrawer()
                    }
                )
            }
        }
    }
}

@Composable
private fun DrawerItemContent(item: DrawerItem, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.body1,
            color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationEntryDrawerItem(
    item: DrawerItem,
    isSelected: Boolean
) {
    val appViewModel = LocalAppViewModel.current
    val navController = LocalNavHostController.current

    val backgroundColor = if (isSelected) {
        MaterialTheme.colors.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colors.surface
    }
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            color = backgroundColor,
            onClick = {
                appViewModel.navigate(
                    navController,
                    NavigationItemKey.NOTIFICATIONS
                )
            }
        ) {
            DrawerItemContent(item, isSelected)
            NotificationBadge()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DrawerItem(
    item: DrawerItem,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colors.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colors.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = backgroundColor,
        onClick = onItemClick
    ) {
        DrawerItemContent(item, isSelected)
    }
}