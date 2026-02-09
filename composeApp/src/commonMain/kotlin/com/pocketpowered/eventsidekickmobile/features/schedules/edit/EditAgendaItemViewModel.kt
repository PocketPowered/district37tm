package com.district37.toastmasters.features.schedules.edit

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.graphql.type.UpdateAgendaItemInput
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
 * ViewModel for editing an existing agenda item
 */
class EditAgendaItemViewModel(
    private val agendaItemRepository: AgendaItemRepository,
    private val agendaItemId: Int
) : BaseFormViewModel<AgendaItem>() {

    override val tag = "EditAgendaItemViewModel"

    // Loading state for initial data fetch
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

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

    // Available tags for selection
    val availableTags: List<AgendaItemTag?> = listOf(null) + AgendaItemTag.entries

    // Derived states (issues #12, #14)
    val selectedTagFormatted: StateFlow<String> = _selectedTag.map { tag ->
        DisplayFormatters.formatTagName(tag)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val canSubmit: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        _title,
        _startTime,
        _endTime
    ) { title, startTime, endTime ->
        title.isNotBlank() && startTime != null && endTime != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadAgendaItem()
    }

    private fun loadAgendaItem() {
        viewModelScope.launch {
            _isLoading.value = true
            _loadError.value = null

            when (val result = agendaItemRepository.getAgendaItem(agendaItemId)) {
                is Resource.Success -> {
                    val item = result.data
                    _title.value = item.title
                    _description.value = item.description ?: ""
                    _selectedTag.value = item.tag
                    _startTime.value = item.startTime?.instant
                    _endTime.value = item.endTime?.instant
                    _performers.value = item.performers
                    _selectedLocation.value = item.location
                    // Get venue ID and event date range from the event
                    val event = item.event
                    _venueId.value = event?.venueId
                    _eventStartDate.value = event?.startDate?.instant
                    _eventEndDate.value = event?.endDate?.instant
                    Logger.d(tag, "Loaded agenda item: ${item.title}, venueId: ${_venueId.value}, eventRange: ${_eventStartDate.value} - ${_eventEndDate.value}")
                }
                is Resource.Error -> {
                    _loadError.value = result.message ?: "Failed to load agenda item"
                    Logger.e(tag, "Failed to load agenda item: ${result.message}")
                }
                else -> {}
            }

            _isLoading.value = false
        }
    }

    fun retry() {
        loadAgendaItem()
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

        return isValid
    }

    override suspend fun submitForm(): Resource<AgendaItem> {
        // Convert models.AgendaItemTag to graphql.type.AgendaItemTag
        val graphqlTag = _selectedTag.value?.let { tag ->
            GraphQLAgendaItemTag.safeValueOf(tag.name)
        }

        val input = UpdateAgendaItemInput(
            title = Optional.presentIfNotNull(_title.value.trim().takeIf { it.isNotBlank() }),
            description = Optional.presentIfNotNull(_description.value.trim().takeIf { it.isNotBlank() }),
            tag = Optional.presentIfNotNull(graphqlTag),
            startTime = Optional.presentIfNotNull(_startTime.value),
            endTime = Optional.presentIfNotNull(_endTime.value),
            performerIds = Optional.present(_performers.value.map { it.id }),
            locationId = Optional.present(_selectedLocation.value?.id)
        )

        return agendaItemRepository.updateAgendaItem(agendaItemId, input)
    }
}
