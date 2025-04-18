package com.district37.toastmasters.navigation

import com.wongislandd.nexus.navigation.NavigationItem

val EVENT_ID_ARG = "eventId"

val supportedNavigationItems = mutableMapOf(
    NavigationItemKey.LANDING_PAGE to NavigationItem(
        NavigationItemKey.LANDING_PAGE.name,
        "District 37 Toastmasters",
        "home"
    ),
    NavigationItemKey.EVENT_LIST to NavigationItem(
        NavigationItemKey.EVENT_LIST.name,
        "Events",
        "eventlist"
    ),
    NavigationItemKey.EVENT_DETAILS to NavigationItem(
        NavigationItemKey.EVENT_DETAILS.name,
        "Event Details",
        "event",
        supportedArgs = listOf(EVENT_ID_ARG)
    ),
    NavigationItemKey.INFO to NavigationItem(
        NavigationItemKey.INFO.name,
        "Info",
        "info"
    )

)