package com.district37.toastmasters.resources

import androidx.lifecycle.viewModelScope
import com.district37.toastmasters.ResourcesRepository
import com.district37.toastmasters.ResourceWithCategory
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

private val hiddenResourceTypes = setOf("splash")
private val priorityResourceTypes = listOf("general", "first_timer")

class ResourcesViewModel(
    private val repository: ResourcesRepository,
    uiEvent: EventBus<UiEvent>,
    backChannelEventBus: EventBus<BackChannelEvent>
) : SliceableViewModel(uiEvent, backChannelEventBus) {

    private val _resources =
        MutableStateFlow<Resource<List<CategorizedResources>>>(Resource.Loading)
    val resources: StateFlow<Resource<List<CategorizedResources>>> = _resources.asStateFlow()

    init {
        loadResources()
    }

    private fun loadResources() {
        viewModelScope.launch(Dispatchers.IO) {
            _resources.update {
                repository.getAllResources().map { resources ->
                    resources
                        .filterNot { resource -> hiddenResourceTypes.contains(resource.resourceType) }
                        .groupBy { resource -> resource.resourceType }
                        .entries
                        .sortedWith(::compareResourceGroups)
                        .map { (resourceType, resourceLinks) ->
                            CategorizedResources(
                                resourceType = resourceType,
                                title = resourceType.toResourceTypeTitle(),
                                resources = resourceLinks.map(ResourceWithCategory::link)
                            )
                        }
                }
            }
        }
    }

    fun onRefresh() {
        loadResources()
    }
}

private fun compareResourceGroups(
    left: Map.Entry<String, List<ResourceWithCategory>>,
    right: Map.Entry<String, List<ResourceWithCategory>>
): Int {
    val leftPriority = priorityResourceTypes.indexOf(left.key)
    val rightPriority = priorityResourceTypes.indexOf(right.key)

    if (leftPriority >= 0 && rightPriority >= 0) {
        return leftPriority.compareTo(rightPriority)
    }

    if (leftPriority >= 0) return -1
    if (rightPriority >= 0) return 1

    return left.key.toResourceTypeTitle().compareTo(right.key.toResourceTypeTitle())
}

private fun String.toResourceTypeTitle(): String {
    return split('_')
        .asSequence()
        .filter { part -> part.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
}
