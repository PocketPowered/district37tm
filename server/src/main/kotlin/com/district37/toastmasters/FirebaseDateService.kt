package com.district37.toastmasters

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseDateService {
    private val json = System.getenv("GOOGLE_CREDENTIALS_JSON")
        ?: error("Missing GOOGLE_CREDENTIALS_JSON env variable")
    private val serviceAccount = GoogleCredentials.fromStream(json.byteInputStream())

    private val firestore: Firestore = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("district37-toastmasters")
        .setCredentials(serviceAccount)
        .build()
        .service

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
        val docRef = firestore.collection("dates").document(timestamp.toString())
        docRef.delete().get()
        true
    }
} 