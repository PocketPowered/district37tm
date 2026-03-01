package com.district37.toastmasters

import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.district37.toastmasters.favorites.FavoritedEventsSlice
import com.district37.toastmasters.navigation.NavigationItemKey
import com.district37.toastmasters.navigation.supportedNavigationItems
import com.district37.toastmasters.notifications.NotificationOnboardingStore
import com.district37.toastmasters.notifications.NotificationsSlice
import com.district37.toastmasters.splash.SplashRepository
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.navigation.NavigationSlice
import com.wongislandd.nexus.viewmodel.SliceableViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(
    val navigationSlice: NavigationSlice,
    val notificationsSlice: NotificationsSlice,
    val versionInfo: VersionInfo,
    favoritesSlice: FavoritedEventsSlice,
    private val notificationOnboardingStore: NotificationOnboardingStore,
    private val splashRepository: SplashRepository,
    uiEventBus: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEventBus, backChannelEventBus) {

    init {
        registerSlices(navigationSlice, notificationsSlice, favoritesSlice)
        // Keep splash override cache fresh at app scope, even after splash navigation.
        viewModelScope.launch(Dispatchers.Default) {
            splashRepository.syncSplashImageUrlFromNetwork()
        }
    }

    fun navigate(
        navigationController: NavController,
        navigationKey: NavigationItemKey,
        args: Map<String, Any?> = emptyMap(),
        removeSelfFromStack: Boolean = false,
        isTopLevelDestination: Boolean = false
    ) {
        Logger.withTag("Navigation").i { "Navigating to $navigationKey!" }
        val homeRoute = supportedNavigationItems[NavigationItemKey.EVENT_LIST]?.baseRoute
            ?: throw IllegalStateException("Home route missing")
        navigationSlice.navigationHelper.navigate(
            navigationController,
            navigationKey.name,
            args,
            removeSelfFromStack,
            isTopLevelDestination,
            homeRoute
        )
    }

    fun completeNotificationOnboarding() {
        notificationOnboardingStore.setCompletedOnboarding(true)
    }
}
