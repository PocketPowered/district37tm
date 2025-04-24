package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseReferencesService  {
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
                    id = doc.id,
                    displayName = doc.getString("displayName"),
                    url = doc.getString("url"),
                    description = doc.getString("description")
                )
            }
    }

    suspend fun createReference(reference: BackendExternalLink): BackendExternalLink = withContext(Dispatchers.IO) {
        val docRef = referencesCollection.document()
        val newReference = reference.copy(id = docRef.id)
        docRef.set(mapOf(
            "displayName" to newReference.displayName,
            "url" to newReference.url,
            "description" to newReference.description
        )).get()
        newReference
    }

    suspend fun updateReference(id: String, reference: BackendExternalLink): BackendExternalLink = withContext(Dispatchers.IO) {
        val docRef = referencesCollection.document(id)
        val updatedReference = reference.copy(id = id)
        docRef.update(mapOf(
            "displayName" to updatedReference.displayName,
            "url" to updatedReference.url,
            "description" to updatedReference.description
        )).get()
        updatedReference
    }

    suspend fun deleteReference(id: String): Boolean = withContext(Dispatchers.IO) {
        val docRef = referencesCollection.document(id)
        docRef.delete().get()
        true
    }
}