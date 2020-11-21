package com.github.thibaultbee.srtdroid.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.ErrorType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

@RunWith(AndroidJUnit4::class)
class ErrorTest {
    private val srt = Srt()
    private var socket = Socket()

    @Before
    fun setUp() {
        assert(srt.startUp() >= 0)
    }

    @After
    fun tearDown() {
        if (socket.isValid())
            socket.close()
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun getErrorTest() {
        assertEquals(-1, socket.listen(3))
        assertEquals(Error.lastError, ErrorType.EUNBOUNDSOCK)
        assertEquals(Error.lastErrorMessage, ErrorType.EUNBOUNDSOCK.toString())
    }

    @Test
    fun clearErrorTest() {
        assertEquals(-1, socket.listen(3))
        assertEquals(Error.lastError, ErrorType.EUNBOUNDSOCK)
        Error.clearLastError()
        assertEquals(Error.lastError, ErrorType.SUCCESS)
    }

    @Test
    fun allValuesTest() {
        ErrorType.values().forEach {
            assertNotNull(it.toString())
        }
    }
}