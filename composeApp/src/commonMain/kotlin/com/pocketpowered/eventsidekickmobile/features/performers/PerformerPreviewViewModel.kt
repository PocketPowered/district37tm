package com.district37.toastmasters.features.performers

import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.viewmodel.BasePreviewViewModel

/**
 * ViewModel for performer preview component
 * Fetches performer by ID
 */
class PerformerPreviewViewModel(
    performerRepository: PerformerRepository,
    performerId: Int
) : BasePreviewViewModel<Performer, PerformerRepository>(performerId, performerRepository) {
    override val tag = "PerformerPreviewViewModel"
}
