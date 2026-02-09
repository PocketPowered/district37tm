package com.district37.toastmasters.models

/**
 * Interface for entities that have permissions
 *
 * This interface allows BaseDetailViewModel to automatically extract
 * permissions from any entity that implements it, eliminating duplicate
 * permissions handling code across detail ViewModels.
 */
interface HasPermissions {
    val permissions: EntityPermissions?
}
