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
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val PROD_HOST = "https://yarbshxeeufpgquawcuy.supabase.co"
private const val DEV_HOST = "https://yarbshxeeufpgquawcuy.supabase.co"
private const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_pnaLVfP6H6Kxi5wCPhSO2A_aK4zo24t"
private const val useDevHost = false

private val HOST = if (useDevHost) DEV_HOST else PROD_HOST

fun createHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        defaultRequest {
            url.takeFrom(HOST)
            headers.append("apikey", SUPABASE_PUBLISHABLE_KEY)
            headers.append("Authorization", "Bearer $SUPABASE_PUBLISHABLE_KEY")
            headers.append("Accept", "application/json")
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.withTag("Network").i {
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
