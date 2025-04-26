package com.district37.toastmasters

import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.district37.toastmasters.favorites.FavoritedEventsSlice
import com.district37.toastmasters.navigation.NavigationItemKey
import com.district37.toastmasters.notifications.NotificationsSlice
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.navigation.NavigationSlice
import com.wongislandd.nexus.viewmodel.SliceableViewModel

class AppViewModel(
    val navigationSlice: NavigationSlice,
    val notificationsSlice: NotificationsSlice,
    val versionInfo: VersionInfo,
    favoritesSlice: FavoritedEventsSlice,
    uiEventBus: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEventBus, backChannelEventBus) {

    init {
        registerSlices(navigationSlice, notificationsSlice, favoritesSlice)
    }

    fun navigate(
        navigationController: NavController,
        navigationKey: NavigationItemKey,
        args: Map<String, Any?> = emptyMap(),
        removeSelfFromStack: Boolean = false
    ) {
        Logger.withTag("Navigation").i { "Navigating to $navigationKey!" }
        navigationSlice.navigationHelper.navigate(
            navigationController,
            navigationKey.name,
            args,
            removeSelfFromStack
        )
    }
}