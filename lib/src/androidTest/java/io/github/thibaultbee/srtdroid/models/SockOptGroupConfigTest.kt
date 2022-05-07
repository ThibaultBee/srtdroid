package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.enums.SockOpt
import org.junit.After
import org.junit.Test

class SockOptGroupConfigTest {
    private val optionConfig = SockOptGroupConfig()

    @After
    fun tearDown() {
        optionConfig.close()
    }

    @Test
    fun addValidSockOptTest() {
        optionConfig.add(SockOpt.SNDBUF, 128.toByte())
    }

    @Test
    fun addInvalidSockOptTest() {
        optionConfig.add(SockOpt.BINDTODEVICE, "test")
    }
}