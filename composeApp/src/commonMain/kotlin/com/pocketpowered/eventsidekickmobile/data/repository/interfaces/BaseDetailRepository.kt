package com.district37.toastmasters.data.repository.interfaces

import com.district37.toastmasters.util.Resource

/**
 * Interface for repositories that provide detail (full) information for entities
 *
 * This interface defines a standard contract for loading complete entity data
 * with all relationships and details included
 *
 * @param T The type of entity this repository provides details for
 */
interface BaseDetailRepository<T> {
    /**
     * Get complete details for an entity by ID
     *
     * @param id The unique identifier of the entity
     * @return Resource wrapper containing the entity or error state
     */
    suspend fun getDetails(id: Int): Resource<T>
}
