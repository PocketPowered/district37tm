package com.district37.toastmasters.navigation

import androidx.navigation.NavController
import com.district37.toastmasters.features.account.components.ProfileTab
import com.district37.toastmasters.util.Logger

/**
 * Handles navigation for processed deeplinks.
 * Takes a pending deeplink destination and navigates to the appropriate screen.
 *
 * With the unified NavGraph, all destinations are directly accessible without
 * needing to switch tabs first.
 */
class DeeplinkNavigator(
    private val navController: NavController,
    private val deeplinkHandler: DeeplinkHandler,
    private val profileTabNavigationState: ProfileTabNavigationState
) {
    private val TAG = "DeeplinkNavigator"

    /**
     * Process the pending deeplink and navigate to the appropriate screen.
     * All destinations are now directly accessible from the single unified NavGraph.
     *
     * @return true if navigation was successful, false otherwise
     */
    suspend fun handlePendingDeeplink(): Boolean {
        val destination = deeplinkHandler.pendingDeeplink.value ?: return false

        try {
            Logger.d(TAG, "Navigating to: $destination")

            val navigated = when (destination) {
                is DeeplinkDestination.Event -> {
                    val id = destination.id
                    if (id != null) {
                        navController.navigate(EventDetailNavigationArgs(id))
                        true
                    } else {
                        // TODO: Support slug-based navigation when detail ViewModels support it
                        Logger.i(TAG, "Slug-based event navigation not yet supported: ${destination.slug}")
                        false
                    }
                }
                is DeeplinkDestination.Venue -> {
                    val id = destination.id
                    if (id != null) {
                        navController.navigate(VenueDetailNavigationArgs(id))
                        true
                    } else {
                        Logger.i(TAG, "Slug-based venue navigation not yet supported: ${destination.slug}")
                        false
                    }
                }
                is DeeplinkDestination.Performer -> {
                    val id = destination.id
                    if (id != null) {
                        navController.navigate(PerformerDetailNavigationArgs(id))
                        true
                    } else {
                        Logger.i(TAG, "Slug-based performer navigation not yet supported: ${destination.slug}")
                        false
                    }
                }
                is DeeplinkDestination.AgendaItem -> {
                    navController.navigate(AgendaItemDetailNavigationArgs(destination.id))
                    true
                }
                is DeeplinkDestination.Location -> {
                    val id = destination.id
                    if (id != null) {
                        navController.navigate(LocationDetailNavigationArgs(id))
                        true
                    } else {
                        Logger.i(TAG, "Slug-based location navigation not yet supported: ${destination.slug}")
                        false
                    }
                }
                is DeeplinkDestination.Organization -> {
                    val id = destination.id
                    if (id != null) {
                        navController.navigate(OrganizationDetailNavigationArgs(id))
                        true
                    } else {
                        Logger.i(TAG, "Slug-based organization navigation not yet supported: ${destination.slug}")
                        false
                    }
                }
                is DeeplinkDestination.Profile -> {
                    // TODO: Implement profile navigation once backend supports userByUsername query
                    Logger.i(TAG, "Profile deeplinks not yet supported (username: ${destination.username})")
                    false
                }
                is DeeplinkDestination.OAuthCallback -> {
                    // OAuth is handled separately in platform code
                    false
                }
                is DeeplinkDestination.Chat -> {
                    // Chat is now directly accessible from the unified NavGraph
                    navController.navigate(
                        ChatRoute(
                            conversationId = destination.conversationId,
                            displayName = destination.displayName,
                            avatarUrl = destination.avatarUrl
                        )
                    )
                    true
                }
                is DeeplinkDestination.MyRequests -> {
                    // Set the pending tab to REQUESTS before navigating
                    profileTabNavigationState.setPendingTab(ProfileTab.REQUESTS)
                    // Navigate to the Account tab (which shows the profile)
                    navController.navigate(AccountHomeRoute) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    true
                }
                is DeeplinkDestination.Unknown -> {
                    Logger.i(TAG, "Cannot navigate to unknown destination")
                    false
                }
            }

            if (navigated) {
                deeplinkHandler.clearPendingDeeplink()
                Logger.d(TAG, "Navigation successful, cleared pending deeplink")
            }

            return navigated
        } catch (e: Exception) {
            Logger.e(TAG, "Navigation failed: ${e.message}")
            return false
        }
    }
}
