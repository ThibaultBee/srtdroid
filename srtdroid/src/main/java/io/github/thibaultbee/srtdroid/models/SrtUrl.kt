/*
 * Copyright (C) 2024 Thibault B.
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

import android.net.Uri
import android.util.Log
import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.enums.SockOpt
import io.github.thibaultbee.srtdroid.enums.Transtype
import io.github.thibaultbee.srtdroid.extensions.toBoolean
import java.security.InvalidParameterException

/**
 * Extracts [SrtUrl] from a FFmpeg format: srt://hostname:port[?options]
 */
fun SrtUrl(url: String): SrtUrl {
    val uri = Uri.parse(url)
    if (uri.scheme != SrtUrl.SRT_SCHEME) {
        throw InvalidParameterException("URL $url is not an srt URL")
    }
    val hostname = uri.host
        ?: throw InvalidParameterException("Failed to parse URL $url: unknown host")
    val port = uri.port

    val connectTimeoutInMs =
        uri.getQueryParameter(SrtUrl.CONNECTION_TIMEOUT_QUERY_PARAMETER)?.toLong()
    val flightFlagSize = uri.getQueryParameter(SrtUrl.FFS_QUERY_PARAMETER)?.toInt()
    val inputBandwidth = uri.getQueryParameter(SrtUrl.INPUT_BANDWIDTH_QUERY_PARAMETER)?.toInt()
    val iptos = uri.getQueryParameter(SrtUrl.IPTOS_QUERY_PARAMETER)?.toInt()
    val ipttl = uri.getQueryParameter(SrtUrl.IPTTL_QUERY_PARAMETER)?.toInt()
    val latencyInUs = uri.getQueryParameter(SrtUrl.LATENCY_QUERY_PARAMETER)?.toLong()
    val listenTimeoutInUs = uri.getQueryParameter(SrtUrl.LISTEN_TIMEOUT_QUERY_PARAMETER)?.toLong()
    val maxBandwidth = uri.getQueryParameter(SrtUrl.MAX_BANDWIDTH_QUERY_PARAMETER)?.toLong()
    val mode = uri.getQueryParameter(SrtUrl.MODE_QUERY_PARAMETER)?.let { SrtUrl.Mode.entryOf(it) }
    val maxSegmentSize = uri.getQueryParameter(SrtUrl.MAX_SEGMENT_SIZE_QUERY_PARAMETER)?.toInt()
    val nakReport = uri.getQueryParameter(SrtUrl.NAK_REPORT_QUERY_PARAMETER)?.toInt()?.toBoolean()
    val overheadBandwidth =
        uri.getQueryParameter(SrtUrl.OVERHEAD_BANDWIDTH_QUERY_PARAMETER)?.toInt()
    val passphrase = uri.getQueryParameter(SrtUrl.PASS_PHRASE_QUERY_PARAMETER)
    val enforcedEncryption =
        uri.getQueryParameter(SrtUrl.ENFORCED_ENCRYPTION_QUERY_PARAMETER)?.toInt()?.toBoolean()
    val kmRefreshRate = uri.getQueryParameter(SrtUrl.KM_REFRESH_RATE_QUERY_PARAMETER)?.toInt()
    val kmPreannounce = uri.getQueryParameter(SrtUrl.KM_PREANNOUNCE_QUERY_PARAMETER)?.toInt()
    val senderDropDelayInUs =
        uri.getQueryParameter(SrtUrl.SENDER_DROP_DELAY_QUERY_PARAMETER)?.toLong()
    val payloadSize = uri.getQueryParameter(SrtUrl.PAYLOAD_SIZE_QUERY_PARAMETER)?.toInt()
        ?: uri.getQueryParameter(
            SrtUrl.PACKET_SIZE_QUERY_PARAMETER
        )?.toInt()
    val peerLatencyInUs = uri.getQueryParameter(SrtUrl.PEER_LATENCY_QUERY_PARAMETER)?.toLong()
    val pbKeyLength = uri.getQueryParameter(SrtUrl.PB_KEY_LENGTH_QUERY_PARAMETER)?.toInt()
    val receiverLatencyInUs =
        uri.getQueryParameter(SrtUrl.RECEIVER_LATENCY_QUERY_PARAMETER)?.toLong()
    val receiveUDPBufferSize =
        uri.getQueryParameter(SrtUrl.RECEIVE_UDP_BUFFER_SIZE_QUERY_PARAMETER)?.toInt()
    val sendUDPBufferSize =
        uri.getQueryParameter(SrtUrl.SEND_UDP_BUFFER_SIZE_QUERY_PARAMETER)?.toInt()
    val timeoutInUs = uri.getQueryParameter(SrtUrl.TIMEOUT_QUERY_PARAMETER)?.toLong()
    val enableTooLatePacketDrop =
        uri.getQueryParameter(SrtUrl.ENABLE_TOO_LATE_PACKET_DROP_QUERY_PARAMETER)?.toInt()
            ?.toBoolean()
    val sendBufferSize = uri.getQueryParameter(SrtUrl.SEND_BUFFER_SIZE_QUERY_PARAMETER)?.toInt()
    val recvBufferSize = uri.getQueryParameter(SrtUrl.RECV_BUFFER_SIZE_QUERY_PARAMETER)?.toInt()
    val lossMaxTTL = uri.getQueryParameter(SrtUrl.LOSS_MAX_TTL_QUERY_PARAMETER)?.toInt()
    val minVersion = uri.getQueryParameter(SrtUrl.MIN_VERSION_QUERY_PARAMETER)?.toInt()
    val streamId = uri.getQueryParameter(SrtUrl.STREAM_ID_QUERY_PARAMETER) ?: uri.getQueryParameter(
        SrtUrl.SRT_STREAM_ID_QUERY_PARAMETER
    )
    val smoother =
        uri.getQueryParameter(SrtUrl.SMOOTHER_QUERY_PARAMETER)?.let { Transtype.entryOf(it) }
    val enableMessageApi =
        uri.getQueryParameter(SrtUrl.ENABLE_MESSAGE_API_QUERY_PARAMETER)?.toInt()?.toBoolean()
    val transtype =
        uri.getQueryParameter(SrtUrl.TRANSTYPE_QUERY_PARAMETER)?.let { Transtype.entryOf(it) }
    val lingerInS = uri.getQueryParameter(SrtUrl.LINGER_QUERY_PARAMETER)?.toInt()
    val enableTimestampBasedPacketDelivery =
        uri.getQueryParameter(SrtUrl.ENABLE_TIMESTAMP_BASED_PACKET_DELIVERY_QUERY_PARAMETER)
            ?.toInt()?.toBoolean()

    val unknownParameters =
        uri.queryParameterNames.find { SrtUrl.supportedQueryParameterList.contains(it).not() }
    if (unknownParameters != null) {
        throw InvalidParameterException("Failed to parse URL $url: unknown parameter(s): $unknownParameters")
    }

    return SrtUrl(
        hostname,
        port,
        connectTimeoutInMs?.toInt(),
        flightFlagSize,
        inputBandwidth?.toLong(),
        iptos,
        ipttl,
        latencyInUs?.div(1000)?.toInt(),
        listenTimeoutInUs?.div(1000)?.toInt(),
        maxBandwidth,
        mode,
        maxSegmentSize,
        nakReport,
        overheadBandwidth,
        passphrase,
        enforcedEncryption,
        kmRefreshRate,
        kmPreannounce,
        senderDropDelayInUs?.div(1000)?.toInt(),
        payloadSize,
        peerLatencyInUs?.div(1000)?.toInt(),
        pbKeyLength,
        receiverLatencyInUs?.div(1000)?.toInt(),
        receiveUDPBufferSize,
        sendUDPBufferSize,
        timeoutInUs?.div(1000)?.toInt(),
        enableTooLatePacketDrop,
        sendBufferSize,
        recvBufferSize,
        lossMaxTTL,
        minVersion,
        streamId,
        smoother,
        enableMessageApi,
        transtype,
        lingerInS,
        enableTimestampBasedPacketDelivery
    )
}

