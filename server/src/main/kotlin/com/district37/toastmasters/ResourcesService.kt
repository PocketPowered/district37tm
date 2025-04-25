package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink

class ResourcesService(private val firebaseService: FirebaseResourcesService) {

    suspend fun getAllResources(): List<BackendExternalLink> {
        return firebaseService.getAllResources()
    }

    suspend fun createResource(resource: BackendExternalLink): BackendExternalLink {
        return firebaseService.createResource(resource)
    }

    suspend fun updateResource(id: String, resource: BackendExternalLink): BackendExternalLink {
        return firebaseService.updateResource(id, resource)
    }

    suspend fun deleteResource(id: String) {
        firebaseService.deleteResource(id)
    }

    // First-timer resources methods
    suspend fun getAllFirstTimerResources(): List<BackendExternalLink> {
        return firebaseService.getAllFirstTimerResources()
    }

    suspend fun createFirstTimerResource(resource: BackendExternalLink): BackendExternalLink {
        return firebaseService.createFirstTimerResource(resource)
    }

    suspend fun updateFirstTimerResource(id: String, resource: BackendExternalLink): BackendExternalLink {
        return firebaseService.updateFirstTimerResource(id, resource)
    }

    suspend fun deleteFirstTimerResource(id: String) {
        firebaseService.deleteFirstTimerResource(id)
    }
} 