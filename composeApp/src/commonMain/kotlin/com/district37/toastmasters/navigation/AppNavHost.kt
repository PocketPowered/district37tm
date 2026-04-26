package com.district37.toastmasters.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.district37.toastmasters.devsettings.DevSettingsScreen
import com.district37.toastmasters.eventdetails.EventDetailsScreen
import com.district37.toastmasters.eventlist.EventCalendarScreen
import com.district37.toastmasters.eventlist.EventListScreen
import com.district37.toastmasters.locations.LocationEventsScreen
import com.district37.toastmasters.locations.LocationsScreen
import com.district37.toastmasters.notifications.NotificationOnboardingScreen
import com.district37.toastmasters.notifications.NotificationsScreen
import com.district37.toastmasters.resources.ResourcesScreen
import com.district37.toastmasters.splash.SplashScreen
import com.wongislandd.nexus.navigation.LocalNavHostController

private const val STARTUP_GRAPH_ROUTE = "startup-graph"
private const val MAIN_GRAPH_ROUTE = "main-graph"

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: NavigationItemKey = NavigationItemKey.SPLASH_SCREEN
) {
    val navController = LocalNavHostController.current
    requireNotNull(supportedNavigationItems[startDestination]) {
        "Couldn't find registered start destination!"
    }
    val pageTurnEnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(280)
    )

    val pageTurnExitTransition = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(280)
    )

    val pageReturnEnterTransition = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(280)
    )

    val pageReturnExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(280)
    )

    val topLevelEnterTransition = fadeIn(animationSpec = tween(220))
    val topLevelExitTransition = fadeOut(animationSpec = tween(220))

    val splashRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.SPLASH_SCREEN]
    ).completeRoute
    val onboardingRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.NOTIFICATION_ONBOARDING]
    ).completeRoute
    val eventListRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.EVENT_LIST]
    ).completeRoute
    val eventCalendarRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.EVENT_CALENDAR]
    ).completeRoute
    val eventDetailsRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.EVENT_DETAILS]
    ).completeRoute
    val notificationsRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.NOTIFICATIONS]
    ).completeRoute
    val resourcesRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.RESOURCES]
    ).completeRoute
    val mapsRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.MAPS]
    ).completeRoute
    val devSettingsRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.DEV_SETTINGS]
    ).completeRoute
    val locationEventsRoute = requireNotNull(
        supportedNavigationItems[NavigationItemKey.LOCATION_EVENTS]
    ).completeRoute

    val startupStartDestination = if (startDestination == NavigationItemKey.NOTIFICATION_ONBOARDING) {
        onboardingRoute
    } else {
        splashRoute
    }
    val navStartDestination = when (startDestination) {
        NavigationItemKey.SPLASH_SCREEN,
        NavigationItemKey.NOTIFICATION_ONBOARDING -> STARTUP_GRAPH_ROUTE

        else -> MAIN_GRAPH_ROUTE
    }

    NavHost(
        navController = navController,
        startDestination = navStartDestination,
        enterTransition = {
            pageTurnEnterTransition
        },
        exitTransition = {
            pageTurnExitTransition
        },
        popEnterTransition = {
            pageReturnEnterTransition
        },
        popExitTransition = {
            pageReturnExitTransition
        },
        modifier = modifier
    ) {
        navigation(
            route = STARTUP_GRAPH_ROUTE,
            startDestination = startupStartDestination
        ) {
            composable(route = splashRoute) {
                SplashScreen()
            }

            composable(route = onboardingRoute) {
                NotificationOnboardingScreen()
            }
        }

        navigation(
            route = MAIN_GRAPH_ROUTE,
            startDestination = eventListRoute
        ) {
            composable(
                route = eventListRoute,
                enterTransition = { topLevelEnterTransition },
                exitTransition = { topLevelExitTransition },
                popEnterTransition = { topLevelEnterTransition },
                popExitTransition = { topLevelExitTransition }
            ) {
                EventListScreen()
            }

            composable(
                route = eventCalendarRoute,
                enterTransition = { topLevelEnterTransition },
                exitTransition = { topLevelExitTransition },
                popEnterTransition = { topLevelEnterTransition },
                popExitTransition = { topLevelExitTransition }
            ) {
                EventCalendarScreen()
            }

            composable(
                route = eventDetailsRoute,
                arguments = listOf(
                    navArgument(EVENT_ID_ARG) { type = NavType.IntType }
                )
            ) {
                val eventId = it.arguments?.getInt(EVENT_ID_ARG)
                    ?: throw IllegalArgumentException("Navigated to details screen without an event id!")
                EventDetailsScreen(eventId)
            }

            composable(
                route = notificationsRoute,
                enterTransition = { topLevelEnterTransition },
                exitTransition = { topLevelExitTransition },
                popEnterTransition = { topLevelEnterTransition },
                popExitTransition = { topLevelExitTransition }
            ) {
                NotificationsScreen()
            }

            composable(
                route = resourcesRoute,
                enterTransition = { topLevelEnterTransition },
                exitTransition = { topLevelExitTransition },
                popEnterTransition = { topLevelEnterTransition },
                popExitTransition = { topLevelExitTransition }
            ) {
                ResourcesScreen()
            }

            composable(
                route = mapsRoute,
                enterTransition = { topLevelEnterTransition },
                exitTransition = { topLevelExitTransition },
                popEnterTransition = { topLevelEnterTransition },
                popExitTransition = { topLevelExitTransition }
            ) {
                LocationsScreen()
            }

            composable(
                route = devSettingsRoute,
                enterTransition = { topLevelEnterTransition },
                exitTransition = { topLevelExitTransition },
                popEnterTransition = { topLevelEnterTransition },
                popExitTransition = { topLevelExitTransition }
            ) {
                DevSettingsScreen()
            }

            composable(
                route = locationEventsRoute,
                arguments = listOf(
                    navArgument(LOCATION_NAME_ARG) { type = NavType.StringType }
                )
            ) {
                val locationName = it.arguments?.getString(LOCATION_NAME_ARG)
                    ?: throw IllegalArgumentException("Navigated to location events without a location name!")
                LocationEventsScreen(locationName)
            }
        }
    }
}
