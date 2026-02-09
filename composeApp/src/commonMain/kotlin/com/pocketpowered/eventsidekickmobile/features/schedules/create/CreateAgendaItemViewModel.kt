package com.district37.toastmasters.features.schedules.create

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.graphql.type.CreateAgendaItemInput
import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.AgendaItemTag
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.models.Performer
import com.district37.toastmasters.graphql.type.AgendaItemTag as GraphQLAgendaItemTag
import com.district37.toastmasters.util.DisplayFormatters
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.viewmodel.BaseFormViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

/**
 * ViewModel for creating a new agenda item
 */
class CreateAgendaItemViewModel(
    private val agendaItemRepository: AgendaItemRepository,
    private val eventRepository: EventRepository,
    private val eventId: Int
) : BaseFormViewModel<AgendaItem>() {

    override val tag = "CreateAgendaItemViewModel"

    // Loading state for initial data fetch (event info for dates)
    private val _isLoadingEvent = MutableStateFlow(true)
    val isLoadingEvent: StateFlow<Boolean> = _isLoadingEvent.asStateFlow()

    // Form fields
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _selectedTag = MutableStateFlow<AgendaItemTag?>(null)
    val selectedTag: StateFlow<AgendaItemTag?> = _selectedTag.asStateFlow()

    // Start and End Time (REQUIRED)
    private val _startTime = MutableStateFlow<Instant?>(null)
    val startTime: StateFlow<Instant?> = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow<Instant?>(null)
    val endTime: StateFlow<Instant?> = _endTime.asStateFlow()

    // Event date range constraints (agenda items must be within this range)
    private val _eventStartDate = MutableStateFlow<Instant?>(null)
    val eventStartDate: StateFlow<Instant?> = _eventStartDate.asStateFlow()

    private val _eventEndDate = MutableStateFlow<Instant?>(null)
    val eventEndDate: StateFlow<Instant?> = _eventEndDate.asStateFlow()

    // Performers
    private val _performers = MutableStateFlow<List<Performer>>(emptyList())
    val performers: StateFlow<List<Performer>> = _performers.asStateFlow()

    // Location
    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation.asStateFlow()

    // Venue ID from the event (for filtering locations)
    private val _venueId = MutableStateFlow<Int?>(null)
    val venueId: StateFlow<Int?> = _venueId.asStateFlow()

    // Check if event has no venue (blocks agenda item creation)
    private val _hasNoVenue = MutableStateFlow(false)
    val hasNoVenue: StateFlow<Boolean> = _hasNoVenue.asStateFlow()

    // Available tags for selection
    val availableTags: List<AgendaItemTag?> = listOf(null) + AgendaItemTag.entries

    // Derived states (issues #11, #13)
    val selectedTagFormatted: StateFlow<String> = _selectedTag.map { tag ->
        DisplayFormatters.formatTagName(tag)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val canSubmit: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        _title,
        _startTime,
        _endTime,
        _selectedLocation
    ) { title, startTime, endTime, location ->
        title.isNotBlank() && startTime != null && endTime != null && location != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadEventInfo()
    }

    private fun loadEventInfo() {
        viewModelScope.launch {
            _isLoadingEvent.value = true
            when (val result = eventRepository.getDetails(eventId)) {
                is Resource.Success -> {
                    val event = result.data
                    _eventStartDate.value = event.startDate?.instant
                    _eventEndDate.value = event.endDate?.instant
                    _venueId.value = event.venueId
                    _hasNoVenue.value = event.venueId == null
                    Logger.d(tag, "Loaded event info, venueId: ${_venueId.value}, eventRange: ${_eventStartDate.value} - ${_eventEndDate.value}")
                }
                is Resource.Error -> {
                    Logger.e(tag, "Failed to load event info: ${result.message}")
                }
                else -> {}
            }
            _isLoadingEvent.value = false
        }
    }

    fun updateTitle(value: String) {
        _title.update { value }
        clearFieldError("title")
    }

    fun updateDescription(value: String) {
        _description.update { value }
    }

    fun updateTag(value: AgendaItemTag?) {
        _selectedTag.update { value }
    }

    fun updateStartTime(value: Instant) {
        _startTime.update { value }
        clearFieldError("startTime")

        // Auto-fill end time when start time is selected and end time is empty
        if (_endTime.value == null) {
            _endTime.update { value }
        }

        // If end time is before start time, clear the end time error to re-validate
        _endTime.value?.let {
            if (it >= value) {
                clearFieldError("endTime")
            }
        }
    }

    fun updateEndTime(value: Instant) {
        _endTime.update { value }
        clearFieldError("endTime")
    }

    fun addPerformer(performer: Performer) {
        _performers.update { current ->
            if (current.any { it.id == performer.id }) {
                current // Already exists
            } else {
                current + performer
            }
        }
    }

    fun addPerformerById(id: Int, name: String) {
        // Create a lightweight performer object with just the essential fields
        val performer = Performer(
            id = id,
            name = name,
            bio = null
        )
        addPerformer(performer)
    }

    fun removePerformer(performer: Performer) {
        _performers.update { current ->
            current.filter { it.id != performer.id }
        }
    }

    fun updateLocation(location: Location?) {
        _selectedLocation.update { location }
        if (location != null) {
            clearFieldError("location")
        }
    }

    fun updateLocationById(id: Int, name: String) {
        // Create a lightweight location object with just the essential fields
        val location = Location(
            id = id,
            name = name,
            description = null,
            venueId = _venueId.value
        )
        updateLocation(location)
    }

    override fun validate(): Boolean {
        var isValid = true

        if (_title.value.isBlank()) {
            setFieldError("title", "Title is required")
            isValid = false
        }

        // Validate start time is set
        val startTimeValue = _startTime.value
        if (startTimeValue == null) {
            setFieldError("startTime", "Start time is required")
            isValid = false
        }

        // Validate end time is set
        val endTimeValue = _endTime.value
        if (endTimeValue == null) {
            setFieldError("endTime", "End time is required")
            isValid = false
        }

        // Validate end time is after start time
        if (startTimeValue != null && endTimeValue != null) {
            if (endTimeValue <= startTimeValue) {
                setFieldError("endTime", "End time must be after start time")
                isValid = false
            }
        }

        // Validate times are within event date range
        val eventStart = _eventStartDate.value
        val eventEnd = _eventEndDate.value

        if (eventStart != null && startTimeValue != null && startTimeValue < eventStart) {
            setFieldError("startTime", "Start time must be within the event dates")
            isValid = false
        }

        if (eventEnd != null && endTimeValue != null && endTimeValue > eventEnd) {
            setFieldError("endTime", "End time must be within the event dates")
            isValid = false
        }

        // Validate location is selected
        if (_selectedLocation.value == null) {
            setFieldError("location", "Location is required")
            isValid = false
        }

        return isValid
    }

    override suspend fun submitForm(): Resource<AgendaItem> {
        // Convert models.AgendaItemTag to graphql.type.AgendaItemTag
        val graphqlTag = _selectedTag.value?.let { tag ->
            GraphQLAgendaItemTag.safeValueOf(tag.name)
        }

        val input = CreateAgendaItemInput(
            eventId = eventId,
            title = _title.value.trim(),
            description = Optional.presentIfNotNull(_description.value.trim().takeIf { it.isNotBlank() }),
            tag = Optional.presentIfNotNull(graphqlTag),
            startTime = Optional.presentIfNotNull(_startTime.value),
            endTime = Optional.presentIfNotNull(_endTime.value),
            performerIds = Optional.present(_performers.value.map { it.id }),
            locationId = Optional.present(_selectedLocation.value?.id)
        )

        return agendaItemRepository.createAgendaItem(input)
    }
}
