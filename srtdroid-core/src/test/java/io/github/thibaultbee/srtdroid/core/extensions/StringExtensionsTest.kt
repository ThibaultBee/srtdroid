package io.github.thibaultbee.srtdroid.core.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {
    @Test
    fun `replace between with an end`() {
        val input = "Hello, [name]!"
        val start = "["
        val end = "]"
        val value = "MY NAME"
        val expected = "Hello, [MY NAME]!"
        assertEquals(expected, input.replaceBetween(start, end, value))
    }

    @Test
    fun `replace between without an end`() {
        val input = "Hello, [name"
        val start = "["
        val end = "]"
        val value = "MY NAME"
        val expected = "Hello, [MY NAME"
        assertEquals(expected, input.replaceBetween(start, end, value))
    }

    @Test
    fun `substring between with an end`() {
        val input = "Hello, [name]!"
        val start = "["
        val end = "]"
        val expected = "name"
        assertEquals(expected, input.substringBetween(start, end))
    }

    @Test
    fun `substring between without an end`() {
        val input = "Hello, [name"
        val start = "["
        val end = "]"
        val expected = "name"
        assertEquals(expected, input.substringBetween(start, end))
    }
}