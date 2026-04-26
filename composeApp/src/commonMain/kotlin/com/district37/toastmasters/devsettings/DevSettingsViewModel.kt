package com.district37.toastmasters.devsettings

import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.database.DevSettingsRepository
import com.district37.toastmasters.graphql.AllConferencesQuery
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.ErrorType
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.SliceableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConferenceOption(
    val id: Long,
    val name: String,
    val isProductionActive: Boolean,
    val startDate: String?,
    val endDate: String?
)

data class DevSettingsState(
    val conferences: List<ConferenceOption> = emptyList(),
    val overrideId: Long? = null
)

class DevSettingsViewModel(
    private val apolloClient: ApolloClient,
    private val devSettingsRepository: DevSettingsRepository,
    uiEventBus: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEventBus, backChannelEventBus) {

    private val _state = MutableStateFlow<Resource<DevSettingsState>>(Resource.Loading)
    val state: StateFlow<Resource<DevSettingsState>> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { Resource.Loading }
            try {
                val response = apolloClient.query(AllConferencesQuery()).execute()
                if (response.hasErrors()) {
                    _state.update { Resource.Error(ErrorType.CLIENT_ERROR) }
                    return@launch
                }

                val overrideId = devSettingsRepository.getConferenceOverrideId()
                val conferences = response.data?.conferencesCollection?.edges
                    ?.map { it.node }
                    ?.map { node ->
                        ConferenceOption(
                            id = node.id,
                            name = node.name,
                            isProductionActive = node.is_active ?: false,
                            startDate = node.start_date?.toString(),
                            endDate = node.end_date?.toString()
                        )
                    }
                    ?: emptyList()

                _state.update { Resource.Success(DevSettingsState(conferences, overrideId)) }
            } catch (e: Exception) {
                _state.update { Resource.Error(ErrorType.UNKNOWN, e) }
            }
        }
    }

    fun setOverride(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            devSettingsRepository.setConferenceOverrideId(id)
            _state.update { current ->
                if (current is Resource.Success) {
                    Resource.Success(current.data.copy(overrideId = id))
                } else current
            }
        }
    }

    fun clearOverride() {
        viewModelScope.launch(Dispatchers.IO) {
            devSettingsRepository.clearConferenceOverrideId()
            _state.update { current ->
                if (current is Resource.Success) {
                    Resource.Success(current.data.copy(overrideId = null))
                } else current
            }
        }
    }

    fun onRefresh() {
        load()
    }
}
