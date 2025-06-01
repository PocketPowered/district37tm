package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink
import com.google.cloud.firestore.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FirebaseResourcesService : KoinComponent {
    private val firestore: Firestore by inject()
    private val resourcesCollection = firestore.collection("resources")
    private val firstTimerResourcesCollection = firestore.collection("firstTimerResources")

    suspend fun getAllResources(): List<BackendExternalLink> = withContext(Dispatchers.IO) {
        resourcesCollection
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

    suspend fun createResource(resource: BackendExternalLink): BackendExternalLink = withContext(Dispatchers.IO) {
        val docRef = resourcesCollection.document()
        val newResource = resource.copy(id = docRef.id)
        docRef.set(mapOf(
            "displayName" to newResource.displayName,
            "url" to newResource.url,
            "description" to newResource.description
        )).get()
        newResource
    }

    suspend fun updateResource(id: String, resource: BackendExternalLink): BackendExternalLink = withContext(Dispatchers.IO) {
        val docRef = resourcesCollection.document(id)
        val updatedResource = resource.copy(id = id)
        docRef.update(mapOf(
            "displayName" to updatedResource.displayName,
            "url" to updatedResource.url,
            "description" to updatedResource.description
        )).get()
        updatedResource
    }

    suspend fun deleteResource(id: String): Boolean = withContext(Dispatchers.IO) {
        val docRef = resourcesCollection.document(id)
        docRef.delete().get()
        true
    }

    suspend fun getAllFirstTimerResources(): List<BackendExternalLink> = withContext(Dispatchers.IO) {
        firstTimerResourcesCollection
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

    suspend fun createFirstTimerResource(resource: BackendExternalLink): BackendExternalLink = withContext(Dispatchers.IO) {
        val docRef = firstTimerResourcesCollection.document()
        val newResource = resource.copy(id = docRef.id)
        docRef.set(mapOf(
            "displayName" to newResource.displayName,
            "url" to newResource.url,
            "description" to newResource.description
        )).get()
        newResource
    }

    suspend fun updateFirstTimerResource(id: String, resource: BackendExternalLink): BackendExternalLink = withContext(Dispatchers.IO) {
        val docRef = firstTimerResourcesCollection.document(id)
        val updatedResource = resource.copy(id = id)
        docRef.update(mapOf(
            "displayName" to updatedResource.displayName,
            "url" to updatedResource.url,
            "description" to updatedResource.description
        )).get()
        updatedResource
    }

    suspend fun deleteFirstTimerResource(id: String): Boolean = withContext(Dispatchers.IO) {
        val docRef = firstTimerResourcesCollection.document(id)
        docRef.delete().get()
        true
    }
}