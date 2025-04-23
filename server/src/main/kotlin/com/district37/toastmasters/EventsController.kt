package com.district37.toastmasters

import com.district37.toastmasters.models.BackendEventDetails
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.eventsController() {
    val eventService: EventService by inject()

    routing {
        // Create new event
        post("/events") {
            try {
                val event = call.receive<BackendEventDetails>()
                val createdEvent = eventService.createEvent(event)
                call.respond(HttpStatusCode.Created, createdEvent)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid event data: ${e.message}")
            }
        }

        // Get all events
        get("/events/all") {
            call.respond(eventService.getAllEvents())
        }

        // Get single event
        get("/event/{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            call.respond(eventService.getEvent(id))
        }

        // Get events by date
        get("/events") {
            val date = call.request.queryParameters["date"]
            call.respond(eventService.getEventPreviews(date))
        }

        // Get favorite events
        get("/events/favorites") {
            val idsParam = call.request.queryParameters["ids"] ?: ""
            val ids = idsParam.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it > 0 }
            call.respond(eventService.getEventsByIds(ids))
        }

        // Get available tabs
        get("/availableTabs") {
            call.respond(eventService.getAvailableTabsInfo())
        }

        // Update single event
        put("/event/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            val event = call.receive<BackendEventDetails>()
            if (event.id != id) {
                throw IllegalArgumentException("Event ID in path does not match event ID in body")
            }
            call.respond(eventService.updateEvent(event))
        }

        // Update single event partially
        patch("/event/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            val update = call.receive<BackendEventDetails>()
            if (update.id != id) {
                throw IllegalArgumentException("Event ID in path does not match event ID in body")
            }
            call.respond(eventService.updateEventPartial(update))
        }

        // Update multiple events
        put("/events") {
            val events = call.receive<List<BackendEventDetails>>()
            call.respond(eventService.updateEvents(events))
        }

        // Update multiple events partially
        patch("/events") {
            val updates = call.receive<List<BackendEventDetails>>()
            call.respond(eventService.updateEventsPartial(updates))
        }

        // Delete single event
        delete("/event/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            val success = eventService.deleteEvent(id)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        // Delete multiple events
        delete("/events") {
            val ids = call.receive<List<Int>>()
            val success = eventService.deleteEvents(ids)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}