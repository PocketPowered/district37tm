package com.wongislandd.nexus.di

import com.wongislandd.nexus.events.eventsModule
import com.wongislandd.nexus.navigation.navigationModule
import com.wongislandd.nexus.networking.networkModule
import org.koin.core.module.Module

val infraModule: List<Module> = listOf(networkModule, navigationModule, eventsModule)

