package com.wongislandd.nexus.viewmodel

import androidx.lifecycle.ViewModel
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent

abstract class SliceableViewModel(
    val uiEventBus: EventBus<UiEvent>,
    val backChannelEventBus: EventBus<BackChannelEvent>
) : ViewModel() {

    private fun registerSlice(viewModelSlice: ViewModelSlice) {
        viewModelSlice.register(this)
    }

    fun registerSlices(vararg viewModelSlices: ViewModelSlice) {
        viewModelSlices.forEach { slice ->
            registerSlice(slice)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}