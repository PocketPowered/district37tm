package com.district37.toastmasters.navigation

import com.wongislandd.nexus.navigation.NavigationItem

val EVENT_ID_ARG = "eventId"

val supportedNavigationItems = mutableMapOf(
    NavigationItemKey.SPLASH_SCREEN to NavigationItem(
        NavigationItemKey.SPLASH_SCREEN.name,
        "",
        "splash"
    ),
    NavigationItemKey.LANDING_PAGE to NavigationItem(
        NavigationItemKey.LANDING_PAGE.name,
        "District 37 Toastmasters",
        "home"
    ),
    NavigationItemKey.EVENT_LIST to NavigationItem(
        NavigationItemKey.EVENT_LIST.name,
        "Agenda",
        "eventlist"
    ),
    NavigationItemKey.EVENT_DETAILS to NavigationItem(
        NavigationItemKey.EVENT_DETAILS.name,
        "Event Details",
        "event",
        supportedArgs = listOf(EVENT_ID_ARG)
    ),
    NavigationItemKey.NOTIFICATIONS to NavigationItem(
        NavigationItemKey.NOTIFICATIONS.name,
        "Notifications",
        "notifications"
    ),
    NavigationItemKey.RESOURCES to NavigationItem(
        NavigationItemKey.RESOURCES.name,
        "Resources",
        "resources"
    ),
    NavigationItemKey.FIRST_TIMER_RESOURCES to NavigationItem(
        NavigationItemKey.FIRST_TIMER_RESOURCES.name,
        "First Timer",
        "first-timer-resources"
    )
)