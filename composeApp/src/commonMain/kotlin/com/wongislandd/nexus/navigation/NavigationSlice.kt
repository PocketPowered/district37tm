package com.wongislandd.nexus.navigation

import com.wongislandd.nexus.viewmodel.ViewModelSlice
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class NavigationSlice(
    supportedNavigationItems: Set<NavigationItem>,
    navigationItemRegistry: NavigationItemRegistry,
    val navigationHelper: NavigationHelper
) : ViewModelSlice() {

    private val _currentlySelectedNavigationItem: MutableStateFlow<NavigationItem> =
        MutableStateFlow(
            NavigationItem("default", "default", "default")
        )
    val currentlySelectedNavigationItem: StateFlow<NavigationItem> =
        _currentlySelectedNavigationItem
    private val allSupportedNavigationItems = supportedNavigationItems

    init {
        navigationItemRegistry.register(*supportedNavigationItems.toTypedArray())
    }

    fun confirmNavigationToRoute(route: String) {
        allSupportedNavigationItems.find { item ->
            item.completeRoute == route || item.baseRoute == route
        }?.also { route ->
            _currentlySelectedNavigationItem.update {
                route
            }
        } ?: Logger.withTag("Navigation").i {
            "Skipping navigation selection update for non-trackable route: $route"
        }
    }
}
