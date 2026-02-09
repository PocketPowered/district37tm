package com.district37.toastmasters.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlin.reflect.KClass

/**
 * Represents which tab context the user is currently in.
 * Used to determine bottom nav highlighting and "tap same tab to reset" behavior.
 */
enum class TabContext {
    EXPLORE,
    CREATE,
    ACCOUNT
}

/**
 * Root destinations for each tab.
 * These are used to determine the current tab context and to reset navigation.
 */
val TAB_ROOT_ROUTES: Map<TabContext, KClass<*>> = mapOf(
    TabContext.EXPLORE to ExploreRoute::class,
    TabContext.CREATE to CreateHubRoute::class,
    TabContext.ACCOUNT to AccountHomeRoute::class
)

/**
 * Determines the current tab context by finding the most recent
 * root destination in the back stack.
 *
 * Walks backwards through the back stack and returns the TabContext
 * corresponding to the first root route found.
 *
 * @return The current tab context, defaulting to EXPLORE if none found
 */
fun NavController.currentTabContext(): TabContext {
    // Walk back stack to find most recent root route
    for (entry in currentBackStack.value.reversed()) {
        for ((tabContext, routeClass) in TAB_ROOT_ROUTES) {
            if (entry.destination.hasRoute(routeClass)) {
                return tabContext
            }
        }
    }
    return TabContext.EXPLORE // Default
}

/**
 * Resets navigation to the root of the specified tab.
 * Pops the back stack to the tab's root destination, keeping it on the stack.
 *
 * @param tab The tab to reset to its root
 */
fun NavController.resetToTabRoot(tab: TabContext) {
    val rootRouteClass = TAB_ROOT_ROUTES[tab] ?: return

    // Pop back to the root route, keeping it on the stack
    popBackStack(
        route = rootRouteClass,
        inclusive = false
    )
}
