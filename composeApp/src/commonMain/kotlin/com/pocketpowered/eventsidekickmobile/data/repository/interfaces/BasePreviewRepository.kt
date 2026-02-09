package com.district37.toastmasters.data.repository.interfaces

import com.district37.toastmasters.util.Resource

/**
 * Base interface for repositories that provide preview item fetching
 * T: The type of item being fetched
 */
interface BasePreviewRepository<T> {
    suspend fun getPreview(id: Int): Resource<T>
}