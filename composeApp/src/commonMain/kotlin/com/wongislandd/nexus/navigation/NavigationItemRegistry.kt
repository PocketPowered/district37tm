package com.wongislandd.nexus.navigation

class NavigationItemRegistry {

    private val registeredItems = mutableMapOf<String, NavigationItem>()

    private fun register(item: NavigationItem) {
        registeredItems[item.navigationKey] = item
    }

    fun register(vararg items: NavigationItem) {
        items.forEach { register(it) }
    }

    fun getNavigationItem(key: String): NavigationItem? {
        return registeredItems[key]
    }
}