package com.district37.toastmasters

import com.district37.toastmasters.models.BackendExternalLink
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.referencesController() {
    val referencesService: ReferencesService by inject()

    routing {
        route("/references") {
            get {
                try {
                    val references = referencesService.getAllReferences()
                    call.respond(references)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            post {
                try {
                    val reference = call.receive<BackendExternalLink>()
                    val createdReference = referencesService.createReference(reference)
                    call.respond(HttpStatusCode.Created, createdReference)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing reference ID")
                    val reference = call.receive<BackendExternalLink>()
                    val updatedReference = referencesService.updateReference(id, reference)
                    call.respond(updatedReference)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing reference ID")
                    referencesService.deleteReference(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }
    }
} 