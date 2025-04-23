package com.district37.toastmasters

import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.BackendTabInfo
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseEventService {
    private val json = System.getenv("GOOGLE_CREDENTIALS_JSON")
        ?: error("Missing GOOGLE_CREDENTIALS_JSON env variable")
    private val serviceAccount = GoogleCredentials.fromStream(json.byteInputStream())

    private val firestore: Firestore = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("district37-toastmasters")
        .setCredentials(serviceAccount)
        .build()
        .service

    suspend fun createEvent(event: BackendEventDetails): BackendEventDetails = withContext(Dispatchers.IO) {
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
                val event = doc.toObject(BackendEventDetails::class.java)
                event.let {
                    BackendEventPreview(
                        id = it.id,
                        title = it.title,
                        image = it.images?.firstOrNull(),
                        time = it.time,
                        locationInfo = it.locationInfo
                    )
                }
            }
        }

    suspend fun getEventsByIds(ids: List<Int>): List<BackendEventPreview> =
        withContext(Dispatchers.IO) {
            val events = firestore.collection("events")
                .whereIn("id", ids)
                .get()
                .get()
                .documents
                .mapNotNull { doc ->
                    val event = doc.toObject(BackendEventDetails::class.java)
                    event.let {
                        BackendEventPreview(
                            id = it.id,
                            title = it.title,
                            image = it.images?.firstOrNull(),
                            time = it.time,
                            locationInfo = it.locationInfo
                        )
                    }
                }
            events
        }

    suspend fun getAvailableTabsInfo(): List<BackendTabInfo> = withContext(Dispatchers.IO) {
        val tabs = firestore.collection("tabs")
            .get()
            .get()
            .documents
            .mapNotNull { doc ->
                doc.toObject(BackendTabInfo::class.java)
            }
        tabs
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
                dateKey = update.dateKey ?: currentEvent.dateKey
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
                    dateKey = update.dateKey ?: currentEvent.dateKey
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