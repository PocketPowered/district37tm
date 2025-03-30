package com.district37.toastmasters.eventdetails

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val eventDetailsModule = module {
    viewModelOf(::EventDetailsViewModel)
    factoryOf(::EventDetailsScreenStateSlice)
}