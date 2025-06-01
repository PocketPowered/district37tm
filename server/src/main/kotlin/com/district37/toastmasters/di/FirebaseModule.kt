package com.district37.toastmasters.di

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import org.koin.dsl.module

val firebaseModule = module {
    single<Firestore> {
        val json = System.getenv("GOOGLE_CREDENTIALS_JSON")
            ?: error("Missing GOOGLE_CREDENTIALS_JSON env variable")
        val serviceAccount = GoogleCredentials.fromStream(json.byteInputStream())

        FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId("district37-toastmasters")
            .setCredentials(serviceAccount)
            .build()
            .service
    }

    single<GoogleCredentials> {
        val json = System.getenv("GOOGLE_CREDENTIALS_JSON")
            ?: error("Missing GOOGLE_CREDENTIALS_JSON env variable")
        GoogleCredentials.fromStream(json.byteInputStream())
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
    }
} 