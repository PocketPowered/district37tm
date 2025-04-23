package com.district37.toastmasters

import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.BackendTabInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventService(private val firebaseService: FirebaseEventService) {

    suspend fun getAllEvents(): List<BackendEventDetails> = withContext(Dispatchers.IO) {
        firebaseService.getAllEvents()
    }

    suspend fun getEvent(id: Int): BackendEventDetails = withContext(Dispatchers.IO) {
        firebaseService.getEvent(id)
    }

    suspend fun getEventPreviews(dateKey: String?): List<BackendEventPreview> =
        withContext(Dispatchers.IO) {
            firebaseService.getEventPreviews(dateKey)
        }

    suspend fun getEventsByIds(ids: List<Int>): List<BackendEventPreview> =
        withContext(Dispatchers.IO) {
            firebaseService.getEventsByIds(ids)
        }

    suspend fun getAvailableTabsInfo(): List<BackendTabInfo> = withContext(Dispatchers.IO) {
        firebaseService.getAvailableTabsInfo()
    }

    suspend fun updateEvent(event: BackendEventDetails): BackendEventDetails = withContext(Dispatchers.IO) {
        firebaseService.updateEvent(event)
    }

    suspend fun updateEventPartial(update: BackendEventDetails): BackendEventDetails = withContext(Dispatchers.IO) {
        firebaseService.updateEventPartial(update)
    }

    suspend fun updateEvents(events: List<BackendEventDetails>): List<BackendEventDetails> = withContext(Dispatchers.IO) {
        firebaseService.updateEvents(events)
    }

    suspend fun updateEventsPartial(updates: List<BackendEventDetails>): List<BackendEventDetails> = withContext(Dispatchers.IO) {
        firebaseService.updateEventsPartial(updates)
    }

    suspend fun deleteEvent(id: Int): Boolean = withContext(Dispatchers.IO) {
        firebaseService.deleteEvent(id)
    }

    suspend fun deleteEvents(ids: List<Int>): Boolean = withContext(Dispatchers.IO) {
        firebaseService.deleteEvents(ids)
    }
}