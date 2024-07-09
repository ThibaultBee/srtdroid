package io.github.thibaultbee.srtdroid.core.extensions

import io.github.thibaultbee.srtdroid.core.models.SrtSocket
import io.github.thibaultbee.srtdroid.core.models.SrtUrl
import java.net.BindException
import java.net.ConnectException
import java.net.SocketException

/**
 * Binds the socket to a local address.
 *
 * **See Also:** [srt_bind](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind)
 *
 * @param url the URL to bind to in FFmpeg format srt://hostname:port[?options]
 *
 * @throws BindException if bind has failed
 */
fun SrtSocket.bind(url: String) = bind(SrtUrl(url))

/**
 * Binds the socket to a local address.
 *
 * **See Also:** [srt_bind](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind)
 *
 * @param srtUrl the URL to bind to in FFmpeg format srt://hostname:port[?options]
 *
 * @throws BindException if bind has failed
 */
fun SrtSocket.bind(srtUrl: SrtUrl) {
    if (srtUrl.mode != null) {
        require(srtUrl.mode != SrtUrl.Mode.CALLER) { "Bind is only for `listener` or `rendezvous` mode but ${srtUrl.mode}" }
    }

    srtUrl.preApplyTo(this)
    srtUrl.preBindApplyTo(this)
    bind(srtUrl.hostname, srtUrl.port)
    srtUrl.postApplyTo(this)
}

/**
 * Connects a socket to an URL.
 *
 * **See Also:** [srt_connect](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect)
 *
 * @param url the URL to connect to in FFmpeg format srt://hostname:port[?options]
 * @throws ConnectException if connection has failed
 */
fun SrtSocket.connect(url: String) = connect(SrtUrl(url))

/**
 * Connects a socket to an URL.
 *
 * **See Also:** [srt_connect](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect)
 *
 * @param srtUrl the URL to connect to in FFmpeg format srt://hostname:port[?options]
 * @throws ConnectException if connection has failed
 */
fun SrtSocket.connect(srtUrl: SrtUrl) {
    if (srtUrl.mode != null) {
        require(srtUrl.mode != SrtUrl.Mode.LISTENER) { "Connect is only for `caller` or `rendezvous` mode but ${srtUrl.mode}" }
    }

    srtUrl.preApplyTo(this)
    connect(srtUrl.hostname, srtUrl.port)
    srtUrl.postApplyTo(this)
}

/**
 * Performs a rendezvous connection.
 *
 * **See Also:** [srt_rendezvous](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_rendezvous)
 *
 * @param url the URL to rendezvous to in FFmpeg format srt://hostname:port[?options]
 * @throws SocketException if rendezvous connection has failed
 */
fun SrtSocket.rendezVous(url: String) = rendezVous(SrtUrl(url))

/**
 * Performs a rendezvous connection.
 *
 * **See Also:** [srt_rendezvous](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_rendezvous)
 *
 * @param srtUrl the URL to rendezvous to in FFmpeg format srt://hostname:port[?options]
 * @throws SocketException if rendezvous connection has failed
 */
fun SrtSocket.rendezVous(
    srtUrl: SrtUrl
) {
    if (srtUrl.mode != null) {
        require(srtUrl.mode == SrtUrl.Mode.RENDEZ_VOUS) { "Connect is only for `caller` or `rendezvous` mode but ${srtUrl.mode}" }
    }

    srtUrl.preApplyTo(this)
    rendezVous(srtUrl.hostname, srtUrl.hostname, srtUrl.port)
    srtUrl.postApplyTo(this)
}
