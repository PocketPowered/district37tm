package com.district37.toastmasters.navigation

import com.wongislandd.nexus.navigation.NavigationItem

val EVENT_ID_ARG = "eventId"
val LOCATION_NAME_ARG = "locationName"

val supportedNavigationItems = mutableMapOf(
    NavigationItemKey.SPLASH_SCREEN to NavigationItem(
        NavigationItemKey.SPLASH_SCREEN.name,
        "",
        "splash"
    ),
    NavigationItemKey.NOTIFICATION_ONBOARDING to NavigationItem(
        NavigationItemKey.NOTIFICATION_ONBOARDING.name,
        "Enable Notifications",
        "notification-onboarding"
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
    NavigationItemKey.EVENT_CALENDAR to NavigationItem(
        NavigationItemKey.EVENT_CALENDAR.name,
        "Agenda",
        "eventcalendar"
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
    NavigationItemKey.MAPS to NavigationItem(
        NavigationItemKey.MAPS.name,
        "Maps",
        "maps"
    ),
    NavigationItemKey.LOCATION_EVENTS to NavigationItem(
        NavigationItemKey.LOCATION_EVENTS.name,
        "Events at Location",
        "locationevents",
        supportedArgs = listOf(LOCATION_NAME_ARG)
    ),
    NavigationItemKey.DEV_SETTINGS to NavigationItem(
        NavigationItemKey.DEV_SETTINGS.name,
        "Dev Settings",
        "dev-settings"
    )
)
