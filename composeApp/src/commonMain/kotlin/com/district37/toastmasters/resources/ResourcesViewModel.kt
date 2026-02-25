package com.district37.toastmasters.resources

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.ResourcesRepository
import com.district37.toastmasters.models.ExternalLink
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

class ResourcesViewModel(
    private val repository: ResourcesRepository,
    private val resourceLinkTransformer: ResourceLinkTransformer,
    uiEvent: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEvent, backChannelEventBus) {

    private val _resources =
        MutableStateFlow<Resource<List<ExternalLink>>>(Resource.Loading)
    val resources: StateFlow<Resource<List<ExternalLink>>> = _resources.asStateFlow()

    init {
        loadResources()
    }

    private fun loadResources() {
        viewModelScope.launch(Dispatchers.IO) {
            _resources.update {
                repository.getAllResources().map { resources ->
                    resources.mapNotNull { resourceLinkTransformer.transform(it) }
                }
            }
        }
    }

    fun onRefresh() {
        loadResources()
    }
} 
