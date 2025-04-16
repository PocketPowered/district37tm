package com.district37.toastmasters

import com.district37.toastmasters.models.BackendAgendaItem
import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.BackendExternalLink
import io.ktor.server.plugins.NotFoundException
import kotlinx.datetime.Clock

class EventService {

    fun getEvent(id: Int): BackendEventDetails {
        return MockEventDataProvider.events[id] ?: throw NotFoundException("Event not found")
    }

    fun getEventPreviews(): List<BackendEventPreview> {
        return MockEventDataProvider.eventPreviews
    }
}

object MockEventDataProvider {

    private val eventsSource = listOf(
        BackendEventDetails(
            id = 1,
            title = "First Timers",
            description = "If you are attending for the first time, this is for you",
            images = listOf(
                "https://d37toastmasters.org/wp-content/uploads/2025/04/2025-D37-Conference-Events_4-1-2025_Promos.pdf",
                "https://example.com/event1/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds,
            locationInfo = "Charlotte NC",
            agenda = listOf(
                BackendAgendaItem(
                    title = "Opening Ceremony",
                    description = "Welcome speech and introduction to the event.",
                    time = Clock.System.now().epochSeconds + 3600,
                    locationInfo = "Main Hall"
                ),
                BackendAgendaItem(
                    title = "Keynote Speech",
                    description = "A talk on the future of technology.",
                    time = Clock.System.now().epochSeconds + 7200,
                    locationInfo = "Conference Room A"
                )
            ),
            additionalLinks = listOf(
                BackendExternalLink(
                    displayName = "Event Website",
                    url = "https://example.com/event1"
                ),
                BackendExternalLink(
                    displayName = "Register Here",
                    url = "https://example.com/event1/register"
                )
            )
        ),
        BackendEventDetails(
            id = 2,
            title = "International Speech Contest",
            description = "Watch the amazing speakers compete for ISC",
            images = listOf(
                "https://d100tm.org/wp-content/uploads/2023/10/100-years-sparkle-district-100.png",
                "https://example.com/event2/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds + 86400,
            locationInfo = "Los Angeles, CA",
            agenda = listOf(
                BackendAgendaItem(
                    title = "Networking Session",
                    description = "An opportunity to connect with industry professionals.",
                    time = Clock.System.now().epochSeconds + 90000,
                    locationInfo = "Lobby Area"
                ),
                BackendAgendaItem(
                    title = "Panel Discussion",
                    description = "Experts discuss trends in AI and Machine Learning.",
                    time = Clock.System.now().epochSeconds + 93600,
                    locationInfo = "Main Auditorium"
                )
            ),
            additionalLinks = listOf(
                BackendExternalLink(
                    displayName = "Speaker List",
                    url = "https://example.com/event2/speakers"
                ),
                BackendExternalLink(
                    displayName = "Live Stream",
                    url = "https://example.com/event2/live"
                )
            )
        ),
        BackendEventDetails(
            id = 3,
            title = "District 37 Evaluation contest",
            description = "Join us in San Francisco for workshops, panels, and hands-on experiences in tech.",
            images = listOf(
                "https://d100tm.org/wp-content/uploads/2023/10/100-years-sparkle-district-100.png",
                "https://example.com/event3/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds + 172800,
            locationInfo = "San Francisco, CA",
            agenda = listOf(
                BackendAgendaItem(
                    title = "Workshop: Kotlin for Android",
                    description = "A hands-on coding session on modern Android development.",
                    time = Clock.System.now().epochSeconds + 175200,
                    locationInfo = "Tech Hub Room B"
                ),
                BackendAgendaItem(
                    title = "Closing Remarks",
                    description = "A summary of the event and future initiatives.",
                    time = Clock.System.now().epochSeconds + 180000,
                    locationInfo = "Grand Hall"
                )
            ),
            additionalLinks = listOf(
                BackendExternalLink(
                    displayName = "Kotlin Docs",
                    url = "https://kotlinlang.org/docs/"
                ),
                BackendExternalLink(
                    displayName = "Event Recap",
                    url = "https://example.com/event3/recap"
                )
            )
        )
    )

    val events = eventsSource.associateBy { it.id }

    val eventPreviews = eventsSource.map {
        BackendEventPreview(
            id = it.id,
            title = it.title,
            image = it.images.firstOrNull() ?: "no url in mock",
            time = it.time,
            locationInfo = it.locationInfo
        )
    }
}