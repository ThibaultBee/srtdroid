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
package io.github.thibaultbee.srtdroid.core.models

import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.ErrorType
import io.github.thibaultbee.srtdroid.core.enums.RejectReasonCode
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.SockStatus
import io.github.thibaultbee.srtdroid.core.enums.Transtype
import io.github.thibaultbee.srtdroid.core.models.rejectreason.InternalRejectReason
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.SocketException


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */
class SrtSocket6Test {
    private lateinit var socket: SrtSocket

    @Before
    fun setUp() {
        socket = SrtSocket()
        assertTrue(socket.isValid)
    }

    @After
    fun tearDown() {
        if (socket.isValid)
            socket.close()
        assertEquals(Srt.cleanUp(), 0)
    }

    @Test
    fun bindTest() {
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        socket.bind("::1", 1111)
        assertTrue(socket.isBound)
    }

    @Test
    fun sockStatusTest() {
        assertEquals(SockStatus.INIT, socket.sockState)
        socket.bind("::1", 2222)
        assertEquals(SockStatus.OPENED, socket.sockState)
    }

    @Test
    fun listenTest() {
        try {
            socket.listen(3)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.EUNBOUNDSOCK.toString())
        }
        socket.bind("::1", 3333)
        socket.listen(3)
    }

    @Test
    fun connectTest() {
        try {
            socket.connect("::1", 4444)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOSERVER.toString())
        }
        assertEquals(InternalRejectReason(RejectReasonCode.TIMEOUT), socket.rejectReason)
    }

    @Test
    fun rendezVousTest() {
        try {
            socket.rendezVous("::1", "::2", 5555)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOSERVER.toString())
        }
    }

    @Test
    fun getSockNameTest() {
        try {
            assertNull(socket.sockName)
            fail()
        } catch (_: Exception) {
        }
        socket.bind("::1", 6666)
        assertEquals(socket.sockName.address.hostAddress, "::1")
        assertEquals(socket.sockName.port, 6666)
    }
}