package com.district37.toastmasters.eventlist

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val eventListModule = module {
    viewModelOf(::EventListViewModel)
    factoryOf(::EventListScreenStateSlice)
    singleOf(::EventPreviewTransformer)
}