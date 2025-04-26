package com.wongislandd.nexus.navigation

import com.district37.toastmasters.navigation.NavigationItemKey
import com.district37.toastmasters.navigation.supportedNavigationItems
import com.wongislandd.nexus.viewmodel.ViewModelSlice
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

    init {
        navigationItemRegistry.register(*supportedNavigationItems.toTypedArray())
    }

    fun confirmNavigationToRoute(route: String) {
        // TODO Tighten the navigation tracking off of loose strings
        supportedNavigationItems.values.find { it.completeRoute == route }?.also { route ->
            _currentlySelectedNavigationItem.update {
                route
            }
        } ?: throw IllegalArgumentException("No navigation item found for route: $route")

    }
}