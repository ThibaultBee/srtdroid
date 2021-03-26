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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.ErrorType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.net.SocketException


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

@RunWith(AndroidJUnit4::class)
class ErrorTest {
    private var socket = Socket()

    @After
    fun tearDown() {
        if (socket.isValid)
            socket.close()
        assertEquals(Srt.cleanUp(), 0)
    }

    @Test
    fun getErrorTest() {
        try {
            socket.listen(3)
        } catch (e: SocketException) {
        }
        assertEquals(Error.lastError, ErrorType.EUNBOUNDSOCK)
        assertEquals(Error.lastErrorMessage, ErrorType.EUNBOUNDSOCK.toString())
    }

    @Test
    fun clearErrorTest() {
        try {
            socket.listen(3)
        } catch (e: SocketException) {
        }
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