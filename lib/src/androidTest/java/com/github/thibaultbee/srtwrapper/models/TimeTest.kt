package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.Srt
import com.github.thibaultbee.srtwrapper.models.Time
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


class TimeTest {
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
    fun nowTest() {
        assertNotEquals (0, Time.now())
    }

}