/**
 * SrtUrl is a data class that represents an SRT URL as defined by [FFmpeg](https://ffmpeg.org/ffmpeg-protocols.html#srt).
 *
 * If a value is null, FFmpeg default values will be used. If default values are not defined, SRT default values will be used.
 *
 * [listenTimeoutInUs] and [timeoutInMs] are not used.
 */
data class SrtUrl(
    val hostname: String,
    val port: Int,
    val connectTimeoutInMs: Int? = null,
    val flightFlagSize: Int? = 25600,
    val inputBandwidth: Long? = 0,
    val iptos: Int? = 0xB8,
    val ipttl: Int? = 64,
    val latencyInMs: Int? = null,
    val listenTimeoutInMs: Int? = null,
    val maxBandwidth: Long? = 0,
    val mode: Mode? = Mode.CALLER,
    val maxSegmentSize: Int? = 1500,
    val nakReport: Boolean? = true,
    val overheadBandwidth: Int? = 25,
    val passphrase: String? = null,
    val enforcedEncryption: Boolean? = true,
    val kmRefreshRate: Int? = null,
    val kmPreannounce: Int? = null,
    val senderDropDelayInMs: Int? = null,
    val payloadSize: Int? = null,
    val peerLatencyInMs: Int? = null,
    val pbKeyLength: Int? = 0,
    val receiverLatencyInMs: Int? = null,
    val receiveUDPBufferSize: Int? = null,
    val sendUDPBufferSize: Int? = null,
    val timeoutInMs: Int? = null,
    val enableTooLatePacketDrop: Boolean? = null,
    val sendBufferSize: Int? = null,
    val recvBufferSize: Int? = null,
    val lossMaxTTL: Int? = null,
    val minVersion: Int? = null,
    val streamId: String? = null,
    val smoother: Transtype? = null,
    val enableMessageApi: Boolean? = false,
    val transtype: Transtype? = null,
    val lingerInS: Int? = null,
    val enableTimestampBasedPacketDelivery: Boolean? = null,
) {
    init {
        hostname.removePrefix(SRT_PREFIX)
        require(hostname.isNotBlank()) { "Invalid host $hostname" }
        require(port > 0) { "Invalid port $port" }
        require(port < 65536) { "Invalid port $port" }

        if (timeoutInMs != null) {
            Log.w(TAG, "timeoutInMs is not used")
        }
        if (listenTimeoutInMs != null) {
            Log.w(TAG, "listenTimeoutInMs is not used")
        }
    }

    internal fun preBindApplyTo(socket: Socket) {
        iptos?.let { socket.setSockFlag(SockOpt.IPTOS, it) }
        ipttl?.let { socket.setSockFlag(SockOpt.IPTTL, it) }
        maxSegmentSize?.let { socket.setSockFlag(SockOpt.MSS, it) }
        receiveUDPBufferSize?.let { socket.setSockFlag(SockOpt.UDP_RCVBUF, it) }
        sendUDPBufferSize?.let { socket.setSockFlag(SockOpt.UDP_SNDBUF, it) }
        sendBufferSize?.let { socket.setSockFlag(SockOpt.SNDBUF, it) }
        recvBufferSize?.let { socket.setSockFlag(SockOpt.RCVBUF, it) }
    }

    internal fun preApplyTo(socket: Socket) {
        connectTimeoutInMs?.let { socket.setSockFlag(SockOpt.CONNTIMEO, it) }
        flightFlagSize?.let { socket.setSockFlag(SockOpt.FC, it) }

        latencyInMs?.let { socket.setSockFlag(SockOpt.LATENCY, it) }

        nakReport?.let { socket.setSockFlag(SockOpt.NAKREPORT, it) }

        passphrase?.let { socket.setSockFlag(SockOpt.PASSPHRASE, it) }
        enforcedEncryption?.let { socket.setSockFlag(SockOpt.ENFORCEDENCRYPTION, it) }
        kmRefreshRate?.let { socket.setSockFlag(SockOpt.KMREFRESHRATE, it) }
        kmPreannounce?.let { socket.setSockFlag(SockOpt.KMPREANNOUNCE, it) }

        payloadSize?.let { socket.setSockFlag(SockOpt.PAYLOADSIZE, it) }
        peerLatencyInMs?.let { socket.setSockFlag(SockOpt.PEERLATENCY, it) }
        pbKeyLength?.let { socket.setSockFlag(SockOpt.PBKEYLEN, it) }
        receiverLatencyInMs?.let { socket.setSockFlag(SockOpt.RCVLATENCY, it) }

        enableTooLatePacketDrop?.let { socket.setSockFlag(SockOpt.TLPKTDROP, it) }

        minVersion?.let { socket.setSockFlag(SockOpt.MINVERSION, it) }
        streamId?.let { socket.setSockFlag(SockOpt.STREAMID, it) }
        smoother?.let { socket.setSockFlag(SockOpt.CONGESTION, it) }
        enableMessageApi?.let { socket.setSockFlag(SockOpt.MESSAGEAPI, it) }
        transtype?.let { socket.setSockFlag(SockOpt.TRANSTYPE, it) }

        enableTimestampBasedPacketDelivery?.let { socket.setSockFlag(SockOpt.TSBPDMODE, it) }
    }

    internal fun postApplyTo(socket: Socket) {
        inputBandwidth?.let { socket.setSockFlag(SockOpt.INPUTBW, it) }
        maxBandwidth?.let { socket.setSockFlag(SockOpt.MAXBW, it) }
        overheadBandwidth?.let { socket.setSockFlag(SockOpt.OHEADBW, it) }
        senderDropDelayInMs?.let { socket.setSockFlag(SockOpt.SNDDROPDELAY, it) }
        lossMaxTTL?.let { socket.setSockFlag(SockOpt.LOSSMAXTTL, it) }
        lingerInS?.let { socket.setSockFlag(SockOpt.LINGER, it) }
    }

    enum class Mode(val value: String) {
        LISTENER("listener"),
        CALLER("caller"),
        RENDEZ_VOUS("rendezvous");

        companion object {
            fun entryOf(value: String) = entries.first {
                it.value == value
            }
        }
    }

    companion object {
        private const val TAG = "SrtUrl"

        internal const val SRT_SCHEME = "srt"
        private const val SRT_PREFIX = "$SRT_SCHEME://"

        internal const val CONNECTION_TIMEOUT_QUERY_PARAMETER = "connect_timeout"
        internal const val FFS_QUERY_PARAMETER = "ffs"
        internal const val INPUT_BANDWIDTH_QUERY_PARAMETER = "inputbw"
        internal const val IPTOS_QUERY_PARAMETER = "iptos"
        internal const val IPTTL_QUERY_PARAMETER = "ipttl"
        internal const val LATENCY_QUERY_PARAMETER = "latency"
        internal const val LISTEN_TIMEOUT_QUERY_PARAMETER = "listen_timeout"
        internal const val MAX_BANDWIDTH_QUERY_PARAMETER = "maxbw"
        internal const val MODE_QUERY_PARAMETER = "mode"
        internal const val MAX_SEGMENT_SIZE_QUERY_PARAMETER = "mss"
        internal const val NAK_REPORT_QUERY_PARAMETER = "nakreport"
        internal const val OVERHEAD_BANDWIDTH_QUERY_PARAMETER = "oheadbw"
        internal const val PASS_PHRASE_QUERY_PARAMETER = "passphrase"
        internal const val ENFORCED_ENCRYPTION_QUERY_PARAMETER = "enforced_encryption"
        internal const val KM_REFRESH_RATE_QUERY_PARAMETER = "kmrefreshrate"
        internal const val KM_PREANNOUNCE_QUERY_PARAMETER = "kmpreannounce"
        internal const val SENDER_DROP_DELAY_QUERY_PARAMETER = "snddropdelay"
        internal const val PAYLOAD_SIZE_QUERY_PARAMETER = "payload_size"
        internal const val PACKET_SIZE_QUERY_PARAMETER = "pkt_size"
        internal const val PEER_LATENCY_QUERY_PARAMETER = "peerlatency"
        internal const val PB_KEY_LENGTH_QUERY_PARAMETER = "pbkeylen"
        internal const val RECEIVER_LATENCY_QUERY_PARAMETER = "rcvlatency"
        internal const val RECEIVE_UDP_BUFFER_SIZE_QUERY_PARAMETER = "recv_buffer_size"
        internal const val SEND_UDP_BUFFER_SIZE_QUERY_PARAMETER = "send_buffer_size"
        internal const val TIMEOUT_QUERY_PARAMETER = "timeout"
        internal const val ENABLE_TOO_LATE_PACKET_DROP_QUERY_PARAMETER = "tlpktdrop"
        internal const val SEND_BUFFER_SIZE_QUERY_PARAMETER = "sndbuf"
        internal const val RECV_BUFFER_SIZE_QUERY_PARAMETER = "rcvbuf"
        internal const val LOSS_MAX_TTL_QUERY_PARAMETER = "lossmaxttl"
        internal const val MIN_VERSION_QUERY_PARAMETER = "minversion"
        internal const val STREAM_ID_QUERY_PARAMETER = "streamid"
        internal const val SRT_STREAM_ID_QUERY_PARAMETER = "srt_streamid"
        internal const val SMOOTHER_QUERY_PARAMETER = "smoother"
        internal const val ENABLE_MESSAGE_API_QUERY_PARAMETER = "messageapi"
        internal const val TRANSTYPE_QUERY_PARAMETER = "transtype"
        internal const val LINGER_QUERY_PARAMETER = "linger"
        internal const val ENABLE_TIMESTAMP_BASED_PACKET_DELIVERY_QUERY_PARAMETER = "tsbpd"

        internal val supportedQueryParameterList = listOf(
            CONNECTION_TIMEOUT_QUERY_PARAMETER,
            FFS_QUERY_PARAMETER,
            INPUT_BANDWIDTH_QUERY_PARAMETER,
            IPTOS_QUERY_PARAMETER,
            IPTTL_QUERY_PARAMETER,
            LATENCY_QUERY_PARAMETER,
            LISTEN_TIMEOUT_QUERY_PARAMETER,
            MAX_BANDWIDTH_QUERY_PARAMETER,
            MODE_QUERY_PARAMETER,
            MAX_SEGMENT_SIZE_QUERY_PARAMETER,
            NAK_REPORT_QUERY_PARAMETER,
            OVERHEAD_BANDWIDTH_QUERY_PARAMETER,
            PASS_PHRASE_QUERY_PARAMETER,
            ENFORCED_ENCRYPTION_QUERY_PARAMETER,
            KM_REFRESH_RATE_QUERY_PARAMETER,
            KM_PREANNOUNCE_QUERY_PARAMETER,
            SENDER_DROP_DELAY_QUERY_PARAMETER,
            PAYLOAD_SIZE_QUERY_PARAMETER,
            PACKET_SIZE_QUERY_PARAMETER,
            PEER_LATENCY_QUERY_PARAMETER,
            PB_KEY_LENGTH_QUERY_PARAMETER,
            RECEIVER_LATENCY_QUERY_PARAMETER,
            RECEIVE_UDP_BUFFER_SIZE_QUERY_PARAMETER,
            SEND_UDP_BUFFER_SIZE_QUERY_PARAMETER,
            TIMEOUT_QUERY_PARAMETER,
            ENABLE_TOO_LATE_PACKET_DROP_QUERY_PARAMETER,
            SEND_BUFFER_SIZE_QUERY_PARAMETER,
            RECV_BUFFER_SIZE_QUERY_PARAMETER,
            LOSS_MAX_TTL_QUERY_PARAMETER,
            MIN_VERSION_QUERY_PARAMETER,
            STREAM_ID_QUERY_PARAMETER,
            SRT_STREAM_ID_QUERY_PARAMETER,
            SMOOTHER_QUERY_PARAMETER,
            ENABLE_MESSAGE_API_QUERY_PARAMETER,
            TRANSTYPE_QUERY_PARAMETER,
            LINGER_QUERY_PARAMETER,
            ENABLE_TIMESTAMP_BASED_PACKET_DELIVERY_QUERY_PARAMETER
        )

        init {
            Srt.startUp()
        }
    }
}