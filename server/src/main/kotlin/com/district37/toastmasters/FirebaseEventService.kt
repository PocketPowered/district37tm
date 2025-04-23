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
    val serviceAccount = GoogleCredentials.fromStream(json.byteInputStream())

    private val firestore: Firestore = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("district37-toastmasters")
        .setCredentials(serviceAccount)
        .build()
        .service

    suspend fun getEvent(id: Int): BackendEventDetails = withContext(Dispatchers.IO) {
        val doc = firestore.collection("events").document(id.toString()).get().get()
        if (!doc.exists()) {
            throw NotFoundException("Event not found")
        }
        doc.toObject(BackendEventDetails::class.java) ?: throw NotFoundException("Event not found")
    }

    suspend fun getEventPreviews(dateKey: String?): List<BackendEventPreview> =
        withContext(Dispatchers.IO) {
            val query = if (dateKey != null) {
                firestore.collection("events")
                    .whereEqualTo("dateKey", dateKey)
            } else {
                firestore.collection("events")
            }

            query.get().get().documents.mapNotNull { doc ->
                val event = doc.toObject(BackendEventDetails::class.java)
                event?.let {
                    BackendEventPreview(
                        id = it.id,
                        title = it.title,
                        image = it.images.firstOrNull() ?: "no url in mock",
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
                    event?.let {
                        BackendEventPreview(
                            id = it.id,
                            title = it.title,
                            image = it.images.firstOrNull() ?: "no url in mock",
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
} 