package com.district37.toastmasters

import com.district37.toastmasters.di.persistentModule
import com.district37.toastmasters.di.requestModule
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.scope

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::main)
        .start(wait = true)
}

fun Application.main() {
    install(Koin) {
        modules(requestModule, persistentModule)
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(RequestContextPlugin)
    eventsModule()
}

val RequestContextPlugin = createApplicationPlugin(name = "RequestContextPlugin") {
    onCall { call ->
        val requestContextProvider = call.scope.get<RequestContextProviderImpl>()
        requestContextProvider.setContext(call)
    }
}