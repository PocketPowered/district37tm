package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink

class ReferencesService(private val firebaseService: FirebaseReferencesService) {

    suspend fun getAllReferences(): List<BackendExternalLink> {
        return firebaseService.getAllReferences()
    }

    suspend fun createReference(reference: BackendExternalLink): BackendExternalLink {
        return firebaseService.createReference(reference)
    }

    suspend fun updateReference(id: String, reference: BackendExternalLink): BackendExternalLink {
        return firebaseService.updateReference(id, reference)
    }

    suspend fun deleteReference(id: String) {
        firebaseService.deleteReference(id)
    }
} 