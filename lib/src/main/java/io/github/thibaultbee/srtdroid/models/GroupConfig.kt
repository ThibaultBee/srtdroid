package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.enums.ErrorType
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * Entry points for group connections
 *
 * **See Also:** [SRT_SOCKGROUPCONFIG](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sockgroupconfig)
 */
data class GroupConfig(
    /**
     * The member socket (filled back as output)
     */
    val socket: Socket,
    /**
     * The address to which the [socket] should be bound
     */
    val src: InetSocketAddress,
    /**
     * The address to which the [socket] should be connected
     */
    val peer: InetSocketAddress,
    /**
     * The weight parameter for the link (group-type dependent)
     */
    val weight: Short,
    /**
     * The configuration object, if used
     */
    val config: SockOptGroupConfig,
    /**
     * The error of the connecting operation
     */
    val error: ErrorType,
    /**
     * An integer value unique for every connection, or -1 if unused
     */
    val token: Int
) {
    companion object {
        init {
            Srt.startUp()
        }

        @JvmStatic
        private external fun nativePrepareDefaultConfig(
            local: InetSocketAddress?,
            remote: InetSocketAddress
        ): GroupConfig

        /**
         * This function prepares a default [GroupConfig] object as an element of the array you can prepare for [SocketGroup.connect] function.
         *
         * **See Also:** [srt_prepare_endpoint](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_prepare_endpoint)
         *
         * @param local the [InetSocketAddress] to bind to
         * @param remote the [InetSocketAddress] to connect to
         * @throws ConnectException if connection has failed
         */
        fun getDefaultConfig(local: InetSocketAddress? = null, remote: InetSocketAddress) =
            nativePrepareDefaultConfig(local, remote)

        /**
         * This function prepares a default [GroupConfig] object as an element of the array you can prepare for [SocketGroup.connect] function.
         *
         * **See Also:** [srt_prepare_endpoint](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_prepare_endpoint)
         *
         * @param localAddress the address to bind to
         * @param localPort the port to bind to
         * @param remoteAddress the address to connect to
         * @param remotePort the port to connect to
         * @throws ConnectException if connection has failed
         */
        fun getDefaultConfig(
            localAddress: String? = null,
            localPort: Int = 0,
            remoteAddress: String,
            remotePort: Int
        ) =
            nativePrepareDefaultConfig(
                localAddress?.let { InetSocketAddress(localAddress, localPort) },
                InetSocketAddress(remoteAddress, remotePort)
            )

        /**
         * This function prepares a default [GroupConfig] object as an element of the array you can prepare for [SocketGroup.connect] function.
         *
         * **See Also:** [srt_prepare_endpoint](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_prepare_endpoint)
         *
         * @param localAddress the [InetAddress] to bind to
         * @param localPort the port to bind to
         * @param remoteAddress the [InetAddress] to connect to
         * @param remotePort the port to connect to
         * @throws ConnectException if connection has failed
         */
        fun getDefaultConfig(
            localAddress: InetAddress? = null,
            localPort: Int = 0,
            remoteAddress: InetAddress,
            remotePort: Int
        ) =
            nativePrepareDefaultConfig(
                localAddress?.let { InetSocketAddress(localAddress, localPort) },
                InetSocketAddress(remoteAddress, remotePort)
            )
    }
}
