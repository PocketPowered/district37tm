package com.district37.toastmasters

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DateService(private val firebaseService: FirebaseDateService) {

    suspend fun getAvailableDates(): List<Long> = withContext(Dispatchers.IO) {
        firebaseService.getAvailableDates()
    }

    suspend fun addDate(timestamp: Long): Long = withContext(Dispatchers.IO) {
        firebaseService.addDate(timestamp)
    }

    suspend fun removeDate(timestamp: Long): Boolean = withContext(Dispatchers.IO) {
        firebaseService.removeDate(timestamp)
    }
} 