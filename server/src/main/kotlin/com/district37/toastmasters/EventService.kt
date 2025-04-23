package com.district37.toastmasters

import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.BackendTabInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventService(private val firebaseService: FirebaseEventService) {

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
}