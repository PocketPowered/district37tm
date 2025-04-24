package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class FirebaseReferencesService : KoinComponent {
    private val json = System.getenv("GOOGLE_CREDENTIALS_JSON")
        ?: error("Missing GOOGLE_CREDENTIALS_JSON env variable")
    private val serviceAccount = GoogleCredentials.fromStream(json.byteInputStream())

    private val firestore: Firestore = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("district37-toastmasters")
        .setCredentials(serviceAccount)
        .build()
        .service

    private val referencesCollection = firestore.collection("references")

    suspend fun getAllReferences(): List<BackendExternalLink> = withContext(Dispatchers.IO) {
        referencesCollection
            .get()
            .get()
            .documents
            .mapNotNull { doc ->
                BackendExternalLink(
                    displayName = doc.getString("displayName"),
                    url = doc.getString("url")
                )
            }
    }

    suspend fun createReference(reference: BackendExternalLink): BackendExternalLink = withContext(Dispatchers.IO) {
        val docRef = referencesCollection.document()
        docRef.set(mapOf(
            "displayName" to reference.displayName,
            "url" to reference.url
        )).get()
        reference
    }

    suspend fun updateReference(id: String, reference: BackendExternalLink): BackendExternalLink = withContext(Dispatchers.IO) {
        val docRef = referencesCollection.document(id)
        docRef.update(mapOf(
            "displayName" to reference.displayName,
            "url" to reference.url
        )).get()
        reference
    }

    suspend fun deleteReference(id: String): Boolean = withContext(Dispatchers.IO) {
        val docRef = referencesCollection.document(id)
        docRef.delete().get()
        true
    }
}