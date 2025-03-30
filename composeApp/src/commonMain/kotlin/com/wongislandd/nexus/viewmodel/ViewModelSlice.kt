package com.wongislandd.nexus.viewmodel

import androidx.lifecycle.viewModelScope
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.events.collectEvents
import kotlinx.coroutines.CoroutineScope


abstract class ViewModelSlice {
    lateinit var sliceScope: CoroutineScope

    lateinit var uiEvents: EventBus<UiEvent>

    lateinit var backChannelEvents: EventBus<BackChannelEvent>

    fun register(viewModel: SliceableViewModel) {
        sliceScope = viewModel.viewModelScope
        uiEvents = viewModel.uiEventBus
        backChannelEvents = viewModel.backChannelEventBus
        listenForUiEvents()
        listenForBackChannelEvents()
        afterInit()
    }

    open fun afterInit() {}

    private fun listenForUiEvents() {
        sliceScope.collectEvents(uiEvents) {
            handleUiEvent(it)
        }
    }

    private fun listenForBackChannelEvents() {
        sliceScope.collectEvents(backChannelEvents) {
            handleBackChannelEvent(it)
        }
    }

    open fun handleUiEvent(event: UiEvent) {}

    open fun handleBackChannelEvent(event: BackChannelEvent) {}
}