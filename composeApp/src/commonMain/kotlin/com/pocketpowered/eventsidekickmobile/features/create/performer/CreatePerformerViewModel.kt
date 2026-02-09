package com.district37.toastmasters.features.create.performer

import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.graphql.type.CreatePerformerInput
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseFormViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for creating a new performer
 */
class CreatePerformerViewModel(
    private val performerRepository: PerformerRepository
) : BaseFormViewModel<Performer>() {

    override val tag = "CreatePerformerViewModel"

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    private val _performerType = MutableStateFlow("")
    val performerType: StateFlow<String> = _performerType.asStateFlow()

    // Update functions
    fun updateName(value: String) {
        _name.update { value }
        clearFieldError("name")
    }

    fun updateBio(value: String) {
        _bio.update { value }
    }

    fun updatePerformerType(value: String) {
        _performerType.update { value }
    }

    override fun validate(): Boolean {
        var isValid = true

        if (_name.value.isBlank()) {
            setFieldError("name", "Performer name is required")
            isValid = false
        }

        return isValid
    }

    override suspend fun submitForm(): Resource<Performer> {
        val input = CreatePerformerInput(
            name = _name.value.trim(),
            bio = Optional.presentIfNotNull(_bio.value.trim().takeIf { it.isNotBlank() }),
            performerType = Optional.presentIfNotNull(_performerType.value.trim().takeIf { it.isNotBlank() })
        )

        return performerRepository.createPerformer(input)
    }
}
