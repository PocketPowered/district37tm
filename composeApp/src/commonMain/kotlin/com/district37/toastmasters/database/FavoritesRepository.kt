package com.district37.toastmasters.database

import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

class FavoritesRepository(database: TMDatabase) : KoinComponent {

    private val favoritesQueries = database.favoritesQueries

    fun getAllFavorites(): Flow<List<Long>> {
        return favoritesQueries.getAllFavorites()
            .asFlow()
            .map { query ->
                query.executeAsList().map { it.event_id }
            }
    }

    fun isFavorite(eventId: Long): Flow<Boolean> {
        return favoritesQueries.isFavorite(eventId)
            .asFlow()
            .map { query ->
                query.executeAsOne()
            }
    }

    fun addFavorite(eventId: Long) {
        favoritesQueries.insertFavorite(
            event_id = eventId,
            created_at = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun removeFavorite(eventId: Long) {
        favoritesQueries.deleteFavorite(eventId)
    }
} 