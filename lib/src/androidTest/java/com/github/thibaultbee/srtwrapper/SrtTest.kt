package com.github.thibaultbee.srtwrapper

import org.junit.Test

import org.junit.Assert.*

class SrtTest {

    @Test
    fun startCleanTest() {
        val srt = Srt()

        assertEquals(0, srt.startUp())
        srt.setLogLevel(3)
        assertEquals(0, srt.cleanUp())
    }
}