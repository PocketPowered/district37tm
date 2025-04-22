package com.district37.toastmasters.favorites

import com.district37.toastmasters.database.FavoritesRepository
import com.wongislandd.nexus.viewmodel.ViewModelSlice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FavoritesSlice(private val favoritesRepository: FavoritesRepository) : ViewModelSlice(),
    KoinComponent {

    private val _favoritesFlow = MutableStateFlow<Set<Long>>(emptySet())
    val favoritesFlow: StateFlow<Set<Long>> = _favoritesFlow

    override fun afterInit() {
        super.afterInit()

        // Collect all favorites
        sliceScope.launch {
            favoritesRepository.getAllFavorites()
                .collect { favorites ->
                    _favoritesFlow.value = favorites.toSet()
                }
        }
    }

    fun toggleFavorite(eventId: Long) {
        sliceScope.launch {
            if (_favoritesFlow.value.contains(eventId)) {
                favoritesRepository.removeFavorite(eventId)
            } else {
                favoritesRepository.addFavorite(eventId)
            }
        }
    }

    fun isFavorite(eventId: Long): Boolean {
        return _favoritesFlow.value.contains(eventId)
    }
}