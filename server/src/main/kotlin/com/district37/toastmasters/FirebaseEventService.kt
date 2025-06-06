package com.district37.toastmasters

import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import com.google.cloud.firestore.Firestore
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FirebaseEventService : KoinComponent {
    private val firestore: Firestore by inject()

    suspend fun createEvent(event: BackendEventDetails): BackendEventDetails =
        withContext(Dispatchers.IO) {
            // Generate a new ID by finding the highest existing ID and incrementing it
            val maxId = firestore.collection("events")
                .orderBy("id", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .get()
                .documents
                .firstOrNull()
                ?.toObject(BackendEventDetails::class.java)
                ?.id ?: 0

            val newEvent = event.copy(id = maxId + 1)
            val docRef = firestore.collection("events").document(newEvent.id.toString())
            docRef.set(newEvent).get()
            newEvent
        }

    suspend fun getAllEvents(): List<BackendEventDetails> = withContext(Dispatchers.IO) {
        firestore.collection("events")
            .get()
            .get()
            .documents
            .mapNotNull { doc ->
                doc.toObject(BackendEventDetails::class.java)
            }
            .sortedBy { it.time?.startTime }
    }

    suspend fun getEvent(id: Int): BackendEventDetails = withContext(Dispatchers.IO) {
        val doc = firestore.collection("events").document(id.toString()).get().get()
        if (!doc.exists()) {
            throw NotFoundException("Event not found")
        }
        doc.toObject(BackendEventDetails::class.java) ?: throw NotFoundException("Event not found")
    }

    suspend fun getEventPreviews(dateKey: Long?): List<BackendEventPreview> =
        withContext(Dispatchers.IO) {
            val query = if (dateKey != null) {
                firestore.collection("events")
                    .whereEqualTo("dateKey", dateKey)
            } else {
                firestore.collection("events")
            }

            query.get().get().documents.mapNotNull { doc ->
                doc.toObject(BackendEventDetails::class.java)
            }
                .sortedBy { it.time?.startTime }
                .map {
                    BackendEventPreview(
                        id = it.id,
                        title = it.title,
                        image = it.images?.firstOrNull(),
                        time = it.time,
                        locationInfo = it.locationInfo,
                        tag = it.tag
                    )
                }
        }

    suspend fun getEventsByIds(ids: List<Int>): List<BackendEventPreview> =
        withContext(Dispatchers.IO) {
            firestore.collection("events")
                .whereIn("id", ids)
                .get()
                .get()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(BackendEventDetails::class.java)
                }
                .sortedBy { it.time?.startTime }
                .map {
                    BackendEventPreview(
                        id = it.id,
                        title = it.title,
                        image = it.images?.firstOrNull(),
                        time = it.time,
                        locationInfo = it.locationInfo,
                        tag = it.tag
                    )
                }
        }

    suspend fun updateEvent(event: BackendEventDetails): BackendEventDetails =
        withContext(Dispatchers.IO) {
            val docRef = firestore.collection("events").document(event.id.toString())
            docRef.set(event).get()
            event
        }

    suspend fun updateEventPartial(update: BackendEventDetails): BackendEventDetails =
        withContext(Dispatchers.IO) {
            val docRef = firestore.collection("events").document(update.id.toString())
            val currentEvent = docRef.get().get().toObject(BackendEventDetails::class.java)
                ?: throw NotFoundException("Event not found")

            val updatedEvent = currentEvent.copy(
                images = update.images ?: currentEvent.images,
                title = update.title ?: currentEvent.title,
                description = update.description ?: currentEvent.description,
                time = update.time ?: currentEvent.time,
                locationInfo = update.locationInfo ?: currentEvent.locationInfo,
                agenda = update.agenda ?: currentEvent.agenda,
                additionalLinks = update.additionalLinks ?: currentEvent.additionalLinks,
                dateKey = update.dateKey ?: currentEvent.dateKey,
                tag = update.tag
            )

            docRef.set(updatedEvent).get()
            updatedEvent
        }

    suspend fun updateEvents(events: List<BackendEventDetails>): List<BackendEventDetails> =
        withContext(Dispatchers.IO) {
            val batch = firestore.batch()
            events.forEach { event ->
                val docRef = firestore.collection("events").document(event.id.toString())
                batch.set(docRef, event)
            }
            batch.commit().get()
            events
        }

    suspend fun updateEventsPartial(updates: List<BackendEventDetails>): List<BackendEventDetails> =
        withContext(Dispatchers.IO) {
            val batch = firestore.batch()
            val updatedEvents = mutableListOf<BackendEventDetails>()

            updates.forEach { update ->
                val docRef = firestore.collection("events").document(update.id.toString())
                val currentEvent = docRef.get().get().toObject(BackendEventDetails::class.java)
                    ?: throw NotFoundException("Event not found: ${update.id}")

                val updatedEvent = currentEvent.copy(
                    images = update.images ?: currentEvent.images,
                    title = update.title ?: currentEvent.title,
                    description = update.description ?: currentEvent.description,
                    time = update.time ?: currentEvent.time,
                    locationInfo = update.locationInfo ?: currentEvent.locationInfo,
                    agenda = update.agenda ?: currentEvent.agenda,
                    additionalLinks = update.additionalLinks ?: currentEvent.additionalLinks,
                    dateKey = update.dateKey ?: currentEvent.dateKey,
                    tag = update.tag
                )

                batch.set(docRef, updatedEvent)
                updatedEvents.add(updatedEvent)
            }

            batch.commit().get()
            updatedEvents
        }

    suspend fun deleteEvent(id: Int): Boolean = withContext(Dispatchers.IO) {
        val docRef = firestore.collection("events").document(id.toString())
        docRef.delete().get()
        true
    }

    suspend fun deleteEvents(ids: List<Int>): Boolean = withContext(Dispatchers.IO) {
        val batch = firestore.batch()
        ids.forEach { id ->
            val docRef = firestore.collection("events").document(id.toString())
            batch.delete(docRef)
        }
        batch.commit().get()
        true
    }
}