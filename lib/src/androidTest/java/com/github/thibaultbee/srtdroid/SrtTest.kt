package com.github.thibaultbee.srtdroid

import org.junit.Assert.assertEquals
import org.junit.Test

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
        assert(srt.version > 0)
        assertEquals(0, srt.cleanUp())
    }
}