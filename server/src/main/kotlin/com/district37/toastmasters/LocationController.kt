package com.district37.toastmasters

import com.district37.toastmasters.models.Location
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.locationController() {
    val locationService: FirebaseLocationService by inject()

    routing {
        route("/locations") {
            get {
                try {
                    val locations = locationService.getAllLocations()
                    call.respond(locations)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            get("/search") {
                try {
                    val query = call.request.queryParameters["q"]
                        ?: throw IllegalArgumentException("Missing search query parameter 'q'")
                    val locations = locationService.searchLocationsByName(query)
                    call.respond(locations)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            post {
                try {
                    val location = call.receive<Location>()
                    val createdLocation = locationService.createLocation(location)
                    call.respond(HttpStatusCode.Created, createdLocation)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Missing location ID")
                    val location = call.receive<Location>()
                    val updatedLocation = locationService.updateLocation(id, location)
                    call.respond(updatedLocation)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Missing location ID")
                    locationService.deleteLocation(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }
    }
} 