package com.district37.toastmasters.di

import com.district37.toastmasters.AppViewModel
import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.database.DatabaseFactory
import com.district37.toastmasters.database.FavoritesRepository
import com.district37.toastmasters.database.NotificationRepository
import com.district37.toastmasters.database.TMDatabase
import com.district37.toastmasters.eventdetails.eventDetailsModule
import com.district37.toastmasters.eventlist.eventListModule
import com.district37.toastmasters.favorites.FavoritedEventsScreenStateSlice
import com.district37.toastmasters.navigation.supportedNavigationItems
import com.district37.toastmasters.notifications.NotificationsSlice
import com.district37.toastmasters.util.EpochTimeTransformer
import com.wongislandd.nexus.di.infraModule
import com.wongislandd.nexus.navigation.NavigationItem
import com.wongislandd.nexus.navigation.NavigationSlice
import com.wongislandd.nexus.weblink.webLinkModule
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun appModule(appContext: Any? = null) = module {
    viewModelOf(::AppViewModel)
    factoryOf(::NavigationSlice)
    factoryOf(::NotificationsSlice)
    singleOf(::EventRepository)
    singleOf(::EpochTimeTransformer)
    single<Set<NavigationItem>> { supportedNavigationItems.values.toSet() }
    single { DatabaseFactory(appContext) }
    single { TMDatabase(get<DatabaseFactory>().createDriver()) }
    singleOf(::NotificationRepository)
    singleOf(::FavoritesRepository)
    singleOf(::FavoritedEventsScreenStateSlice)
}

fun initializeKoin(context: Any? = null) =
    startKoin {
        modules(
            *infraModule.toTypedArray(),
            appModule(context),
            webLinkModule(context),
            eventListModule,
            eventDetailsModule,
        )
    }