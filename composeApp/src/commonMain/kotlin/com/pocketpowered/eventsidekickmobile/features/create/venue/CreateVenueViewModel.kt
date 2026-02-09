package com.district37.toastmasters.features.create.venue

import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.graphql.type.CreateVenueInput
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseFormViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for creating a new venue
 */
class CreateVenueViewModel(
    private val venueRepository: VenueRepository
) : BaseFormViewModel<Venue>() {

    override val tag = "CreateVenueViewModel"

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    private val _zipCode = MutableStateFlow("")
    val zipCode: StateFlow<String> = _zipCode.asStateFlow()

    private val _capacity = MutableStateFlow("")
    val capacity: StateFlow<String> = _capacity.asStateFlow()

    // Update functions
    fun updateName(value: String) {
        _name.update { value }
        clearFieldError("name")
    }

    fun updateAddress(value: String) {
        _address.update { value }
        clearFieldError("address")
    }

    fun updateCity(value: String) {
        _city.update { value }
        clearFieldError("city")
    }

    fun updateState(value: String) {
        _state.update { value }
        clearFieldError("state")
    }

    fun updateZipCode(value: String) {
        _zipCode.update { value }
        clearFieldError("zipCode")
    }

    fun updateCapacity(value: String) {
        // Only allow digits
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _capacity.update { value }
        }
    }

    override fun validate(): Boolean {
        var isValid = true

        if (_name.value.isBlank()) {
            setFieldError("name", "Venue name is required")
            isValid = false
        }

        if (_address.value.isBlank()) {
            setFieldError("address", "Address is required")
            isValid = false
        }

        if (_city.value.isBlank()) {
            setFieldError("city", "City is required")
            isValid = false
        }

        if (_state.value.isBlank()) {
            setFieldError("state", "State is required")
            isValid = false
        }

        if (_zipCode.value.isBlank()) {
            setFieldError("zipCode", "Zip code is required")
            isValid = false
        }

        return isValid
    }

    override suspend fun submitForm(): Resource<Venue> {
        val input = CreateVenueInput(
            name = _name.value.trim(),
            address = Optional.presentIfNotNull(_address.value.trim().takeIf { it.isNotBlank() }),
            city = Optional.presentIfNotNull(_city.value.trim().takeIf { it.isNotBlank() }),
            state = Optional.presentIfNotNull(_state.value.trim().takeIf { it.isNotBlank() }),
            zipCode = Optional.presentIfNotNull(_zipCode.value.trim().takeIf { it.isNotBlank() }),
            capacity = Optional.presentIfNotNull(_capacity.value.toIntOrNull())
        )

        return venueRepository.createVenue(input)
    }
}
