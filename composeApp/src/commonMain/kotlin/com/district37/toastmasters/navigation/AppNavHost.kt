package com.district37.toastmasters.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.district37.toastmasters.eventdetails.EventDetailsScreen
import com.district37.toastmasters.eventlist.EventListScreen
import com.district37.toastmasters.info.InfoScreen
import com.wongislandd.nexus.navigation.LocalNavHostController

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: NavigationItemKey = NavigationItemKey.EVENT_LIST
) {
    val navController = LocalNavHostController.current
    val startingDestination = supportedNavigationItems[startDestination]
        ?: throw IllegalStateException("Couldn't find registered start destination!")
    val pageTurnEnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(700)
    )

    val pageTurnExitTransition = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(700)
    )

    val pageReturnEnterTransition = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(700)
    )

    val pageReturnExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(700)
    )

    NavHost(
        navController = navController,
        startDestination = startingDestination.completeRoute,
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
        supportedNavigationItems.map { (_, navigationItem) ->
            when (NavigationItemKey.valueOf(navigationItem.navigationKey)) {
                NavigationItemKey.LANDING_PAGE -> {
                    // TODO
                }

                NavigationItemKey.EVENT_LIST -> {
                    composable(route = navigationItem.completeRoute) {
                        EventListScreen()
                    }
                }

                NavigationItemKey.EVENT_DETAILS -> {
                    composable(route = navigationItem.completeRoute,
                        arguments = listOf(
                            navArgument(EVENT_ID_ARG) { type = NavType.IntType }
                        )) {
                        val eventId = it.arguments?.getInt(EVENT_ID_ARG)
                            ?: throw IllegalArgumentException("Navigated to details screen without an event id!")
                        EventDetailsScreen(eventId)
                    }
                }
                NavigationItemKey.INFO -> {
                    composable(route = navigationItem.completeRoute) {
                        InfoScreen()
                    }
                }
            }
        }
    }
}