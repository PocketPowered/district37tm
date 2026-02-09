package com.district37.toastmasters.features.create.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.graphql.type.CreateEventInput
import com.district37.toastmasters.graphql.type.EntityType
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.Venue
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import com.district37.toastmasters.util.WizardImageHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * ViewModel for the Create Event Wizard flow.
 * Manages step navigation and form data collection across multiple steps.
 */
class CreateEventWizardViewModel(
    private val eventRepository: EventRepository,
    private val venueRepository: VenueRepository,
    private val wizardImageHandler: WizardImageHandler
) : ViewModel() {

    companion object {
        private const val TAG = "CreateEventWizardViewModel"
        const val TOTAL_STEPS = 6
    }

    // Step tracking
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    val totalSteps = TOTAL_STEPS

    // Step 1: Event Name
    private val _eventName = MutableStateFlow("")
    val eventName: StateFlow<String> = _eventName.asStateFlow()

    // Step 2: Description
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    // Step 3: Event Type
    private val _eventType = MutableStateFlow<EventType?>(null)
    val eventType: StateFlow<EventType?> = _eventType.asStateFlow()
    val availableEventTypes: List<EventType> = EventType.knownEntries

    // Date fields
    private val _startDate = MutableStateFlow<Instant?>(Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()))
    val startDate: StateFlow<Instant?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Instant?>(Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()))
    val endDate: StateFlow<Instant?> = _endDate.asStateFlow()

    // Step 4: Venue
    private val _selectedVenue = MutableStateFlow<Venue?>(null)
    val selectedVenue: StateFlow<Venue?> = _selectedVenue.asStateFlow()

    private val _venueSearchQuery = MutableStateFlow("")
    val venueSearchQuery: StateFlow<String> = _venueSearchQuery.asStateFlow()

    private val _venueSearchResults = MutableStateFlow<Resource<List<Venue>>>(Resource.NotLoading)
    val venueSearchResults: StateFlow<Resource<List<Venue>>> = _venueSearchResults.asStateFlow()

    // Step 5: Image (optional) - gallery only with focus region
    private val _selectedImageBytes = MutableStateFlow<ByteArray?>(null)
    val selectedImageBytes: StateFlow<ByteArray?> = _selectedImageBytes.asStateFlow()

    private val _imageFocusRegion = MutableStateFlow<com.district37.toastmasters.models.FocusRegion?>(null)

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    // Submission state
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _creationResult = MutableStateFlow<Resource<Event>?>(null)
    val creationResult: StateFlow<Resource<Event>?> = _creationResult.asStateFlow()

    // Derived state for whether user can proceed to next step
    val canProceed: StateFlow<Boolean> = combine(
        _currentStep,
        _eventName,
        _startDate,
        _endDate,
        _selectedVenue
    ) { step, name, startDate, endDate, venue ->
        when (step) {
            0 -> name.trim().length >= 3 // Step 1: Name required (at least 3 characters)
            1 -> true // Step 2: Description is optional
            2 -> true // Step 3: Event type is optional
            3 -> startDate != null && endDate != null && endDate >= startDate // Step 4: Dates required and valid
            4 -> venue != null // Step 5: Venue required
            5 -> true // Step 6: Image URL is optional
            else -> false
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Load initial venues when ViewModel is created
        loadVenues()
    }

    // Navigation
    fun goToNextStep() {
        if (_currentStep.value < totalSteps - 1) {
            _currentStep.update { it + 1 }
        }
    }

    /**
     * Go to previous step.
     * @return false if already at first step (caller should handle exit)
     */
    fun goToPreviousStep(): Boolean {
        return if (_currentStep.value > 0) {
            _currentStep.update { it - 1 }
            true
        } else {
            false
        }
    }

    // Step 1: Name
    fun updateName(name: String) {
        _eventName.update { name }
    }

    // Step 2: Description
    fun updateDescription(desc: String) {
        _description.update { desc }
    }

    // Step 3: Event Type
    fun selectEventType(type: EventType?) {
        _eventType.update { type }
    }

    // Date updates
    fun updateStartDate(date: Instant?) {
        _startDate.update { date }

        // Auto-fill end date when start date is selected and end date is empty
        if (date != null && _endDate.value == null) {
            _endDate.update { date }
        }
    }

    fun updateEndDate(date: Instant?) {
        _endDate.update { date }
    }

    // Step 4: Venue
    fun selectVenue(venue: Venue) {
        _selectedVenue.update { venue }
    }

    fun clearVenue() {
        _selectedVenue.update { null }
    }

    fun updateVenueSearchQuery(query: String) {
        _venueSearchQuery.update { query }
        searchVenues(query)
    }

    // Step 5: Image
    fun setImageSelection(result: com.district37.toastmasters.models.ImageSelectionResult) {
        _selectedImageBytes.update { result.imageBytes }
        _imageFocusRegion.update { result.focusRegion }
    }

    fun clearImageSelection() {
        _selectedImageBytes.update { null }
        _imageFocusRegion.update { null }
    }

    private fun loadVenues() {
        viewModelScope.launch {
            _venueSearchResults.update { Resource.Loading }
            val result = venueRepository.searchVenues(
                searchQuery = null,
                first = 50
            )
            _venueSearchResults.update {
                when (result) {
                    is Resource.Success -> Resource.Success(result.data.items)
                    is Resource.Error -> Resource.Error(result.errorType, result.message ?: "Failed to load venues")
                    is Resource.Loading -> Resource.Loading
                    is Resource.NotLoading -> Resource.NotLoading
                }
            }
        }
    }

    private fun searchVenues(query: String) {
        viewModelScope.launch {
            _venueSearchResults.update { Resource.Loading }
            val result = venueRepository.searchVenues(
                searchQuery = query.takeIf { it.isNotBlank() },
                first = 50
            )
            _venueSearchResults.update {
                when (result) {
                    is Resource.Success -> Resource.Success(result.data.items)
                    is Resource.Error -> Resource.Error(result.errorType, result.message ?: "Failed to search venues")
                    is Resource.Loading -> Resource.Loading
                    is Resource.NotLoading -> Resource.NotLoading
                }
            }
        }
    }

    /**
     * Refresh venues list (e.g., after creating a new venue)
     */
    fun refreshVenues() {
        val query = _venueSearchQuery.value
        if (query.isNotBlank()) {
            searchVenues(query)
        } else {
            loadVenues()
        }
    }

    /**
     * Load and select a venue by ID (used when returning from nested venue creation)
     */
    fun loadAndSelectVenue(venueId: Int) {
        viewModelScope.launch {
            val result = venueRepository.getVenue(venueId)
            if (result is Resource.Success) {
                selectVenue(result.data)
                // Also refresh the venue list to include the new venue
                refreshVenues()
            } else {
                Logger.e(TAG, "Failed to load venue $venueId")
            }
        }
    }

    // Submission
    fun createEvent() {
        if (!canProceed.value) return

        val venue = _selectedVenue.value ?: return

        viewModelScope.launch {
            _isSubmitting.update { true }
            _creationResult.update { Resource.Loading }

            val input = CreateEventInput(
                name = _eventName.value.trim(),
                description = Optional.presentIfNotNull(
                    _description.value.trim().takeIf { it.isNotBlank() }
                ),
                eventType = Optional.presentIfNotNull(_eventType.value),
                startDate = _startDate.value ?: Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()),
                endDate = _endDate.value ?: Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()),
                venueId = venue.id
            )

            val result = eventRepository.createEvent(input)

            if (result is Resource.Success) {
                Logger.d(TAG, "Event created successfully: ${result.data.id}")

                // Handle image via WizardImageHandler
                wizardImageHandler.handleImageUploadAndRecord(
                    imageBytes = _selectedImageBytes.value,
                    focusRegion = _imageFocusRegion.value,
                    entityId = result.data.id,
                    entityType = EntityType.EVENT,
                    entityTypeString = "event",
                    altText = _eventName.value.trim(),
                    tag = TAG
                )
            } else if (result is Resource.Error) {
                Logger.e(TAG, "Failed to create event: ${result.message}")
            }

            _creationResult.update { result }
            _isSubmitting.update { false }
        }
    }

    /**
     * Reset the creation result (e.g., after handling error)
     */
    fun resetCreationResult() {
        _creationResult.update { null }
    }
}
