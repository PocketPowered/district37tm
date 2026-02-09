package com.district37.toastmasters.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResourceTest {

    @Test
    fun `Success state returns correct isSuccess`() {
        val resource: Resource<String> = Resource.Success("data")

        assertTrue(resource.isSuccess)
        assertFalse(resource.isLoading)
        assertFalse(resource.isError)
    }

    @Test
    fun `Error state returns correct isError`() {
        val resource: Resource<String> = Resource.Error(ErrorType.NETWORK_ERROR, "Network failed")

        assertTrue(resource.isError)
        assertFalse(resource.isSuccess)
        assertFalse(resource.isLoading)
    }

    @Test
    fun `Loading state returns correct isLoading`() {
        val resource: Resource<String> = Resource.Loading

        assertTrue(resource.isLoading)
        assertFalse(resource.isSuccess)
        assertFalse(resource.isError)
    }

    @Test
    fun `NotLoading state returns all flags as false`() {
        val resource: Resource<String> = Resource.NotLoading

        assertFalse(resource.isLoading)
        assertFalse(resource.isSuccess)
        assertFalse(resource.isError)
    }

    @Test
    fun `map transforms Success data correctly`() {
        val resource: Resource<Int> = Resource.Success(5)

        val mapped = resource.map { it * 2 }

        assertTrue(mapped is Resource.Success)
        assertEquals(10, (mapped as Resource.Success).data)
    }

    @Test
    fun `map preserves Error state`() {
        val resource: Resource<Int> = Resource.Error(ErrorType.SERVER_ERROR, "Server error")

        val mapped = resource.map { it * 2 }

        assertTrue(mapped is Resource.Error)
        assertEquals(ErrorType.SERVER_ERROR, (mapped as Resource.Error).errorType)
        assertEquals("Server error", mapped.message)
    }

    @Test
    fun `map preserves Loading state`() {
        val resource: Resource<Int> = Resource.Loading

        val mapped = resource.map { it * 2 }

        assertTrue(mapped is Resource.Loading)
    }

    @Test
    fun `map preserves NotLoading state`() {
        val resource: Resource<Int> = Resource.NotLoading

        val mapped = resource.map { it * 2 }

        assertTrue(mapped is Resource.NotLoading)
    }

    @Test
    fun `getOrNull returns data for Success`() {
        val resource: Resource<String> = Resource.Success("test data")

        assertEquals("test data", resource.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Error`() {
        val resource: Resource<String> = Resource.Error(ErrorType.CLIENT_ERROR)

        assertNull(resource.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Loading`() {
        val resource: Resource<String> = Resource.Loading

        assertNull(resource.getOrNull())
    }

    @Test
    fun `getOrNull returns null for NotLoading`() {
        val resource: Resource<String> = Resource.NotLoading

        assertNull(resource.getOrNull())
    }

    @Test
    fun `getOrThrow returns data for Success`() {
        val resource: Resource<String> = Resource.Success("test data")

        assertEquals("test data", resource.getOrThrow())
    }

    @Test
    fun `getOrThrow throws for Error`() {
        val resource: Resource<String> = Resource.Error(ErrorType.UNKNOWN_ERROR, "Something went wrong")

        val exception = assertFailsWith<Exception> {
            resource.getOrThrow()
        }
        assertEquals("Something went wrong", exception.message)
    }

    @Test
    fun `getOrThrow throws for Loading`() {
        val resource: Resource<String> = Resource.Loading

        val exception = assertFailsWith<Exception> {
            resource.getOrThrow()
        }
        assertEquals("Resource is still loading", exception.message)
    }

    @Test
    fun `getOrThrow throws for NotLoading`() {
        val resource: Resource<String> = Resource.NotLoading

        val exception = assertFailsWith<Exception> {
            resource.getOrThrow()
        }
        assertEquals("Resource not loaded", exception.message)
    }

    @Test
    fun `handle calls onSuccess for Success state`() {
        val resource: Resource<String> = Resource.Success("data")
        var successCalled = false
        var errorCalled = false
        var loadingCalled = false

        resource.handle(
            onSuccess = { successCalled = true },
            onError = { _, _ -> errorCalled = true },
            onLoading = { loadingCalled = true }
        )

        assertTrue(successCalled)
        assertFalse(errorCalled)
        assertFalse(loadingCalled)
    }

    @Test
    fun `handle calls onError for Error state`() {
        val resource: Resource<String> = Resource.Error(ErrorType.NETWORK_ERROR, "Network failed")
        var successCalled = false
        var errorCalled = false
        var receivedErrorType: ErrorType? = null
        var receivedMessage: String? = null

        resource.handle(
            onSuccess = { successCalled = true },
            onError = { type, msg ->
                errorCalled = true
                receivedErrorType = type
                receivedMessage = msg
            }
        )

        assertFalse(successCalled)
        assertTrue(errorCalled)
        assertEquals(ErrorType.NETWORK_ERROR, receivedErrorType)
        assertEquals("Network failed", receivedMessage)
    }

    @Test
    fun `handle calls onLoading for Loading state`() {
        val resource: Resource<String> = Resource.Loading
        var successCalled = false
        var errorCalled = false
        var loadingCalled = false

        resource.handle(
            onSuccess = { successCalled = true },
            onError = { _, _ -> errorCalled = true },
            onLoading = { loadingCalled = true }
        )

        assertFalse(successCalled)
        assertFalse(errorCalled)
        assertTrue(loadingCalled)
    }

    @Test
    fun `handle calls nothing for NotLoading state`() {
        val resource: Resource<String> = Resource.NotLoading
        var successCalled = false
        var errorCalled = false
        var loadingCalled = false

        resource.handle(
            onSuccess = { successCalled = true },
            onError = { _, _ -> errorCalled = true },
            onLoading = { loadingCalled = true }
        )

        assertFalse(successCalled)
        assertFalse(errorCalled)
        assertFalse(loadingCalled)
    }
}
