package com.wongislandd.nexus.navigation

import com.wongislandd.nexus.viewmodel.ViewModelSlice

class NavigationSlice(
    supportedNavigationItems: Set<NavigationItem>,
    navigationItemRegistry: NavigationItemRegistry,
    val navigationHelper: NavigationHelper
) : ViewModelSlice() {

    init {
        navigationItemRegistry.register(*supportedNavigationItems.toTypedArray())
    }
}