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
package io.github.thibaultbee.srtdroid.models

import android.util.Pair
import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.enums.ErrorType
import io.github.thibaultbee.srtdroid.enums.RejectReasonCode
import io.github.thibaultbee.srtdroid.enums.SockOpt
import io.github.thibaultbee.srtdroid.enums.SockStatus
import io.github.thibaultbee.srtdroid.listeners.SocketListener
import io.github.thibaultbee.srtdroid.models.rejectreason.InternalRejectReason
import io.github.thibaultbee.srtdroid.models.rejectreason.PredefinedRejectReason
import io.github.thibaultbee.srtdroid.models.rejectreason.RejectReason
import io.github.thibaultbee.srtdroid.models.rejectreason.UserDefinedRejectReason
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.BindException
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.StandardProtocolFamily
import java.nio.ByteBuffer

/**
 * This class represents a SRT socket.
 * To avoid creating an unresponsive UI, don't perform SRT network operations on the main thread.
 * Once it has been called, you must release Srt context with [Srt.cleanUp] when application leaves.
 */
class Socket
private constructor(private val srtsocket: Int) : Closeable {
    companion object {
        @JvmStatic
        private external fun nativeCreateSocket(): Int

        @JvmStatic
        private external fun nativeCreateSocket(
            af: StandardProtocolFamily,
            type: Int,
            protocol: Int
        ): Int

        init {
            Srt.startUp()
        }
    }

    /**
     * Sets up the SRT socket listener. Use it to monitor SRT socket connection.
     *
     * **See Also:** [srt_connect_callback](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect_callback)
     *
     * @see [SocketListener]
     */
    var listener: SocketListener? = null

    /**
     * Deprecated version of [Socket] constructor. Argument is ignored.
     * Also, it crashes on old Android version (where [StandardProtocolFamily] does not exist).
     *
     * You shall assert that the SRT socket is valid with [isValid].
     *
     * **See Also:** [srt_socket](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_socket)
     */
    @Deprecated(message = "Use Socket() instead", replaceWith = ReplaceWith("Socket()"))
    constructor(af: StandardProtocolFamily) : this(nativeCreateSocket(af, 0, 0))

    /**
     * Creates an SRT socket.
     *
     * You shall assert that the SRT socket is valid with [isValid]
     *
     * **See Also:** [srt_create_socket](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_create_socket)
     */
    constructor() : this(nativeCreateSocket())

    private external fun nativeIsValid(): Boolean

    /**
     * Check if the SRT socket is a valid SRT socket.
     *
     * @return true if the SRT socket is valid, otherwise false
     */
    val isValid: Boolean
        get() = nativeIsValid()

    private external fun nativeBind(address: InetSocketAddress): Int

    /**
     * Binds the socket to a local address.
     *
     * **See Also:** [srt_bind](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind)
     *
     * @param address the [InetSocketAddress] to bind to
     *
     * @throws BindException if bind has failed
     */
    fun bind(address: InetSocketAddress) {
        if (nativeBind(address) != 0) {
            throw BindException(Error.lastErrorMessage)
        }
    }

    /**
     * Binds the socket to a local address.
     *
     * **See Also:** [srt_bind](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind)
     *
     * @param address the address to bind to
     * @param port the port to bind to
     * @throws BindException if bind has failed
     */
    fun bind(address: String, port: Int) = bind(InetSocketAddress(address, port))

    /**
     * Binds the socket to a local address.
     *
     * **See Also:** [srt_bind](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind)
     *
     * @param address the [InetAddress] to bind to
     * @param port the port to bind to
     *
     * @throws BindException if bind has failed
     */
    fun bind(address: InetAddress, port: Int) = bind(InetSocketAddress(address, port))

    private external fun nativeGetSockState(): SockStatus

    /**
     * Gets the current status of the socket.
     *
     * **See Also:** [srt_getsockstate](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockstate)
     *
     * @return the current [SockStatus]
     */
    val sockState: SockStatus
        get() = nativeGetSockState()

    private external fun nativeClose(): Int

    /**
     * Closes the socket or group and frees all used resources.
     *
     * **See Also:** [srt_close](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_close)
     *
     * @throws SocketException if close failed
     */
    override fun close() {
        if (nativeClose() != 0) {
            throw SocketException(Error.lastErrorMessage)
        }
    }

    // Connecting
    /**
     * Internal method. Do not use, use [SocketListener.onListen] instead.
     *
     * @see [listener]
     */
    private fun onListen(
        ns: Socket,
        hsVersion: Int,
        peerAddress: InetSocketAddress,
        streamId: String
    ): Int {
        return listener?.onListen(ns, hsVersion, peerAddress, streamId)
            ?: 0 // By default, accept incoming connection
    }

    private external fun nativeListen(backlog: Int): Int

    /**
     * Sets up the listening state on a socket.
     *
     * **See Also:** [srt_listen](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_listen)
     *
     * @param backlog the number of sockets that may be allowed to wait until they are accepted
     * @throws SocketException if listen failed
     * @see [listener]
     */
    fun listen(backlog: Int) {
        if (nativeListen(backlog) != 0) {
            throw SocketException(Error.lastErrorMessage)
        }
    }

    private external fun nativeAccept(): Pair<Socket, InetSocketAddress?>

    /**
     * Accepts a pending connection.
     *
     * **See Also:** [srt_accept](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_accept)
     *
     * @return a pair containing the new Socket connection and the IP address and port specification of the remote device.
     * @throws SocketException if returned SRT socket is not valid
     */
    fun accept(): Pair<Socket, InetSocketAddress?> {
        val pair = nativeAccept()
        if (!pair.first.isValid) {
            throw SocketException(Error.lastErrorMessage)
        }
        return pair
    }

    /**
     * Internal method. Do not use, use [SocketListener.onConnectionLost] instead.
     *
     * @see [listener]
     */
    private fun onConnect(
        ns: Socket,
        error: ErrorType,
        peerAddress: InetSocketAddress,
        token: Int
    ) {
        listener?.onConnectionLost(ns, error, peerAddress, token)
    }

    private external fun nativeConnect(address: InetSocketAddress): Int

    /**
     * Connects a socket to a specified address and port.
     *
     * **See Also:** [srt_connect](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect)
     *
     * @param address the [InetSocketAddress] to connect to
     * @throws ConnectException if connection has failed
     */
    fun connect(address: InetSocketAddress) {
        if (nativeConnect(address) != 0) {
            throw ConnectException(Error.lastErrorMessage)
        }
    }

    /**
     * Connects a socket to a specified address and port.
     *
     * **See Also:** [srt_connect](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect)
     *
     * @param address the address to connect to
     * @param port the port to connect to
     * @throws ConnectException if connection has failed
     */
    fun connect(address: String, port: Int) = connect(InetSocketAddress(address, port))

    /**
     * Connects a socket to a specified address and port.
     *
     * **See Also:** [srt_connect](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect)
     *
     * @param address the [InetAddress] to connect to
     * @param port the port to connect to
     * @throws ConnectException if connection has failed
     */
    fun connect(address: InetAddress, port: Int) = connect(InetSocketAddress(address, port))

    private external fun nativeRendezVous(
        localAddress: InetSocketAddress,
        remoteAddress: InetSocketAddress
    ): Int

    /**
     * Performs a rendezvous connection.
     *
     * **See Also:** [srt_rendezvous](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_rendezvous)
     *
     * @param localAddress the local [InetSocketAddress] to bind to
     * @param remoteAddress the remote [InetSocketAddress] to connect to
     * @throws SocketException if rendezvous connection has failed
     */
    fun rendezVous(
        localAddress: InetSocketAddress,
        remoteAddress: InetSocketAddress
    ) {
        if (nativeRendezVous(localAddress, remoteAddress) != 0) {
            throw SocketException(Error.lastErrorMessage)
        }
    }

    /**
     * Performs a rendezvous connection.
     *
     * **See Also:** [srt_rendezvous](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_rendezvous)
     *
     * @param localAddress the local address to bind to
     * @param remoteAddress the remote address to connect to
     * @throws SocketException if rendezvous connection has failed
     */
    fun rendezVous(localAddress: String, remoteAddress: String, port: Int) = rendezVous(
        InetSocketAddress(localAddress, port),
        InetSocketAddress(remoteAddress, port)
    )

    /**
     * Performs a rendezvous connection.
     *
     * **See Also:** [srt_rendezvous](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_rendezvous)
     *
     * @param localAddress the local [InetAddress] to bind to
     * @param remoteAddress the remote [InetAddress] to connect to
     * @throws SocketException if rendezvous connection has failed
     */
    fun rendezVous(localAddress: InetAddress, remoteAddress: InetAddress, port: Int) = rendezVous(
        InetSocketAddress(localAddress, port),
        InetSocketAddress(remoteAddress, port)
    )

    // Options and properties
    private external fun nativeGetPeerName(): InetSocketAddress?

    /**
     * Retrieves the remote [InetSocketAddress] to which the SRT socket is connected.
     *
     * **See Also:** [srt_getpeername](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getpeername)
     *
     * @return the remote [InetSocketAddress] if SRT socket is connected and valid. Otherwise, it returns a null.
     * @see [inetAddress] and [port]
     * @throws [SocketException] if SRT socket is invalid or not connected
     */
    val peerName: InetSocketAddress
        get() = nativeGetPeerName() ?: throw SocketException(Error.lastErrorMessage)

    /**
     * Retrieves the [InetAddress] to which the socket is connected.
     *
     * **See Also:** [srt_getpeername](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getpeername)
     *
     * @return the remote [InetAddress] if SRT socket is connected and valid. Otherwise, it returns a null.
     * @see [peerName] and [port]
     * @throws [SocketException] if SRT socket is invalid or not connected
     */
    val inetAddress: InetAddress
        get() = peerName.address

    /**
     * Retrieves the port to which the SRT socket is connected.
     *
     * **See Also:** [srt_getpeername](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getpeername)
     *
     * @return the remote port if SRT socket is connected and valid. Otherwise, it returns a 0.
     * @see [peerName] and [inetAddress]
     * @throws [SocketException] if SRT socket is invalid or not connected
     */
    val port: Int
        get() = peerName.port

    private external fun nativeGetSockName(): InetSocketAddress?

    /**
     * Extracts the [InetSocketAddress] to which the socket was bound.
     *
     * **See Also:** [srt_getsockname](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockname)
     *
     * @return if socket is bound and valid, it returns the local [InetSocketAddress]. Otherwise, it returns a null.
     * @throws [SocketException] if SRT socket is invalid or not bound
     * @see [localAddress] and [localPort]
     */
    val sockName: InetSocketAddress
        get() = nativeGetSockName() ?: throw SocketException(Error.lastErrorMessage)

    /**
     * Extracts the [InetAddress] to which the socket was bound.
     *
     * **See Also:** [srt_getsockname](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockname)
     *
     * @return if socket is bound and valid, it returns the local [InetAddress]. Otherwise, it returns a null.
     * @throws [SocketException] if SRT socket is invalid or not bound
     * @see [sockName] and [localPort]
     */
    val localAddress: InetAddress
        get() = sockName.address

    /**
     * Extracts the port to which the socket was bound.
     *
     * **See Also:** [srt_getsockname](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockname)
     *
     * @return if socket is bound and valid, it returns the local port. Otherwise, it returns a 0.
     * @throws [SocketException] if SRT socket is invalid or not bound
     * @see [sockName] and [localPort]
     */
    val localPort: Int
        get() = sockName.port

    private external fun nativeGetSockFlag(opt: SockOpt): Any?

    /**
     * Gets the value of the given socket option.
     *
     * **See Also:** [srt_getsockflag](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockflag)
     *
     * @param opt the [SockOpt] to get
     * @return an object containing the [SockOpt] value. Type depends of the specified [opt].
     * @throws IOException if can't get [SockOpt]
     * @see [setSockFlag]
     */
    fun getSockFlag(opt: SockOpt): Any {
        return nativeGetSockFlag(opt) ?: throw IOException(Error.lastErrorMessage)
    }

    private external fun nativeSetSockFlag(opt: SockOpt, value: Any): Int

    /**
     * Sets the value of the given socket option.
     *
     * **See Also:** [srt_setsockflag](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_setsockflag)
     *
     * @param opt the [SockOpt] to set
     * @param value the [SockOpt] value to set. Type depends of the specified [opt].
     * @throws IOException if can't set [SockOpt]
     * @see [getSockFlag]
     */
    fun setSockFlag(opt: SockOpt, value: Any) {
        if (nativeSetSockFlag(opt, value) != 0) {
            throw IOException(Error.lastErrorMessage)
        }
    }

    // Transmission
    // Send
    private external fun nativeSend(msg: ByteArray, offset: Int, size: Int): Int
    private external fun nativeSend(msg: ByteBuffer, offset: Int, size: Int): Int

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_send](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_send)
     *
     * @param msg the [ByteBuffer] to send. It must be allocate with [ByteBuffer.allocateDirect]. It sends ByteBuffer from [ByteBuffer.position] to [ByteBuffer.limit].
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: ByteBuffer): Int {
        require(msg.isDirect) { "msg must be a direct ByteBuffer" }

        val byteSent = nativeSend(msg, msg.position(), msg.remaining())
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return byteSent
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_send](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_send)
     *
     * @param msg the [ByteArray] to send
     * @param offset the offset of the [msg]
     * @param size the size of the [msg] to send
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: ByteArray, offset: Int, size: Int): Int {
        val byteSent = nativeSend(msg, offset, size)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return byteSent
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_send](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_send)
     *
     * @param msg the [ByteArray] to send
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: ByteArray) = send(msg, 0, msg.size)

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_send](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_send)
     *
     * @param msg the [String] to send
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: String) = send(msg.toByteArray())

    private external fun nativeSend(
        msg: ByteBuffer,
        offset: Int,
        size: Int,
        ttl: Int = -1,
        inOrder: Boolean = false
    ): Int

    private external fun nativeSend(
        msg: ByteArray,
        offset: Int,
        size: Int,
        ttl: Int = -1,
        inOrder: Boolean = false
    ): Int

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_sendmsg](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg)
     *
     * @param msg the [ByteBuffer] to send. It must be allocate with [ByteBuffer.allocateDirect]. It sends ByteBuffer from [ByteBuffer.position] to [ByteBuffer.limit].
     * @param ttl the time (in ms) to wait for a successful delivery. -1 means no time limitation.
     * @param inOrder Required to be received in the order of sending.
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(
        msg: ByteBuffer,
        ttl: Int = -1,
        inOrder: Boolean = false
    ): Int {
        require(msg.isDirect) { "msg must be a direct ByteBuffer" }

        val byteSent = nativeSend(msg, msg.position(), msg.remaining(), ttl, inOrder)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return byteSent
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_sendmsg](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg)
     *
     * @param msg the [ByteArray] to send
     * @param offset the offset of the [msg]
     * @param size the size of the [msg] to send
     * @param ttl the time (in ms) to wait for a successful delivery. -1 means no time limitation.
     * @param inOrder Required to be received in the order of sending.
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(
        msg: ByteArray,
        offset: Int,
        size: Int,
        ttl: Int = -1,
        inOrder: Boolean = false
    ): Int {
        val byteSent = nativeSend(msg, offset, size, ttl, inOrder)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return byteSent
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_sendmsg](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg)
     *
     * @param msg the [ByteArray] to send
     * @param ttl the time (in ms) to wait for a successful delivery. -1 means no time limitation.
     * @param inOrder Required to be received in the order of sending.
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: ByteArray, ttl: Int = -1, inOrder: Boolean = false) =
        send(msg, 0, msg.size, ttl, inOrder)

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_sendmsg](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg)
     *
     * @param msg the [String] to send
     * @param ttl the time (in ms) to wait for a successful delivery. -1 means no time limitation.
     * @param inOrder Required to be received in the order of sending.
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: String, ttl: Int = -1, inOrder: Boolean = false) =
        send(msg.toByteArray(), ttl, inOrder)

    private external fun nativeSend(msg: ByteBuffer, offset: Int, size: Int, msgCtrl: MsgCtrl): Int
    private external fun nativeSend(msg: ByteArray, offset: Int, size: Int, msgCtrl: MsgCtrl): Int

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_sendmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg2)
     *
     * @param msg the [ByteBuffer] to send. It must be allocate with [ByteBuffer.allocateDirect]. It sends ByteBuffer from [ByteBuffer.position] to [ByteBuffer.limit].
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: ByteBuffer, msgCtrl: MsgCtrl): Int {
        require(msg.isDirect) { "msg must be a direct ByteBuffer" }

        val byteSent = nativeSend(msg, msg.position(), msg.remaining(), msgCtrl)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return byteSent
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_sendmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg2)
     *
     * @param msg the [ByteArray] to send
     * @param offset the offset of the [msg]
     * @param size the size of the [msg] to send
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: ByteArray, offset: Int, size: Int, msgCtrl: MsgCtrl): Int {
        val byteSent = nativeSend(msg, offset, size, msgCtrl)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return byteSent
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_sendmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg2)
     *
     * @param msg the [ByteArray] to send
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: ByteArray, msgCtrl: MsgCtrl) = send(msg, 0, msg.size, msgCtrl)

    /**
     * Sends a message to a remote party.
     *
     * **See Also:** [srt_sendmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg2)
     *
     * @param msg the [String] to send
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    fun send(msg: String, msgCtrl: MsgCtrl) = send(msg.toByteArray(), msgCtrl)

    /**
     * Returns an output stream for this socket.
     *
     * **See Also:** [srt_sendmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendmsg2)
     *
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return an output stream for writing bytes to this socket.
     * @see [getInputStream]
     */
    fun getOutputStream(msgCtrl: MsgCtrl? = null) =
        SrtSocketOutputStream(this, msgCtrl) as OutputStream

    private class SrtSocketOutputStream(private val socket: Socket, private val msgCtrl: MsgCtrl?) :
        OutputStream() {

        override fun close() {
            socket.close()
        }

        override fun write(oneByte: Int) {
            val buffer = byteArrayOf(oneByte.toByte())
            write(buffer, 0, 1)
        }

        override fun write(
            buffer: ByteArray,
            defaultOffset: Int,
            defaultByteCount: Int
        ) {
            var byteCount = defaultByteCount
            var offset = defaultOffset
            if (socket.isClose) {
                throw SocketException(ErrorType.ESCLOSED.toString())
            }
            if (socket.getSockFlag(SockOpt.MESSAGEAPI) as Boolean) {
                // In case, message API is true, split buffer in payload size buffer.
                val payloadSize = socket.getSockFlag(SockOpt.PAYLOADSIZE) as Int
                while (byteCount > 0) {
                    val bytesWritten = if (msgCtrl != null) {
                        socket.send(buffer, offset, byteCount.coerceAtMost(payloadSize), msgCtrl)
                    } else {
                        socket.send(buffer, offset, byteCount.coerceAtMost(payloadSize))
                    }
                    byteCount -= bytesWritten
                    offset += bytesWritten
                }
            } else {
                if (msgCtrl != null) {
                    socket.send(buffer, offset, byteCount, msgCtrl)
                } else {
                    socket.send(buffer, offset, byteCount)
                }
            }
        }
    }

    // Recv
    private external fun nativeRecv(size: Int): Pair<Int, ByteArray>

    /**
     * Received a message from a remote device
     *
     * **See Also:** [srt_recv](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recv)
     *
     * @param size Size of the expected message.
     * @return a pair containing the number of bytes received and the [ByteArray] message.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     */
    fun recv(size: Int): Pair<Int, ByteArray> {
        val pair = nativeRecv(size)
        val byteReceived = pair.first
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteReceived == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return pair
        }
    }

    private external fun nativeRecv(
        buffer: ByteArray,
        offset: Int,
        byteCount: Int
    ): Pair<Int, ByteArray>

    /**
     * Received a message from a remote device
     *
     * **See Also:** [srt_recv](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recv)
     *
     * @param buffer the [ByteArray] where received data are copied to.
     * @param offset the offset in the specified [buffer].
     * @param byteCount the size of the specified [buffer].
     * @return a pair containing the number of bytes received and the [ByteArray] message. The [ByteArray] points to the [buffer].
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     */
    fun recv(
        buffer: ByteArray,
        offset: Int = 0,
        byteCount: Int = buffer.size
    ): Pair<Int, ByteArray> {
        val pair = nativeRecv(buffer, offset, byteCount)
        val byteReceived = pair.first
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteReceived == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return pair
        }
    }

    private external fun nativeRecv(size: Int, msgCtrl: MsgCtrl): Pair<Int, ByteArray>

    /**
     * Received a message from a remote device
     *
     * **See Also:** [srt_recvmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recvmsg2)
     *
     * @param size Size of the expected message.
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return a pair containing the number of bytes received and the [ByteArray] message.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     */
    fun recv(size: Int, msgCtrl: MsgCtrl): Pair<Int, ByteArray> {
        val pair = nativeRecv(size, msgCtrl)
        val byteReceived = pair.first
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteReceived == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return pair
        }
    }

    private external fun nativeRecv(
        buffer: ByteArray,
        offset: Int,
        byteCount: Int,
        msgCtrl: MsgCtrl
    ): Pair<Int, ByteArray>

    /**
     * Received a message from a remote device
     *
     * **See Also:** [srt_recvmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recvmsg2)
     *
     * @param buffer the [ByteArray] where received data are copied to.
     * @param offset the offset in the specified [buffer].
     * @param byteCount the size of the specified [buffer].
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return a pair containing the number of bytes received and the [ByteArray] message. The [ByteArray] points to the [buffer].
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     */
    fun recv(
        buffer: ByteArray,
        offset: Int = 0,
        byteCount: Int = buffer.size,
        msgCtrl: MsgCtrl
    ): Pair<Int, ByteArray> {
        val pair = nativeRecv(buffer, offset, byteCount, msgCtrl)
        val byteReceived = pair.first
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteReceived == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return pair
        }
    }

    /**
     * Returns an input stream for this socket.
     *
     * **See Also:** [srt_recvmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recvmsg2)
     *
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return an input stream for reading bytes from this socket.
     * @see [getOutputStream]
     */
    fun getInputStream(msgCtrl: MsgCtrl? = null) =
        SrtSocketInputStream(this, msgCtrl) as InputStream

    private class SrtSocketInputStream(private val socket: Socket, private val msgCtrl: MsgCtrl?) :
        InputStream() {

        override fun available(): Int {
            return socket.available()
        }

        override fun close() {
            socket.close()
        }

        override fun read(): Int {
            val pair = if (msgCtrl != null) {
                socket.recv(1, msgCtrl)
            } else {
                socket.recv(1)
            }
            val byteReceived = pair.first
            val byteArray = pair.second
            return if (byteReceived > 0) {
                byteArray[0].toInt()
            } else {
                -1
            }
        }

        override fun read(buffer: ByteArray, offset: Int, byteCount: Int): Int {
            if (byteCount == 0) {
                return 0
            }

            val pair = if (msgCtrl != null) {
                socket.recv(buffer, offset, byteCount, msgCtrl)
            } else {
                socket.recv(buffer, offset, byteCount)
            }

            return pair.first
        }
    }

    // File
    private external fun nativeSendFile(
        path: String,
        offset: Long,
        size: Long,
        block: Int = 364000
    ): Long

    /**
     * Sends a specified file.
     *
     * **See Also:** [srt_sendfile](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendfile)
     *
     * @param path the path of the file to send
     * @param offset the offset used to read file from
     * @param size the size of the file
     * @param block the size of the single block to read at once before writing it to a file
     * @return the size (>0) of the transmitted data of a file.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recvFile]
     */
    fun sendFile(path: String, offset: Long = 0, size: Long, block: Int = 364000): Long {
        val byteSent = nativeSendFile(path, offset, size, block)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteSent == 0L -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return byteSent
        }
    }

    /**
     * Sends a specified file.
     *
     * **See Also:** [srt_sendfile](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendfile)
     *
     * @param file the [File] to send
     * @param offset the offset used to read file from
     * @param size the size of the file
     * @param block the size of the single block to read at once before writing it to a file
     * @return the size (>0) of the transmitted data of a file.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recvFile]
     */
    fun sendFile(file: File, offset: Long = 0, size: Long, block: Int = 364000) =
        sendFile(file.path, offset, size, block)

    /**
     * Sends a specified file.
     *
     * **See Also:** [srt_sendfile](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sendfile)
     *
     * @param file the [File] to send
     * @param block the size of the single block to read at once before writing it to a file
     * @return the size (>0) of the transmitted data of a file.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recvFile]
     */
    fun sendFile(file: File, block: Int = 364000) =
        sendFile(file.path, 0, file.length(), block)

    private external fun nativeRecvFile(
        path: String,
        offset: Long,
        size: Long,
        block: Int = 7280000
    ): Long

    /**
     * Receives a file. File is create in [path].
     *
     * **See Also:** [srt_recvfile](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recvfile)
     *
     * @param path the path where to write received data
     * @param offset the offset used to write file
     * @param size the size of the file
     * @param block the size of the single block to read at once before writing it to a file
     * @return the size (>0) of the received data of a file.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [sendFile]
     */
    fun recvFile(path: String, offset: Long = 0, size: Long, block: Int = 7280000): Long {
        val byteReceived = nativeRecvFile(path, offset, size, block)
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }

            byteReceived == 0L -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }

            else -> return byteReceived
        }
    }

    /**
     * Receives a file. File is create in [file].
     *
     * **See Also:** [srt_recvfile](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recvfile)
     *
     * @param file the [File] where to write received data
     * @param offset the offset used to write file
     * @param size the size of the file
     * @param block the size of the single block to read at once before writing it to a file
     * @return the size (>0) of the received data of a file.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [sendFile]
     */
    fun recvFile(file: File, offset: Long = 0, size: Long, block: Int = 7280000) =
        recvFile(file.path, offset, size, block)

    // Reject reason
    private external fun nativeGetRejectReason(): Int
    private external fun nativeSetRejectReason(rejectReason: Int): Int

    /**
     * Set/get detailed reason for a failed connection attempt.
     *
     * @see [InternalRejectReason], [PredefinedRejectReason] and [UserDefinedRejectReason]
     */
    var rejectReason: RejectReason
        /**
         * Get detailed reason for a failed connection attempt.
         *
         * **See Also:** [srt_getrejectreason](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getrejectreason)
         *
         * @return the object describing the rejection reason. Could be either [InternalRejectReason], [PredefinedRejectReason] or [UserDefinedRejectReason]
         */
        get() {
            val code = nativeGetRejectReason()
            return when {
                code < RejectReasonCode.PREDEFINED_OFFSET -> InternalRejectReason(RejectReasonCode.values()[code])
                code < RejectReasonCode.USERDEFINED_OFFSET -> PredefinedRejectReason(code - RejectReasonCode.PREDEFINED_OFFSET)
                else -> UserDefinedRejectReason(code - RejectReasonCode.USERDEFINED_OFFSET)
            }
        }
        /**
         * Set detailed reason for a failed connection attempt. You can not set [InternalRejectReason].
         *
         * **See Also:** [srt_setrejectreason](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_setrejectreason)
         *
         * @param value the object describing the rejection reason. Could be either [InternalRejectReason], [PredefinedRejectReason] or [UserDefinedRejectReason]
         * @throws [SocketException] if action has failed
         */
        set(value) {
            val code = when (value) {
                is InternalRejectReason -> { // Forbidden by SRT
                    value.code.ordinal
                }

                is PredefinedRejectReason -> {
                    value.code + RejectReasonCode.PREDEFINED_OFFSET
                }

                is UserDefinedRejectReason -> {
                    value.code + RejectReasonCode.USERDEFINED_OFFSET
                }

                else -> RejectReasonCode.UNKNOWN.ordinal
            }
            if (nativeSetRejectReason(code) != 0) {
                throw SocketException(Error.lastErrorMessage)
            }
        }

    // Performance tracking
    /**
     * Reports the current statistics.
     *
     * **See Also:** [srt_bstats](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bstats)
     *
     * @param clear true if the statistics should be cleared after retrieval
     * @return the current [Stats]
     */
    external fun bstats(clear: Boolean): Stats

    /**
     * Reports the current statistics.
     *
     * **See Also:** [srt_bistats](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bistats)
     *
     * @param clear true if the statistics should be cleared after retrieval
     * @param instantaneous true if the statistics should use instant data, not moving averages
     * @return the current [Stats]
     */
    external fun bistats(clear: Boolean, instantaneous: Boolean): Stats

    // Time access
    private external fun nativeGetConnectionTime(): Long

    /**
     * Gets the time when SRT socket was open to establish a connection.
     *
     * **See Also:** [srt_connection_time](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connection_time)
     *
     * @return the connection time in microseconds
     * @throws [SocketException] if SRT socket is not valid
     */
    val connectionTime: Long
        get() {
            val connectionTime = nativeGetConnectionTime()
            if (connectionTime < 0) {
                throw SocketException(Error.lastErrorMessage)
            }
            return connectionTime
        }

    // Android Socket like API
    /**
     * Sets/gets the value of the [SockOpt.RCVBUF] option for this SRT socket.
     *
     * **See Also:** [SRTO_RCVBUF](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md#SRTO_RCVBUF)
     */
    var receiveBufferSize: Int
        /**
         * Gets the value of the [SockOpt.RCVBUF] option for this SRT socket.
         *
         * @return the receive buffer size in bytes
         * @throws IOException if can't get [SockOpt]
         */
        get() = getSockFlag(SockOpt.RCVBUF) as Int
        /**
         * Sets the value of the [SockOpt.RCVBUF] option for this SRT socket.
         *
         * @param value receive buffer size in bytes
         * @throws IOException if can't set [SockOpt]
         */
        set(value) {
            setSockFlag(SockOpt.RCVBUF, value)
        }

    /**
     * Sets/gets the value of the [SockOpt.SNDBUF] option for this SRT socket.
     *
     * **See Also:** [SRTO_SNDBUF](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md#SRTO_SNDBUF)
     */
    var sendBufferSize: Int
        /**
         * Gets the value of the [SockOpt.SNDBUF] option for this SRT socket.
         *
         * @return the send buffer size in bytes
         * @throws IOException if can't get [SockOpt]
         */
        get() = getSockFlag(SockOpt.SNDBUF) as Int
        /**
         * Sets the value of the [SockOpt.SNDBUF] option for this SRT socket.
         *
         * @param value send buffer size in bytes
         * @throws IOException if can't set [SockOpt]
         */
        set(value) {
            setSockFlag(SockOpt.SNDBUF, value)
        }

    /**
     * Tests if [SockOpt.REUSEADDR] is enabled.
     *
     * **See Also:** [SRTO_REUSEADDR](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md#srto_reuseaddr)
     */
    var reuseAddress: Boolean
        /**
         * Gets the value of the [SockOpt.REUSEADDR] option for this SRT socket.
         *
         * @return true if it allows the SRT socket to use the binding address used already by another SRT socket in the same application, otherwise false
         * @throws IOException if can't get [SockOpt]
         */
        get() = getSockFlag(SockOpt.REUSEADDR) as Boolean
        /**
         * Sets the value of the [SockOpt.REUSEADDR] option for this SRT socket.
         *
         * @param value true if it allows the SRT socket to use the binding address used already by another SRT socket in the same application, otherwise false
         * @throws IOException if can't set [SockOpt]
         */
        set(value) {
            setSockFlag(SockOpt.REUSEADDR, value)
        }

    /**
     * Returns setting for [SockOpt.LINGER].
     *
     * **See Also:** [SRTO_LINGER](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md#srto_linger)
     */
    var soLinger: Int
        /**
         * Gets the value of the [SockOpt.LINGER] option for this SRT socket.
         *
         * @return linger time on close in seconds
         * @throws IOException if can't get [SockOpt]
         */
        get() = getSockFlag(SockOpt.LINGER) as Int
        /**
         * Sets the value of the [SockOpt.LINGER] option for this SRT socket.
         *
         * @param value the linger time on close
         * @throws IOException if can't set [SockOpt]
         */
        set(value) {
            setSockFlag(SockOpt.LINGER, value)
        }

    /**
     * Tests if the SRT socket is bound.
     *
     * @return true if the SRT socket is bound, otherwise false
     */
    val isBound: Boolean
        get() = sockState == SockStatus.OPENED

    /**
     * Tests if the SRT socket is closed.
     *
     * @return true if the SRT socket is closed, otherwise false
     */
    val isClose: Boolean
        get() = (sockState == SockStatus.CLOSED) || (sockState == SockStatus.NONEXIST)

    /**
     * Tests if the SRT socket is connected.
     *
     * @return true if the SRT socket is connected, otherwise false
     */
    val isConnected: Boolean
        get() = sockState == SockStatus.CONNECTED

    /**
     * Get the size of the available data in the receive buffer.
     *
     * @return the size of the available data in the receive buffer
     * @throws IOException if can't get [SockOpt]
     */
    fun available(): Int = getSockFlag(SockOpt.RCVDATA) as Int
}