package com.district37.toastmasters

import com.district37.toastmasters.di.firebaseModule
import com.district37.toastmasters.di.persistentModule
import com.district37.toastmasters.di.requestModule
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.scope

fun main() {
    // Start Koin before the server
    startKoin {
        modules(requestModule, persistentModule, firebaseModule)
    }

    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::main)
        .start(wait = true)
}

fun Application.main() {
    install(Koin) {
        modules(requestModule, persistentModule, firebaseModule)
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        })
    }
    install(CORS) {
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowHeaders { true }
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }
    install(RequestContextPlugin)
    eventsController()
    notificationsController()
    userFCMController()
    dateController()
    resourcesController()
    locationController()
}

val RequestContextPlugin = createApplicationPlugin(name = "RequestContextPlugin") {
    onCall { call ->
        val requestContextProvider = call.scope.get<RequestContextProviderImpl>()
        requestContextProvider.setContext(call)
    }
}