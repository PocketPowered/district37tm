package com.district37.toastmasters

import com.district37.toastmasters.di.util.eventsByDate
import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.BackendTabInfo
import io.ktor.server.plugins.NotFoundException

class EventService {

    fun getEvent(id: Int): BackendEventDetails {
        return MockEventDataProvider.allEvents[id] ?: throw NotFoundException("Event not found")
    }

    fun getEventPreviews(dateKey: String?): List<BackendEventPreview> {
        if (dateKey == null) {
            return MockEventDataProvider.allEventPreviews
        }
        return MockEventDataProvider.eventPreviewsByDate[dateKey]
            ?: throw NotFoundException("Event key not found")
    }

    fun getAvailableTabsInfo(): List<BackendTabInfo> {
        return listOf(
            BackendTabInfo("May 2nd, 2025", "050225"),
            BackendTabInfo("May 3rd, 2025", "050325")
        )
    }
}



object MockEventDataProvider {

    val allEvents = eventsByDate.values.flatten()

    val eventPreviewsByDate: Map<String, List<BackendEventPreview>> =
        eventsByDate.mapValues { (_, value) ->
            value.map {
                BackendEventPreview(
                    id = it.id,
                    title = it.title,
                    image = it.images.firstOrNull() ?: "no url in mock",
                    time = it.time,
                    locationInfo = it.locationInfo
                )
            }
        }

    // Map:
    // "01523" to BackendEventDetails ------> "01523" to BackendEventPreview

    val allEventPreviews = eventPreviewsByDate.values.flatten()
}