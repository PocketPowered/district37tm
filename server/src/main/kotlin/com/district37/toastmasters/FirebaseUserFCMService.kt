package com.district37.toastmasters

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class UserFCMToken(
    val userId: String,
    val token: String,
    val deviceInfo: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

class FirebaseUserFCMService {
    private val json = System.getenv("GOOGLE_CREDENTIALS_JSON")
        ?: error("Missing GOOGLE_CREDENTIALS_JSON env variable")
    private val serviceAccount = GoogleCredentials.fromStream(json.byteInputStream())

    private val firestore: Firestore = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("district37-toastmasters")
        .setCredentials(serviceAccount)
        .build()
        .service

    suspend fun registerToken(userId: String, token: String, deviceInfo: String? = null): UserFCMToken = withContext(Dispatchers.IO) {
        // Remove any old tokens associated with this userId
        val existingTokens = firestore.collection("user_fcm_tokens")
            .whereEqualTo("userId", userId)
            .get()
            .get()
            .documents

        existingTokens.forEach { doc ->
            firestore.collection("user_fcm_tokens").document(doc.id).delete().get()
        }

        val userToken = UserFCMToken(
            userId = userId,
            token = token,
            deviceInfo = deviceInfo
        )

        // Store the token in Firestore
        firestore.collection("user_fcm_tokens")
            .document(token) // Use token as document ID for easy lookup
            .set(userToken)
            .get()

        userToken
    }

    suspend fun getTokensForUser(userId: String): List<UserFCMToken> = withContext(Dispatchers.IO) {
        firestore.collection("user_fcm_tokens")
            .whereEqualTo("userId", userId)
            .get()
            .get()
            .documents
            .mapNotNull { doc ->
                doc.toObject(UserFCMToken::class.java)
            }
    }

    suspend fun deleteToken(token: String): Boolean = withContext(Dispatchers.IO) {
        firestore.collection("user_fcm_tokens")
            .document(token)
            .delete()
            .get()
        true
    }

    suspend fun deleteTokensForUser(userId: String): Boolean = withContext(Dispatchers.IO) {
        val batch = firestore.batch()
        val tokens = getTokensForUser(userId)

        tokens.forEach { token ->
            val docRef = firestore.collection("user_fcm_tokens").document(token.token)
            batch.delete(docRef)
        }

        batch.commit().get()
        true
    }

    suspend fun updateToken(userId: String, oldToken: String, newToken: String, deviceInfo: String? = null): UserFCMToken = withContext(Dispatchers.IO) {
        // Delete old token
        deleteToken(oldToken)

        // Register new token
        registerToken(userId, newToken, deviceInfo)
    }
}