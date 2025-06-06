package com.district37.toastmasters.di

import com.district37.toastmasters.DateService
import com.district37.toastmasters.EventService
import com.district37.toastmasters.FirebaseDateService
import com.district37.toastmasters.FirebaseEventService
import com.district37.toastmasters.FirebaseLocationService
import com.district37.toastmasters.FirebaseNotificationService
import com.district37.toastmasters.FirebaseResourcesService
import com.district37.toastmasters.FirebaseUserFCMService
import com.district37.toastmasters.ResourcesService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val persistentModule = module {
    singleOf(::EventService)
    singleOf(::DateService)
    singleOf(::ResourcesService)
    singleOf(::FirebaseEventService)
    singleOf(::FirebaseUserFCMService)
    singleOf(::FirebaseDateService)
    singleOf(::FirebaseResourcesService)
    singleOf(::FirebaseNotificationService)
    singleOf(::FirebaseLocationService)
}