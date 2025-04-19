package com.wongislandd.nexus.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val PROD_HOST = "https://christopher-wong.com/server"
private const val DEV_HOST = "http://localhost:8080"
// Adjust this to true to hit local server, never commit with this as true
private const val useDevHost = false

private val HOST = if (useDevHost) DEV_HOST else PROD_HOST

fun createHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        defaultRequest {
            url(HOST)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.withTag("Network").d {
                        message
                    }
                }
            }
            level = LogLevel.BODY
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
        install(HttpCache)
        install(ContentNegotiation) {
            json(
                json = Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }
    }
}
