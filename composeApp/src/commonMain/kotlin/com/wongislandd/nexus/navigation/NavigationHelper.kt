package com.wongislandd.nexus.navigation

import androidx.navigation.NavController

class NavigationHelper(private val navigationItemRegistry: NavigationItemRegistry) {

    fun navigate(
        navigationController: NavController,
        navigationKey: String,
        args: Map<String, Any?> = emptyMap(),
        removeSelfFromStack: Boolean = false
    ): Boolean {
        val navigationItem = navigationItemRegistry.getNavigationItem(navigationKey)
        if (navigationItem != null) {
            val route = navigationItem.reconstructRoute(args)
            if (removeSelfFromStack) {
                navigationController.popBackStack()
                navigationController.navigate(route) {
                    popUpTo(route) { inclusive = true }
                }
            } else {
                navigationController.navigate(navigationItem.reconstructRoute(args))
            }
            return true
        } else {
            return false
        }
    }
}