package com.district37.toastmasters

import com.district37.toastmasters.models.AgendaItem
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.ExternalLink
import com.wongislandd.nexus.networking.NetworkClient
import com.wongislandd.nexus.util.Resource
import io.ktor.client.HttpClient
import kotlinx.datetime.Clock

class EventRepository(okHttpClient: HttpClient) : NetworkClient(okHttpClient) {

    suspend fun getEventDetails(id: Int): Resource<Event> {
        return Resource.Success(
            FakeEventProvider.events[id] ?: throw IllegalArgumentException("Event not found")
        )
    }

    suspend fun getEvents(): Resource<List<EventPreview>> {
        return Resource.Success(FakeEventProvider.eventPreviews)
    }
}

object FakeEventProvider {

    private val eventsSource = listOf(
        Event(
            id = 1,
            title = "District 37 Conference in NY",
            description = "An annual conference featuring keynotes, panels, and networking opportunities.",
            images = listOf(
                "https://vastphotos.com/files/uploads/photos/10401/high-resolution-new-york-city-skyline-vast-xl.jpg?v=20220712043521",
                "https://example.com/event1/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds,
            locationInfo = "New York City, NY",
            agenda = listOf(
                AgendaItem(
                    title = "Opening Ceremony",
                    description = "Welcome speech and introduction to the event.",
                    time = Clock.System.now().epochSeconds + 3600,
                    locationInfo = "Main Hall"
                ),
                AgendaItem(
                    title = "Keynote Speech",
                    description = "A talk on the future of technology.",
                    time = Clock.System.now().epochSeconds + 7200,
                    locationInfo = "Conference Room A"
                )
            ),
            additionalLinks = listOf(
                ExternalLink(displayName = "Event Website", url = "https://example.com/event1"),
                ExternalLink(
                    displayName = "Register Here",
                    url = "https://example.com/event1/register"
                )
            )
        ),
        Event(
            id = 2,
            title = "District 37 Conference in CA",
            description = "The West Coast edition of the District 37 Conference, with exciting sessions and networking.",
            images = listOf(
                "https://griffithobservatory.org/wp-content/uploads/2021/01/olenka-kotyk-9TUkYXQKXec-unsplash-1200x1200.jpg",
                "https://example.com/event2/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds + 86400,
            locationInfo = "Los Angeles, CA",
            agenda = listOf(
                AgendaItem(
                    title = "Networking Session",
                    description = "An opportunity to connect with industry professionals.",
                    time = Clock.System.now().epochSeconds + 90000,
                    locationInfo = "Lobby Area"
                ),
                AgendaItem(
                    title = "Panel Discussion",
                    description = "Experts discuss trends in AI and Machine Learning.",
                    time = Clock.System.now().epochSeconds + 93600,
                    locationInfo = "Main Auditorium"
                )
            ),
            additionalLinks = listOf(
                ExternalLink(
                    displayName = "Speaker List",
                    url = "https://example.com/event2/speakers"
                ),
                ExternalLink(displayName = "Live Stream", url = "https://example.com/event2/live")
            )
        ),
        Event(
            id = 3,
            title = "District 37 Conference in SF",
            description = "Join us in San Francisco for workshops, panels, and hands-on experiences in tech.",
            images = listOf(
                "https://www.innsf.com/wp-content/uploads/sites/20/GettyImages-1348089637.jpg",
                "https://example.com/event3/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds + 172800,
            locationInfo = "San Francisco, CA",
            agenda = listOf(
                AgendaItem(
                    title = "Workshop: Kotlin for Android",
                    description = "A hands-on coding session on modern Android development.",
                    time = Clock.System.now().epochSeconds + 175200,
                    locationInfo = "Tech Hub Room B"
                ),
                AgendaItem(
                    title = "Closing Remarks",
                    description = "A summary of the event and future initiatives.",
                    time = Clock.System.now().epochSeconds + 180000,
                    locationInfo = "Grand Hall"
                )
            ),
            additionalLinks = listOf(
                ExternalLink(displayName = "Kotlin Docs", url = "https://kotlinlang.org/docs/"),
                ExternalLink(displayName = "Event Recap", url = "https://example.com/event3/recap")
            )
        )
    )

    val events = eventsSource.associateBy { it.id }

    val eventPreviews = eventsSource.map {
        EventPreview(
            id = it.id,
            title = it.title,
            image = it.images.firstOrNull() ?: "no url in mock",
            time = it.time,
            locationInfo = it.locationInfo
        )
    }
}