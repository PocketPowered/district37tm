package com.district37.toastmasters

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.eventsController() {
    val eventService : EventService by inject()

    routing {
        get("/event/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            call.respond(eventService.getEvent(id))
        }

        get("/events/{isFriday}") {
            val isFriday = call.parameters["isFriday"]?.toBoolean() ?: throw IllegalArgumentException("Invalid ID")
            call.respond(eventService.getEventPreviews(isFriday))
        }
    }
}