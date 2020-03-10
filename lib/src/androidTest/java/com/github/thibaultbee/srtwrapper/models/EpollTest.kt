package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.Srt
import com.github.thibaultbee.srtwrapper.enums.EpollFlag
import com.github.thibaultbee.srtwrapper.enums.EpollOpt
import com.github.thibaultbee.srtwrapper.enums.ErrorType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EpollTest {
    private val srt = Srt()
    private lateinit var epoll: Epoll
    private lateinit var socket: Socket

    @Before
    fun setUp() {
        assertEquals(srt.startUp(), 0)
        epoll = Epoll()
        assertTrue(epoll.isValid())
    }

    @After
    fun tearDown() {
        if (epoll.isValid())
            epoll.release()
        if (::socket.isInitialized) {
            if (socket.isValid())
                socket.close()
        }
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun addUSockTest() {
        socket = Socket()
        assertTrue(socket.isValid())
        assertEquals(0, epoll.addUSock(socket, null))
        val epollOpt = listOf(EpollOpt.ERR, EpollOpt.ET)
        assertEquals(0, epoll.addUSock(socket, epollOpt))
    }

    @Test
    fun updateUSockTest() {
        assertEquals(0, epoll.updateUSock(socket, null))
        val epollOpt = listOf(EpollOpt.ERR, EpollOpt.ET)
        assertEquals(0, epoll.updateUSock(socket, epollOpt))
    }

    @Test
    fun removeUSockTest() {
        assertEquals(0, epoll.removeUSock(socket))
    }

    @Test
    fun testWaitTest() {
        val readFds = listOf(Socket(), Socket(), Socket())
        assertEquals(listOf<EpollFlag>(), epoll.set(listOf(EpollFlag.ENABLE_EMPTY)))
        assertEquals(-1, epoll.wait(readFds, emptyList(), 1000L))
        assertEquals(Error.getLastError(), ErrorType.ETIMEOUT)
        assertEquals(-1, epoll.wait(readFds, timeOut = 1000L))
        assertEquals(Error.getLastError(), ErrorType.ETIMEOUT)
        val writeFds = listOf(Socket(), Socket())
        assertEquals(-1, epoll.wait(readFds, writeFds, 1000L))
        assertEquals(Error.getLastError(), ErrorType.ETIMEOUT)
    }

    @Test
    fun uWaitTest() {
        assertEquals(-1, epoll.uWait(listOf(), 1000L))
        assertEquals(listOf<EpollFlag>(), epoll.set(listOf(EpollFlag.ENABLE_EMPTY)))
        assertEquals(
            0,
            epoll.uWait(listOf(EpollEvent(Socket(), listOf(EpollOpt.IN, EpollOpt.ET))), 1000L)
        )
    }

    @Test
    fun setTest() {
        assertEquals(listOf<EpollFlag>(), epoll.set(listOf(EpollFlag.ENABLE_EMPTY)))
        assertEquals(
            listOf(EpollFlag.ENABLE_EMPTY),
            epoll.set(listOf(EpollFlag.ENABLE_OUTPUTCHECK))
        )
        assertEquals(
            listOf(EpollFlag.ENABLE_EMPTY, EpollFlag.ENABLE_OUTPUTCHECK),
            epoll.set(listOf(EpollFlag.CLEAR_ALL))
        )
        assertEquals(listOf<EpollFlag>(), epoll.set(listOf()))
    }

    @Test
    fun getTest() {
        assertEquals(listOf<EpollFlag>(), epoll.get())
        assertEquals(listOf<EpollFlag>(), epoll.set(listOf(EpollFlag.ENABLE_EMPTY)))
        assertEquals(listOf(EpollFlag.ENABLE_EMPTY), epoll.get())
    }
}