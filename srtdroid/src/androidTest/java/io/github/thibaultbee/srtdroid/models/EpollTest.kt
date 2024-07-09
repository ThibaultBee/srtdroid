/*
 * Copyright (C) 2021 Thibault B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.enums.EpollFlag
import io.github.thibaultbee.srtdroid.enums.EpollOpt
import io.github.thibaultbee.srtdroid.enums.ErrorType
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EpollTest {
    private lateinit var epoll: Epoll
    private lateinit var socket: SrtSocket

    @Before
    fun setUp() {
        epoll = Epoll()
        assertTrue(epoll.isValid)
    }

    @After
    fun tearDown() {
        if (epoll.isValid)
            epoll.release()
        if (::socket.isInitialized) {
            if (socket.isValid)
                socket.close()
        }
        assertEquals(Srt.cleanUp(), 0)
    }

    @Test
    fun addUSockTest() {
        socket = SrtSocket()
        assertTrue(socket.isValid)
        try {
            epoll.addUSock(socket, listOf(EpollOpt.ERR))
        } catch (e: Exception) {
            fail()
        }
        try {
            epoll.addUSock(socket, listOf(EpollOpt.ERR, EpollOpt.ET))
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun updateUSockTest() {
        socket = SrtSocket()
        assertTrue(socket.isValid)
        try {
            epoll.updateUSock(socket, listOf(EpollOpt.ERR))
        } catch (e: Exception) {
            fail()
        }
        try {
            epoll.updateUSock(socket, listOf(EpollOpt.ERR, EpollOpt.ET))
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun removeUSockTest() {
        socket = SrtSocket()
        assertTrue(socket.isValid)
        try {
            epoll.removeUSock(socket)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun testWaitTest() {
        epoll.flags = listOf(EpollFlag.ENABLE_EMPTY)
        try {
            epoll.wait(1000L)
            fail()
        } catch (e: Exception) {
        }
        assertEquals(Error.lastError, ErrorType.ETIMEOUT)
        try {
            epoll.wait(timeout = 1000L)
            fail()
        } catch (e: Exception) {
        }
        assertEquals(Error.lastError, ErrorType.ETIMEOUT)
        try {
            epoll.wait(1000L)
            fail()
        } catch (e: Exception) {
        }
        assertEquals(Error.lastError, ErrorType.ETIMEOUT)
    }

    @Test
    fun uWaitTest() {
        try {
            epoll.uWait(1000L)
            fail()
        } catch (e: Exception) {
        }
        epoll.flags = listOf(EpollFlag.ENABLE_EMPTY)
        assertEquals(listOf(EpollFlag.ENABLE_EMPTY), epoll.flags)
        try {
            epoll.uWait( 1000L)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun setTest() {
        epoll.flags = listOf()
        assertEquals(listOf<EpollFlag>(), epoll.flags)
        epoll.flags = listOf(EpollFlag.ENABLE_EMPTY)
        assertEquals(
            listOf(EpollFlag.ENABLE_EMPTY),
            epoll.setFlags(listOf(EpollFlag.ENABLE_OUTPUTCHECK))
        )
        assertEquals(
            listOf(EpollFlag.ENABLE_EMPTY, EpollFlag.ENABLE_OUTPUTCHECK),
            epoll.setFlags(listOf(EpollFlag.CLEAR_ALL))
        )
        assertEquals(listOf<EpollFlag>(), epoll.setFlags(listOf()))
        epoll.release()
        try {
            epoll.flags = listOf(EpollFlag.ENABLE_EMPTY)
            fail()
        } catch (e: Exception) {
        }
    }

    @Test
    fun getTest() {
        assertEquals(listOf<EpollFlag>(), epoll.flags)
        epoll.flags = listOf(EpollFlag.ENABLE_EMPTY)
        assertEquals(listOf(EpollFlag.ENABLE_EMPTY), epoll.flags)
    }

    @Test
    fun releaseTest() {
        assertTrue(epoll.isValid)
        try {
            epoll.release()
        } catch (e: Exception) {
            fail()
        }
        assertFalse(epoll.isValid)
        try {
            epoll.release()
            fail()
        } catch (e: Exception) {

        }
    }
}