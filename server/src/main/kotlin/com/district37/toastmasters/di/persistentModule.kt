package com.district37.toastmasters.di

import com.district37.toastmasters.EventService
import com.district37.toastmasters.FirebaseEventService
import com.district37.toastmasters.FirebaseUserFCMService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val persistentModule = module {
    singleOf(::EventService)
    singleOf(::FirebaseEventService)
    singleOf(::FirebaseUserFCMService)
}