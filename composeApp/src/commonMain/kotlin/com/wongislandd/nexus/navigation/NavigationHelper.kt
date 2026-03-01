package com.wongislandd.nexus.navigation

import androidx.navigation.NavController

class NavigationHelper(private val navigationItemRegistry: NavigationItemRegistry) {

    fun navigate(
        navigationController: NavController,
        navigationKey: String,
        args: Map<String, Any?> = emptyMap(),
        removeSelfFromStack: Boolean = false,
        isTopLevelDestination: Boolean = false,
        topLevelHomeRoute: String? = null
    ): Boolean {
        val navigationItem = navigationItemRegistry.getNavigationItem(navigationKey)
        if (navigationItem != null) {
            val route = navigationItem.reconstructRoute(args)
            if (removeSelfFromStack) {
                navigationController.popBackStack()
                navigationController.navigate(route) {
                    popUpTo(route) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navigationController.navigate(route) {
                    launchSingleTop = true
                    if (isTopLevelDestination) {
                        val homeRoute = topLevelHomeRoute ?: route
                        popUpTo(homeRoute) {
                            saveState = true
                        }
                        restoreState = true
                    }
                }
            }
            return true
        } else {
            return false
        }
    }
}
