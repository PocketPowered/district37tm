package com.district37.toastmasters

import com.district37.toastmasters.models.BackendEventDetails
import com.google.cloud.firestore.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FirebaseDateService : KoinComponent {
    private val firestore: Firestore by inject()
    private val eventService: FirebaseEventService by inject()

    suspend fun getAvailableDates(): List<Long> = withContext(Dispatchers.IO) {
        firestore.collection("dates")
            .get()
            .get()
            .documents
            .mapNotNull { doc ->
                doc.id.toLongOrNull()
            }
            .sorted()
    }

    suspend fun addDate(timestamp: Long): Long = withContext(Dispatchers.IO) {
        val docRef = firestore.collection("dates").document(timestamp.toString())
        docRef.set(mapOf("timestamp" to timestamp)).get()
        timestamp
    }

    suspend fun removeDate(timestamp: Long): Boolean = withContext(Dispatchers.IO) {
        // First, get all events for this date
        val events = firestore.collection("events")
            .whereEqualTo("dateKey", timestamp)
            .get()
            .get()
            .documents
            .mapNotNull { doc ->
                doc.toObject(BackendEventDetails::class.java)
            }

        // Delete all events for this date
        if (events.isNotEmpty()) {
            val eventIds = events.map { it.id }
            eventService.deleteEvents(eventIds)
        }

        // Finally, delete the date itself
        val docRef = firestore.collection("dates").document(timestamp.toString())
        docRef.delete().get()
        true
    }
} 