package com.district37.toastmasters

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.dateController() {
    val dateService: DateService by inject()

    routing {
        // Get all available dates
        get("/dates") {
            call.respond(dateService.getAvailableDates())
        }

        // Add a new date
        post("/dates") {
            try {
                val timestamp = call.receive<Long>()
                val addedDate = dateService.addDate(timestamp)
                call.respond(HttpStatusCode.Created, addedDate)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid timestamp: ${e.message}")
            }
        }

        // Remove a date
        delete("/dates/{timestamp}") {
            val timestamp = call.parameters["timestamp"]?.toLongOrNull() 
                ?: throw IllegalArgumentException("Invalid timestamp")
            val success = dateService.removeDate(timestamp)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
} 