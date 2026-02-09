package com.district37.toastmasters.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PagedConnectionTest {

    @Test
    fun `append combines items from both pages`() {
        val page1 = PagedConnection(
            items = listOf("a", "b"),
            hasNextPage = true,
            endCursor = "cursor1",
            totalCount = 10
        )
        val page2 = PagedConnection(
            items = listOf("c", "d"),
            hasNextPage = false,
            endCursor = "cursor2",
            totalCount = 10
        )

        val combined = page1.append(page2)

        assertEquals(listOf("a", "b", "c", "d"), combined.items)
    }

    @Test
    fun `append uses pagination info from second page`() {
        val page1 = PagedConnection(
            items = listOf("a"),
            hasNextPage = true,
            endCursor = "cursor1",
            totalCount = 5
        )
        val page2 = PagedConnection(
            items = listOf("b"),
            hasNextPage = false,
            endCursor = "cursor2",
            totalCount = 10
        )

        val combined = page1.append(page2)

        assertFalse(combined.hasNextPage)
        assertEquals("cursor2", combined.endCursor)
        assertEquals(10, combined.totalCount)
    }

    @Test
    fun `map transforms items while preserving pagination`() {
        val connection = PagedConnection(
            items = listOf(1, 2, 3),
            hasNextPage = true,
            endCursor = "cursor",
            totalCount = 100
        )

        val mapped = connection.map { it * 10 }

        assertEquals(listOf(10, 20, 30), mapped.items)
        assertTrue(mapped.hasNextPage)
        assertEquals("cursor", mapped.endCursor)
        assertEquals(100, mapped.totalCount)
    }

    @Test
    fun `filter removes items that do not match predicate`() {
        val connection = PagedConnection(
            items = listOf(1, 2, 3, 4, 5),
            hasNextPage = true,
            endCursor = "cursor",
            totalCount = 100
        )

        val filtered = connection.filter { it % 2 == 0 }

        assertEquals(listOf(2, 4), filtered.items)
    }

    @Test
    fun `filter updates totalCount to filtered size`() {
        val connection = PagedConnection(
            items = listOf(1, 2, 3, 4, 5),
            hasNextPage = true,
            endCursor = "cursor",
            totalCount = 100
        )

        val filtered = connection.filter { it > 3 }

        assertEquals(2, filtered.totalCount)
    }

    @Test
    fun `filter preserves hasNextPage and endCursor`() {
        val connection = PagedConnection(
            items = listOf(1, 2, 3),
            hasNextPage = true,
            endCursor = "cursor123",
            totalCount = 50
        )

        val filtered = connection.filter { it == 2 }

        assertTrue(filtered.hasNextPage)
        assertEquals("cursor123", filtered.endCursor)
    }

    @Test
    fun `empty creates connection with no items`() {
        val empty = PagedConnection.empty<String>()

        assertTrue(empty.items.isEmpty())
    }

    @Test
    fun `empty has hasNextPage false and null cursor`() {
        val empty = PagedConnection.empty<Int>()

        assertFalse(empty.hasNextPage)
        assertNull(empty.endCursor)
        assertEquals(0, empty.totalCount)
    }

    @Test
    fun `totalCount defaults to items size when not specified`() {
        val connection = PagedConnection(
            items = listOf("a", "b", "c"),
            hasNextPage = false,
            endCursor = null
        )

        assertEquals(3, connection.totalCount)
    }

    @Test
    fun `map with empty list returns empty mapped list`() {
        val connection = PagedConnection<Int>(
            items = emptyList(),
            hasNextPage = false,
            endCursor = null,
            totalCount = 0
        )

        val mapped = connection.map { it.toString() }

        assertTrue(mapped.items.isEmpty())
        assertEquals(0, mapped.totalCount)
    }

    @Test
    fun `filter with no matches returns empty list`() {
        val connection = PagedConnection(
            items = listOf(1, 2, 3),
            hasNextPage = true,
            endCursor = "cursor",
            totalCount = 3
        )

        val filtered = connection.filter { it > 100 }

        assertTrue(filtered.items.isEmpty())
        assertEquals(0, filtered.totalCount)
    }
}
