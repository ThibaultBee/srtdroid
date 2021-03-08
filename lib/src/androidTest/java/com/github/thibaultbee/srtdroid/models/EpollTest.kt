/*
 * Copyright (C) 2021 Thibault Beyou
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
package com.github.thibaultbee.srtdroid.models

import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.EpollFlag
import com.github.thibaultbee.srtdroid.enums.EpollOpt
import com.github.thibaultbee.srtdroid.enums.ErrorType
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EpollTest {
    private val srt = Srt()
    private lateinit var epoll: Epoll
    private lateinit var socket: Socket

    @Before
    fun setUp() {
        assert(srt.startUp() >= 0)
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
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun addUSockTest() {
        socket = Socket()
        assertTrue(socket.isValid)
        assertEquals(0, epoll.addUSock(socket))
        val epollOpt = listOf(EpollOpt.ERR, EpollOpt.ET)
        assertEquals(0, epoll.addUSock(socket, epollOpt))
    }

    @Test
    fun updateUSockTest() {
        socket = Socket()
        assertTrue(socket.isValid)
        assertEquals(0, epoll.updateUSock(socket))
        val epollOpt = listOf(EpollOpt.ERR, EpollOpt.ET)
        assertEquals(0, epoll.updateUSock(socket, epollOpt))
    }

    @Test
    fun removeUSockTest() {
        socket = Socket()
        assertTrue(socket.isValid)
        assertEquals(0, epoll.removeUSock(socket))
    }

    @Test
    fun testWaitTest() {
        val readFds = listOf(Socket(), Socket(), Socket())
        epoll.flags = listOf(EpollFlag.ENABLE_EMPTY)
        assertEquals(listOf(EpollFlag.ENABLE_EMPTY), epoll.flags)
        assertEquals(-1, epoll.wait(readFds, emptyList(), 1000L))
        assertEquals(Error.lastError, ErrorType.ETIMEOUT)
        assertEquals(-1, epoll.wait(readFds, timeOut = 1000L))
        assertEquals(Error.lastError, ErrorType.ETIMEOUT)
        val writeFds = listOf(Socket(), Socket())
        assertEquals(-1, epoll.wait(readFds, writeFds, 1000L))
        assertEquals(Error.lastError, ErrorType.ETIMEOUT)
    }

    @Test
    fun uWaitTest() {
        assertEquals(-1, epoll.uWait(listOf(), 1000L))
        epoll.flags = listOf(EpollFlag.ENABLE_EMPTY)
        assertEquals(listOf(EpollFlag.ENABLE_EMPTY), epoll.flags)
        assertEquals(
            0,
            epoll.uWait(listOf(EpollEvent(Socket(), listOf(EpollOpt.IN, EpollOpt.ET))), 1000L)
        )
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
        assertEquals(epoll.release(), 0)
        assertFalse(epoll.isValid)
    }
}