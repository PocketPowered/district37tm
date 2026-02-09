package com.district37.toastmasters.features.create.event

import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.graphql.type.CreateEventInput
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseFormViewModel
import com.district37.toastmasters.viewmodel.validateDateAfter
import com.district37.toastmasters.viewmodel.validateRequired
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for creating a new event
 */
class CreateEventViewModel(
    private val eventRepository: EventRepository,
    private val venueRepository: VenueRepository
) : BaseFormViewModel<Event>() {

    override val tag = "CreateEventViewModel"

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _eventType = MutableStateFlow(EventType.CONFERENCE)
    val eventType: StateFlow<EventType> = _eventType.asStateFlow()

    private val _selectedVenueId = MutableStateFlow<Int?>(null)
    val selectedVenueId: StateFlow<Int?> = _selectedVenueId.asStateFlow()

    private val _selectedVenueName = MutableStateFlow<String?>(null)
    val selectedVenueName: StateFlow<String?> = _selectedVenueName.asStateFlow()

    private val _startDate = MutableStateFlow<Instant?>(Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()))
    val startDate: StateFlow<Instant?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Instant?>(Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()))
    val endDate: StateFlow<Instant?> = _endDate.asStateFlow()

    // Available event types for dropdown
    val eventTypes: List<EventType> = EventType.knownEntries

    // Update functions
    fun updateName(value: String) {
        _name.update { value }
        clearFieldError("name")
    }

    fun updateDescription(value: String) {
        _description.update { value }
    }

    fun updateEventType(value: EventType) {
        _eventType.update { value }
    }

    fun selectVenue(venueId: Int, venueName: String) {
        _selectedVenueId.update { venueId }
        _selectedVenueName.update { venueName }
        clearFieldError("venue")
    }

    fun clearVenue() {
        _selectedVenueId.update { null }
        _selectedVenueName.update { null }
    }

    fun updateStartDate(value: Instant) {
        _startDate.update { value }
        clearFieldError("startDate")
    }

    fun updateEndDate(value: Instant) {
        _endDate.update { value }
        clearFieldError("endDate")
    }

    /**
     * Load and select a venue by ID (used when returning from nested creation flow)
     */
    suspend fun loadAndSelectVenue(venueId: Int) {
        val result = venueRepository.getVenue(venueId)
        if (result is Resource.Success) {
            selectVenue(result.data.id, result.data.name)
        } else {
            Logger.e(tag, "Failed to load venue $venueId")
        }
    }

    override fun validate(): Boolean = validateAll(
        validateRequired("name", _name.value, "Event name is required"),
        validateRequired("venue", _selectedVenueId.value, "Venue is required"),
        validateRequired("startDate", _startDate.value, "Start date is required"),
        validateRequired("endDate", _endDate.value, "End date is required"),
        validateDateAfter("endDate", _endDate.value, _startDate.value, "End date must be after start date")
    )

    override suspend fun submitForm(): Resource<Event> {
        val venueId = _selectedVenueId.value
            ?: throw IllegalStateException("Venue is required")
        val startDate = _startDate.value
            ?: throw IllegalStateException("Start date is required")
        val endDate = _endDate.value
            ?: throw IllegalStateException("End date is required")

        val input = CreateEventInput(
            name = _name.value.trim(),
            description = Optional.presentIfNotNull(_description.value.trim().takeIf { it.isNotBlank() }),
            eventType = Optional.present(_eventType.value),
            venueId = venueId,
            startDate = startDate,
            endDate = endDate
        )

        return eventRepository.createEvent(input)
    }
}
