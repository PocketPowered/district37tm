package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReferencesService : KoinComponent {
    private val firebaseService: FirebaseReferencesService by inject()

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