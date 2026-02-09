package com.district37.toastmasters.features.create.picker

import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BasePaginatedSearchViewModel

/**
 * ViewModel for the performer picker screen
 * Provides search and pagination functionality for selecting performers
 *
 * Refactored to extend BasePaginatedSearchViewModel to eliminate duplicate
 * search and pagination logic.
 */
class PerformerPickerViewModel(
    private val performerRepository: PerformerRepository
) : BasePaginatedSearchViewModel<Performer>() {

    override val tag = "PerformerPickerViewModel"

    /**
     * Implement search operation by calling repository
     */
    override suspend fun performSearchOperation(
        query: String?,
        cursor: String?
    ): Resource<PagedConnection<Performer>> {
        return performerRepository.searchPerformers(
            searchQuery = query,
            cursor = cursor,
            first = 20
        )
    }
}
