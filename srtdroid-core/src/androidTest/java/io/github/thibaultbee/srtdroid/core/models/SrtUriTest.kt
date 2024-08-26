package io.github.thibaultbee.srtdroid.core.models

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SrtUriTest {
    @Test
    fun srtUriStreamId() {
        var srtUri = SrtUri(Uri.parse("srt://host:9000?srt_streamid=abcde"))
        assertEquals("abcde", srtUri.streamId)

        srtUri = SrtUri(Uri.parse("srt://host:9000?streamid=abcde"))
        assertEquals("abcde", srtUri.streamId)

        srtUri = SrtUri(Uri.parse("srt://host:9000?streamid=#!::u=admin,r=bluesbrothers1_hi"))
        assertEquals("#!::u=admin,r=bluesbrothers1_hi", srtUri.streamId)

        srtUri = SrtUri(Uri.parse("srt://host:9000?srt_streamid=#!::u=admin,r=bluesbrothers1_hi"))
        assertEquals("#!::u=admin,r=bluesbrothers1_hi", srtUri.streamId)

        srtUri = SrtUri(Uri.parse("srt://host:9000"))
        assertNull(srtUri.streamId)
    }

    @Test
    fun srtUriOtherParameters() {
        val srtUri =
            SrtUri(Uri.parse("srt://host:9000?transtype=live&srt_streamid=#!::u=admin,r=bluesbrothers1_hi&rcvlatency=2000"))
        assertEquals("#!::u=admin,r=bluesbrothers1_hi", srtUri.streamId)
        assertEquals("live", srtUri.getQueryParameter("transtype"))
        assertEquals("2000", srtUri.getQueryParameter("rcvlatency"))
    }

    @Test
    fun srtUriBuilder() {
        val builder = SrtUri.Builder()
        builder.encodedAuthority("host:9000")
            .streamId("#!::u=admin,r=bluesbrothers1_hi")
            .appendQueryParameter("transtype", "live")
            .appendQueryParameter("rcvlatency", "2000")
        val srtUri = builder.build()
        assertEquals(srtUri.streamId, "#!::u=admin,r=bluesbrothers1_hi")
        assertEquals(srtUri.getQueryParameter("transtype"), "live")
        assertEquals(srtUri.getQueryParameter("rcvlatency"), "2000")
        assertTrue(srtUri.toString().contains("streamid=#!::u=admin,r=bluesbrothers1_hi"))
    }

    @Test
    fun srtUriBuilderException() {
        val builder = SrtUri.Builder()
        builder.encodedAuthority("host:9000")

        try {
            builder.appendQueryParameter("streamid", "abcde")
            fail("Should throw IllegalArgumentException")
        } catch (_: Exception) {
        }

        try {
            builder.appendQueryParameter("srt_streamid", "abcde")
            fail("Should throw IllegalArgumentException")
        } catch (_: Exception) {
        }
    }
}