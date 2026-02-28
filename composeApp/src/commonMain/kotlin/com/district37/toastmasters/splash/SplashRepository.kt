package com.district37.toastmasters.splash

import com.apollographql.apollo3.ApolloClient
import com.district37.toastmasters.graphql.ResourcesByTypeQuery

private const val SPLASH_RESOURCE_TYPE = "splash"

class SplashRepository(
    private val apolloClient: ApolloClient,
    private val splashOverrideStore: SplashOverrideStore
) {

    fun getCachedSplashImageUrl(): String? {
        val cachedUrl = splashOverrideStore.getSplashImageUrl()?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedCachedUrl = cachedUrl?.let(::normalizeSplashUrl)
        if (normalizedCachedUrl != cachedUrl) {
            splashOverrideStore.setSplashImageUrl(normalizedCachedUrl)
        }
        return normalizedCachedUrl
    }

    suspend fun syncSplashImageUrlFromNetwork(): String? {
        try {
            val response = apolloClient.query(ResourcesByTypeQuery(SPLASH_RESOURCE_TYPE)).execute()
            if (!response.hasErrors()) {
                val latestUrl = response.data?.resourcesCollection?.edges
                    ?.asSequence()
                    ?.mapNotNull { it.node.url?.trim()?.takeIf { value -> value.isNotEmpty() } }
                    ?.map(::normalizeSplashUrl)
                    ?.firstOrNull { it.isNotEmpty() && isSupportedUrl(it) }

                splashOverrideStore.setSplashImageUrl(latestUrl)
            }
        } catch (_: Exception) {
            // Keep existing cached override on network failure.
        }
        return splashOverrideStore.getSplashImageUrl()
    }

    private fun isSupportedUrl(url: String): Boolean {
        return url.startsWith("https://") || url.startsWith("http://")
    }

    private fun normalizeSplashUrl(url: String): String {
        if (!url.contains("static.wikia.nocookie.net")) return url
        if (!url.contains("/revision/latest")) return url
        if (url.contains("format=original")) return url
        val separator = if (url.contains("?")) "&" else "?"
        return "$url${separator}format=original"
    }
}
