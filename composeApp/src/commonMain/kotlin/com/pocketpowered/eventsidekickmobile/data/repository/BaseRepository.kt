package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.district37.toastmasters.models.PagedConnection
import com.district37.toastmasters.util.ErrorType
import com.district37.toastmasters.util.Logger
import com.district37.toastmasters.util.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Base repository class providing common logging and error handling for all repositories
 */
abstract class BaseRepository(protected val apolloClient: ApolloClient) {

    protected abstract val tag: String

    /**
     * Execute a GraphQL query with standardized logging and error handling
     *
     * @param queryName Human-readable name of the query for logging
     * @param query Lambda that executes the Apollo query
     * @param transform Lambda that transforms the response data into the desired type
     */
    protected suspend fun <D : Operation.Data, T> executeQuery(
        queryName: String,
        query: suspend () -> ApolloResponse<D>,
        transform: (D) -> T?
    ): Resource<T> {
        return try {
            val response = query()
            handleResponse(queryName, response, transform)
        } catch (e: CancellationException) {
            // Always re-throw CancellationException to properly cancel coroutines
            throw e
        } catch (e: Exception) {
            Logger.e(tag, "Exception during $queryName: ${e.message}", e)
            Resource.Error(ErrorType.NETWORK_ERROR, e.message)
        }
    }

    /**
     * Execute a GraphQL mutation with standardized logging and error handling
     *
     * @param mutationName Human-readable name of the mutation for logging
     * @param mutation Lambda that executes the Apollo mutation
     * @param transform Lambda that transforms the response data into the desired type
     */
    protected suspend fun <D : Operation.Data, T> executeMutation(
        mutationName: String,
        mutation: suspend () -> ApolloResponse<D>,
        transform: (D) -> T?
    ): Resource<T> {
        return try {
            val response = mutation()
            handleResponse(mutationName, response, transform)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(tag, "Exception during $mutationName: ${e.message}", e)
            Resource.Error(ErrorType.NETWORK_ERROR, e.message)
        }
    }

    /**
     * Execute a query with automatic retry for network errors
     *
     * @param queryName Human-readable name of the query for logging
     * @param query Lambda that executes the Apollo query
     * @param transform Lambda that transforms the response data into the desired type
     * @param maxAttempts Maximum number of attempts (default: 3)
     * @param initialDelayMs Initial delay between retries in milliseconds (default: 1000)
     * @param maxDelayMs Maximum delay between retries in milliseconds (default: 5000)
     */
    protected suspend fun <D : Operation.Data, T> executeQueryWithRetry(
        queryName: String,
        query: suspend () -> ApolloResponse<D>,
        transform: (D) -> T?,
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 5000
    ): Resource<T> = executeWithRetry(
        maxAttempts = maxAttempts,
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs
    ) {
        executeQuery(queryName, query, transform)
    }

    /**
     * Execute a mutation with automatic retry for network errors
     *
     * @param mutationName Human-readable name of the mutation for logging
     * @param mutation Lambda that executes the Apollo mutation
     * @param transform Lambda that transforms the response data into the desired type
     * @param maxAttempts Maximum number of attempts (default: 3)
     * @param initialDelayMs Initial delay between retries in milliseconds (default: 1000)
     * @param maxDelayMs Maximum delay between retries in milliseconds (default: 5000)
     */
    protected suspend fun <D : Operation.Data, T> executeMutationWithRetry(
        mutationName: String,
        mutation: suspend () -> ApolloResponse<D>,
        transform: (D) -> T?,
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 5000
    ): Resource<T> = executeWithRetry(
        maxAttempts = maxAttempts,
        initialDelayMs = initialDelayMs,
        maxDelayMs = maxDelayMs
    ) {
        executeMutation(mutationName, mutation, transform)
    }

