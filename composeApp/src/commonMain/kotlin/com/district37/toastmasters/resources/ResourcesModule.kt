package com.district37.toastmasters.resources

import com.district37.toastmasters.ResourcesRepository
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val resourcesModule = module {
    viewModelOf(::ResourcesViewModel)
    singleOf(::ResourcesRepository)
} 