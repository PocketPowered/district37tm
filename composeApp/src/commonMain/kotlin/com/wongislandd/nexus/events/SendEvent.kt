package com.wongislandd.nexus.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.sendEvent(eventBus: EventBus<UiEvent>, event: UiEvent) {
    launch {
        eventBus.sendEvent(event)
    }
}
