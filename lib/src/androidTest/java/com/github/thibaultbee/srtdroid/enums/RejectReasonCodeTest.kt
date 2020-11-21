package com.github.thibaultbee.srtdroid.enums

import com.github.thibaultbee.srtdroid.Srt
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RejectReasonCodeTest {

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
        RejectReasonCode.values().forEach {
            assertNotNull(it.toString())
        }
    }
}