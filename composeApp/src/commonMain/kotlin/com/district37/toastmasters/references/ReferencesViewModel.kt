package com.district37.toastmasters.references

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.ReferencesRepository
import com.district37.toastmasters.models.BackendExternalLink
import com.wongislandd.nexus.events.BackChannelEvent
import com.wongislandd.nexus.events.EventBus
import com.wongislandd.nexus.events.UiEvent
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.viewmodel.SliceableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReferencesViewModel(
    private val repository: ReferencesRepository,
    uiEvent: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEvent, backChannelEventBus) {

    private val _references =
        MutableStateFlow<Resource<List<BackendExternalLink>>>(Resource.Loading)
    val references: StateFlow<Resource<List<BackendExternalLink>>> = _references.asStateFlow()

    init {
        loadReferences()
    }

    private fun loadReferences() {
        viewModelScope.launch(Dispatchers.IO) {
            _references.update {
                repository.getAllReferences()
            }
        }
    }

    fun onRefresh() {
        loadReferences()
    }
} 