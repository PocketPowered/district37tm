package com.district37.toastmasters.locations

import com.district37.toastmasters.LocationsRepository
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val locationsModule = module {
    singleOf(::LocationsRepository)
    viewModelOf(::LocationsViewModel)
} 