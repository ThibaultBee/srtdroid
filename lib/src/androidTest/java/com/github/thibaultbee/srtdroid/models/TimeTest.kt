package com.github.thibaultbee.srtdroid.models

import com.github.thibaultbee.srtdroid.Srt
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test


class TimeTest {
    private val srt = Srt()

    @Before
    fun setUp() {
        assert(srt.startUp() >= 0)
    }

    @After
    fun tearDown() {
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun nowTest() {
        assertNotEquals (0, Time.now())
    }

}