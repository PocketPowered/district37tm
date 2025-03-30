package com.wongislandd.nexus.events

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.module.Module

class EventBus<T: Event> {

    private val _events = MutableSharedFlow<T>(replay = 0)
    val events: SharedFlow<T> = _events.asSharedFlow()

    suspend fun sendEvent(event: T) {
        Logger.i(tag = "EventBus", null) {
            event.toString()
        }
        _events.emit(event)
    }
}

fun <T: Event> CoroutineScope.collectEvents(eventBus: EventBus<T>, onEvent: (T) -> Unit) {
    this.launch {
        eventBus.events.collect { event ->
            onEvent(event)
        }
    }
}

inline fun <reified T : Event> Module.eventBusFactory() {
    factory { EventBus<Event>() }
}