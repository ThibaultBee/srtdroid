package com.github.thibaultbee.srtwrapper

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.models.Socket
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import android.Manifest.permission.INTERNET
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.github.thibaultbee.srtwrapper.enums.Transtype
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import org.junit.Rule
import java.io.File

@RunWith(AndroidJUnit4::class)
class SrtRecvFileTest {
    /*
     * Grant android.permission.INTERNET so that SRT can use socket
     */
    @Rule
    @JvmField
    var socketPermissionRule = GrantPermissionRule.grant(INTERNET)

    private val ip = "10.0.2.2" // emulator host loopback interface
    private val port = 9000
    private val serverFile = "version.h" // file from SRT source code

    /*
     * Same as SRT example recvfile.
     * To be use with sendfile from SRT library examples (./sendfile server_port).
     * Build your own SRT library with ENABLE_EXAMPLES flags.
     * Default IP and port are meant to be execute with an emulator. In this case, run on your host from SRT directory:
     * ./sendfile 9000
     */
    @Test
    fun srtClientTest() {
        val srt = Srt()

        assertEquals(0, srt.startUp())

        val socket = Socket()
        assertTrue(socket.isValid())

        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(0, socket.connect(ip, port))

        assertEquals(
            Ints.BYTES,
            socket.sendMsg(Ints.toByteArray(serverFile.length).reversedArray())
        )
        assertEquals(serverFile.length, socket.sendMsg(serverFile))

        val fileSize = Longs.fromByteArray(socket.recv(Longs.BYTES).reversedArray())
        assert(fileSize > 0)

        // Where file will be written
        val myFile = File(
            InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
            "FileToRecv"
        )
        assertEquals(fileSize, socket.recvFile(myFile, 0, fileSize))

        // TODO: add transmission stats

        assertEquals(0, socket.close())
        assertEquals(0, srt.cleanUp())
    }
}