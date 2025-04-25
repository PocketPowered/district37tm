package com.district37.toastmasters

import com.district37.toastmasters.models.Location
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseLocationService {
    private val json = System.getenv("GOOGLE_CREDENTIALS_JSON")
        ?: error("Missing GOOGLE_CREDENTIALS_JSON env variable")
    private val serviceAccount = GoogleCredentials.fromStream(json.byteInputStream())

    private val firestore: Firestore = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("district37-toastmasters")
        .setCredentials(serviceAccount)
        .build()
        .service

    private val locationsCollection = firestore.collection("locations")

    suspend fun getAllLocations(): List<Location> = withContext(Dispatchers.IO) {
        locationsCollection
            .get()
            .get()
            .documents
            .mapNotNull { doc ->
                Location(
                    id = doc.id,
                    locationName = doc.getString("locationName") ?: "",
                    locationImages = doc.get("locationImages") as? List<String> ?: emptyList()
                )
            }
    }

    suspend fun createLocation(location: Location): Location = withContext(Dispatchers.IO) {
        val docRef = locationsCollection.document()
        val newLocation = location.copy(id = docRef.id)
        docRef.set(mapOf(
            "locationName" to newLocation.locationName,
            "locationImages" to newLocation.locationImages
        )).get()
        newLocation
    }

    suspend fun updateLocation(id: String, location: Location): Location = withContext(Dispatchers.IO) {
        val docRef = locationsCollection.document(id)
        val updatedLocation = location.copy(id = id)
        docRef.update(mapOf(
            "locationName" to updatedLocation.locationName,
            "locationImages" to updatedLocation.locationImages
        )).get()
        updatedLocation
    }

    suspend fun deleteLocation(id: String): Boolean = withContext(Dispatchers.IO) {
        val docRef = locationsCollection.document(id)
        docRef.delete().get()
        true
    }
} 