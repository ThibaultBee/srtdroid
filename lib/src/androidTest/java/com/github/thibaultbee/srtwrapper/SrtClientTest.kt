package com.github.thibaultbee.srtwrapper

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.models.Error
import com.github.thibaultbee.srtwrapper.models.Socket
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import android.Manifest.permission.INTERNET
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class SrtClientTest {
    @Rule @JvmField
    /*
     * Grant android.permission.INTERNET so that SRT can use socket
     */
    var socketPermissionRule = GrantPermissionRule.grant(INTERNET)

    private val ip = "10.0.2.2" // emulator host loopback interface
    private val port = 1234
    private val msg = "Hello from SRT Android"

    /*
     * To be use with test-c-server from SRT library examples. Build your own SRT library with ENABLE_EXAMPLES flags.
     * Default IP and port are meant to be execute with an emulator. In this case, run on your host: ./test-c-server 127.0.0.1 1234
     * Otherwise, you will have to set ip, port and test-c-server parameters according to their network configuration
     */
    @Test
    fun srtClientTest() {
        val srt = Srt()

        assertEquals(0, srt.startUp())

        val socket = Socket()
        assertTrue(socket.isValid())

        assertEquals(0, socket.setSockOpt(SockOpt.SENDER, 1))
        assertEquals(0, socket.connect(ip, port))

        for (i in 1..20) {
            assertEquals(msg.length, socket.sendMsg(msg))
        }

        sleep(1000) // If session is close too early, msg will not be receive by server
        assertEquals(0, socket.close())
        assertEquals(0, srt.cleanUp())
    }
}