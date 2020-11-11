package com.github.thibaultbee.srtwrapper

import org.junit.Test

import org.junit.Assert.*

class SrtTest {

    @Test
    fun startCleanTest() {
        val srt = Srt()

        assert(srt.startUp() >= 0)
        srt.setLogLevel(3)
        assertEquals(0, srt.cleanUp())
    }

    @Test
    fun getVersionTest() {
        val srt = Srt()

        assert(srt.startUp() >= 0)
        assert(srt.getVersion() > 0)
        assertEquals(0, srt.cleanUp())
    }
}