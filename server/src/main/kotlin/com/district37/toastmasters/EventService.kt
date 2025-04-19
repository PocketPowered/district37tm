package com.district37.toastmasters

import com.district37.toastmasters.di.util.fridayList
import com.district37.toastmasters.di.util.saturdayList
import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import io.ktor.server.plugins.NotFoundException

class EventService {

    fun getEvent(id: Int): BackendEventDetails {
        return MockEventDataProvider.events[id] ?: throw NotFoundException("Event not found")
    }

    fun getEventPreviews(isFriday: Boolean = true): List<BackendEventPreview> {
        return MockEventDataProvider.eventPreviews.filter { it.isDayOne == isFriday }
    }
}

object MockEventDataProvider {

    private val eventsSource = fridayList + saturdayList

    val events = eventsSource.associateBy { it.id }

    val eventPreviews = eventsSource.map {
        BackendEventPreview(
            id = it.id,
            title = it.title,
            image = it.images.firstOrNull() ?: "no url in mock",
            time = it.time,
            isDayOne = it.isDayOne,
            locationInfo = it.locationInfo
        )
    }
}