    /**
     * Generic retry mechanism with exponential backoff
     *
     * Retries only on network errors. Other error types are returned immediately.
     * Uses exponential backoff with a maximum delay cap.
     *
     * @param maxAttempts Maximum number of attempts
     * @param initialDelayMs Initial delay between retries
     * @param maxDelayMs Maximum delay between retries
     * @param block The operation to retry
     */
    protected suspend fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 5000,
        block: suspend () -> Resource<T>
    ): Resource<T> {
        var currentDelay = initialDelayMs
        repeat(maxAttempts - 1) { attempt ->
            when (val result = block()) {
                is Resource.Success -> return result
                is Resource.Error -> {
                    if (result.errorType == ErrorType.NETWORK_ERROR) {
                        Logger.d(tag, "Retry attempt ${attempt + 1} after network error. Waiting ${currentDelay}ms...")
                        delay(currentDelay)
                        currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
                    } else {
                        // Don't retry non-network errors
                        return result
                    }
                }
                else -> return result
            }
        }
        // Final attempt
        return block()
    }

    /**
     * Handle Apollo response with standardized error checking and transformation
     */
    private fun <D : Operation.Data, T> handleResponse(
        operationName: String,
        response: ApolloResponse<D>,
        transform: (D) -> T?
    ): Resource<T> {
        return if (response.hasErrors()) {
            Logger.e(tag, "$operationName error: ${response.errors}")
            Resource.Error(ErrorType.SERVER_ERROR, response.errors?.firstOrNull()?.message)
        } else {
            val data = response.data
            if (data == null) {
                Logger.e(tag, "$operationName: No data returned in response")
                Resource.Error(ErrorType.CLIENT_ERROR, "No data returned")
            } else {
                val result = transform(data)
                if (result != null) {
                    Resource.Success(result)
                } else {
                    Logger.e(tag, "$operationName: Data not found or transformation failed")
                    Resource.Error(ErrorType.CLIENT_ERROR, "Data not found")
                }
            }
        }
    }

    // ========== Pagination Helpers ==========

    /**
     * Helper to transform GraphQL connection edges into a PagedConnection.
     *
     * This consolidates the common pattern:
     * ```kotlin
     * val items = connection.edges.map { edge -> edge.node.toModel() }
     * PagedConnection(
     *     items = items,
     *     hasNextPage = connection.pageInfo.hasNextPage,
     *     endCursor = connection.pageInfo.endCursor
     * )
     * ```
     *
     * Usage:
     * ```kotlin
     * transform = { data ->
     *     data.eventsConnection?.let { connection ->
     *         toPagedConnection(
     *             edges = connection.edges,
     *             pageInfo = connection.pageInfo.paginationInfo,
     *             transform = { edge -> edge.node.eventPreview.toEvent() }
     *         )
     *     }
     * }
     * ```
     *
     * @param edges The list of edges from the GraphQL connection
     * @param hasNextPage Whether there are more pages available
     * @param endCursor The cursor for fetching the next page
     * @param totalCount Optional total count of items
     * @param transform Function to transform each edge into the desired type
     */
    protected fun <Edge, T> toPagedConnection(
        edges: List<Edge>,
        hasNextPage: Boolean,
        endCursor: String?,
        totalCount: Int? = null,
        transform: (Edge) -> T
    ): PagedConnection<T> {
        val items = edges.map(transform)
        return PagedConnection(
            items = items,
            hasNextPage = hasNextPage,
            endCursor = endCursor,
            totalCount = totalCount ?: items.size
        )
    }

    /**
     * Overload that accepts a PaginationInfo-like object.
     * Works with GraphQL pageInfo structures that have hasNextPage and endCursor.
     */
    protected fun <Edge, PageInfo, T> toPagedConnection(
        edges: List<Edge>,
        pageInfo: PageInfo,
        totalCount: Int? = null,
        getHasNextPage: (PageInfo) -> Boolean,
        getEndCursor: (PageInfo) -> String?,
        transform: (Edge) -> T
    ): PagedConnection<T> = toPagedConnection(
        edges = edges,
        hasNextPage = getHasNextPage(pageInfo),
        endCursor = getEndCursor(pageInfo),
        totalCount = totalCount,
        transform = transform
    )
}
