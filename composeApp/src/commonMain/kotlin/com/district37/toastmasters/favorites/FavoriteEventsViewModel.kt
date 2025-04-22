package com.district37.toastmasters.favorites

import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.viewmodel.SliceableViewModel

class FavoriteEventsViewModel(
    val favoritesSlice: FavoritedEventsScreenStateSlice,
    uiEvent: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(
    uiEvent,
    backChannelEventBus
) {

    init {
        registerSlices(favoritesSlice)
    }
} 