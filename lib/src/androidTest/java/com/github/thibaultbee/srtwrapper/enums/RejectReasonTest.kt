package com.github.thibaultbee.srtwrapper.enums

import com.github.thibaultbee.srtwrapper.Srt
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RejectReasonTest {

    private val srt = Srt()

    @Before
    fun setUp() {
        assertEquals(srt.startUp(), 0)
    }

    @After
    fun tearDown() {
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun toStringTest() {
        RejectReason.values().forEach {
            assertNotNull(it.toString())
        }
    }
}