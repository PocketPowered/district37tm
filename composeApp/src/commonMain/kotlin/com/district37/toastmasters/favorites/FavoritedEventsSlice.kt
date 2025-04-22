package com.district37.toastmasters.favorites

import com.district37.toastmasters.database.FavoritesRepository
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.viewmodel.ViewModelSlice

data class FavoriteEventToggle(val eventId: Int, val isFavorited: Boolean) : UiEvent

class FavoritedEventsSlice(
    private val favoritesRepository: FavoritesRepository
) : ViewModelSlice() {

    override fun handleUiEvent(event: UiEvent) {
        super.handleUiEvent(event)
        when (event) {
            is FavoriteEventToggle -> {
                if (event.isFavorited) {
                    favoritesRepository.addFavorite(event.eventId.toLong())
                } else {
                    favoritesRepository.removeFavorite(event.eventId.toLong())
                }
            }
        }
    }
} 