package com.district37.toastmasters.favorites

import com.district37.toastmasters.EventRepository
import com.district37.toastmasters.database.FavoritesRepository
import com.district37.toastmasters.eventlist.EventPreviewTransformer
import com.district37.toastmasters.models.EventPreview
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritedEventsScreenStateSlice(
    private val favoritesRepository: FavoritesRepository,
    private val eventsRepository: EventRepository,
    private val eventPreviewTransformer: EventPreviewTransformer
) : ViewModelSlice() {

    private val _screenState = MutableStateFlow<Resource<List<EventPreview>>>(Resource.Loading)
    val screenState = _screenState

    override fun afterInit() {
        super.afterInit()

        // Combine favorites with events to get favorited events
        sliceScope.launch(Dispatchers.IO) {
            favoritesRepository.getAllFavorites().collect { favorites ->
                if (favorites.isNotEmpty()) {
                    _screenState.update {
                        eventsRepository.getEventsByIds(favorites.map { it.toInt() })
                            .map { favoritedEvents ->
                                favoritedEvents.map { eventPreviewTransformer.transform(it) }
                            }
                    }
                }
            }
        }
    }
} 