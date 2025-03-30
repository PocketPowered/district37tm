package com.wongislandd.nexus.events

import org.koin.dsl.module

val eventsModule = module {
    eventBusFactory<BackChannelEvent>()
    eventBusFactory<UiEvent>()
}