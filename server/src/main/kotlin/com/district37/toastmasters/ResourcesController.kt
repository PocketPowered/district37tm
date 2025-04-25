package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink
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

fun Application.resourcesController() {
    val resourcesService: ResourcesService by inject()

    routing {
        route("/resources") {
            get {
                try {
                    val resources = resourcesService.getAllResources()
                    call.respond(resources)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            post {
                try {
                    val resource = call.receive<BackendExternalLink>()
                    val createdResource = resourcesService.createResource(resource)
                    call.respond(HttpStatusCode.Created, createdResource)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Missing resource ID")
                    val resource = call.receive<BackendExternalLink>()
                    val updatedResource = resourcesService.updateResource(id, resource)
                    call.respond(updatedResource)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Missing resource ID")
                    resourcesService.deleteResource(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }

        route("/first-timer-resources") {
            get {
                try {
                    val resources = resourcesService.getAllFirstTimerResources()
                    call.respond(resources)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            post {
                try {
                    val resource = call.receive<BackendExternalLink>()
                    val createdResource = resourcesService.createFirstTimerResource(resource)
                    call.respond(HttpStatusCode.Created, createdResource)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Missing resource ID")
                    val resource = call.receive<BackendExternalLink>()
                    val updatedResource = resourcesService.updateFirstTimerResource(id, resource)
                    call.respond(updatedResource)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"]
                        ?: throw IllegalArgumentException("Missing resource ID")
                    resourcesService.deleteFirstTimerResource(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }
    }
} 