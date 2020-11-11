package com.github.thibaultbee.srtwrapper.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtwrapper.Srt
import com.github.thibaultbee.srtwrapper.enums.ErrorType
import org.junit.After
import org.junit.Assert.*
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
        assertEquals(Error.getLastError(), ErrorType.EUNBOUNDSOCK)
        assertEquals(Error.getLastErrorMessage(), ErrorType.EUNBOUNDSOCK.toString())
    }

    @Test
    fun clearErrorTest() {
        assertEquals(-1, socket.listen(3))
        assertEquals(Error.getLastError(), ErrorType.EUNBOUNDSOCK)
        Error.clearLastError()
        assertEquals(Error.getLastError(), ErrorType.SUCCESS)
    }

    @Test
    fun allValuesTest() {
        ErrorType.values().forEach {
            assertNotNull(it.toString())
        }
    }
}