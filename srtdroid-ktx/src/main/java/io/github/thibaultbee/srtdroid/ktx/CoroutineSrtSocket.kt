package io.github.thibaultbee.srtdroid.ktx

import android.util.Log
import android.util.Pair
import io.github.thibaultbee.srtdroid.core.enums.EpollOpt
import io.github.thibaultbee.srtdroid.core.enums.ErrorType
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.SockStatus
import io.github.thibaultbee.srtdroid.core.interfaces.ConfigurableSrtSocket
import io.github.thibaultbee.srtdroid.core.models.Epoll
import io.github.thibaultbee.srtdroid.core.models.MsgCtrl
import io.github.thibaultbee.srtdroid.core.models.SrtError
import io.github.thibaultbee.srtdroid.core.models.SrtSocket
import io.github.thibaultbee.srtdroid.core.models.SrtSocket.ServerListener
import io.github.thibaultbee.srtdroid.core.models.Stats
import io.github.thibaultbee.srtdroid.core.models.rejectreason.InternalRejectReason
import io.github.thibaultbee.srtdroid.core.models.rejectreason.PredefinedRejectReason
import io.github.thibaultbee.srtdroid.core.models.rejectreason.RejectReason
import io.github.thibaultbee.srtdroid.core.models.rejectreason.UserDefinedRejectReason
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.BindException
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resumeWithException
import kotlin.math.min

/**
 * A coroutine-based SRT socket.
 */
class CoroutineSrtSocket
private constructor(
    private val socket: SrtSocket
) :
    ConfigurableSrtSocket, CoroutineScope {
    constructor() : this(SrtSocket())

    init {
        socket.setSockFlag(SockOpt.RCVSYN, false)
        socket.setSockFlag(SockOpt.SNDSYN, false)

        socket.clientListener =
            object : SrtSocket.ClientListener {
                override fun onConnectionLost(
                    ns: SrtSocket,
                    error: ErrorType,
                    peerAddress: InetSocketAddress,
                    token: Int
                ) {
                    if (hasBeenConnected) {
                        socketContext.completeExceptionally(ConnectException(error.toString()))
                        coroutineContext.cancelChildren()
                    }
                }
            }
    }

    private var hasBeenConnected = false

    val socketContext: CompletableJob = Job()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val coroutineContext: CoroutineContext = Dispatchers.IO.limitedParallelism(1)

    /**
     * Flow of incoming sockets.
     * It is a hook before returning from accept. Reject an incoming connection by throwing an exception.
     * Only for server sockets.
     */
    val incomingSocket = callbackFlow {
        val listener = object : ServerListener {
            override fun onListen(
                ns: SrtSocket,
                hsVersion: Int,
                peerAddress: InetSocketAddress,
                streamId: String
            ): Int {
                val channelResult = trySendBlocking(IncomingSocket(CoroutineSrtSocket(), streamId))
                return if (channelResult.isSuccess) {
                    0
                } else {
                    Log.e(TAG, "Rejected incoming socket")
                    -1
                }
            }
        }
        socket.serverListener = listener
        awaitClose { socket.serverListener = null }
    }

    /**
     * Check if the SRT socket is a valid SRT socket.
     *
     * @return true if the SRT socket is valid, otherwise false
     */
    val isValid: Boolean
        get() = socket.isValid

    /**
     * Gets the current status of the socket.
     *
     * **See Also:** [srt_getsockstate](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getsockstate)
     *
     * @return the current [SockStatus]
     */
    val sockState: SockStatus
        get() = socket.sockState

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
        get() = socket.peerName

    /**
     * Retrieves the remote [InetAddress] to which the socket is connected.
     *
     * **See Also:** [srt_getpeername](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getpeername)
     *
     * @return the remote [InetAddress] if SRT socket is connected and valid. Otherwise, it returns a null.
     * @see [peerName] and [port]
     * @throws [SocketException] if SRT socket is invalid or not connected
     */
    val inetAddress: InetAddress
        get() = socket.inetAddress

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
        get() = socket.port

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
        get() = socket.sockName

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
        get() = socket.localAddress

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
        get() = socket.localPort

    /**
     * Closes the socket or group and frees all used resources.
     *
     * **See Also:** [srt_close](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_close)
     *
     * @throws SocketException if close failed
     */
    fun close() {
        coroutineContext.cancelChildren()
        socket.close()
        socketContext.complete()
    }

    /**
     * Binds the socket to a local address.
     *
     * **See Also:** [srt_bind](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind)
     *
     * @param address the [InetSocketAddress] to bind to
     *
     * @throws BindException if bind has failed
     */
    suspend fun bind(address: InetSocketAddress) = withContext(coroutineContext) {
        socket.bind(address)
        hasBeenConnected = true
    }

    /**
     * Connects a socket to a specified address and port.
     *
     * **See Also:** [srt_connect](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect)
     *
     * @param address the [InetSocketAddress] to connect to
     * @throws ConnectException if connection has failed
     */
    suspend fun connect(address: InetSocketAddress) {
        execute(EpollOpt.OUT, onContinuation = { socket.connect(address) }) {
            null
        }
        hasBeenConnected = true
    }

    /**
     * Performs a rendezvous connection.
     *
     * **See Also:** [srt_rendezvous](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_rendezvous)
     *
     * @param localAddress the local [InetSocketAddress] to bind to
     * @param remoteAddress the remote [InetSocketAddress] to connect to
     * @throws SocketException if rendezvous connection has failed
     */
    suspend fun rendezVous(
        localAddress: InetSocketAddress,
        remoteAddress: InetSocketAddress
    ) {
        execute(EpollOpt.OUT, onContinuation = { socket.rendezVous(localAddress, remoteAddress) }) {
            null
        }
        hasBeenConnected = true
    }

    /**
     * Sets up the listening state on a socket.
     *
     * **See Also:** [srt_listen](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_listen)
     *
     * @param backlog the number of sockets that may be allowed to wait until they are accepted
     * @throws SocketException if listen failed
     * @see [ServerListener.onListen]
     */
    fun listen(backlog: Int) {
        socket.listen(backlog)
    }

    /**
     * Accepts a pending connection.
     *
     * **See Also:** [srt_accept](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_accept)
     *
     * @return a pair containing the new Socket connection and the IP address and port specification of the remote device.
     * @throws SocketException if returned SRT socket is not valid
     */
    suspend fun accept(): Pair<CoroutineSrtSocket, InetSocketAddress?> {
        return execute(EpollOpt.IN) {
            val pair = socket.accept()
            Pair(CoroutineSrtSocket(pair.first), pair.second)
        }
    }

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
    override fun getSockFlag(opt: SockOpt): Any {
        return socket.getSockFlag(opt)
    }

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
    override fun setSockFlag(opt: SockOpt, value: Any) {
        if ((opt == SockOpt.RCVSYN) || (opt == SockOpt.SNDSYN)) {
            throw IllegalArgumentException("Options not supported")
        }
        socket.setSockFlag(opt, value)
    }

    /**
     * Sends a message to a remote party.
     *
     * It waits till it is possible to write on the socket. When this method is returned, it does
     * not mean that the [msg] was sent.
     *
     * **See Also:** [srt_send](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_send)
     *
     * @param msg the [ByteBuffer] to send. It must be allocate with [ByteBuffer.allocateDirect]. It sends ByteBuffer from [ByteBuffer.position] to [ByteBuffer.limit].
     * @return the number of byte sent
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     * @see [recv]
     */
    suspend fun send(msg: ByteBuffer): Int {
        val timeoutInMs = (socket.getSockFlag(SockOpt.SNDTIMEO) as Int).toLong()

        return execute(EpollOpt.OUT, timeoutInMs) {
            socket.send(msg)
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * It waits till it is possible to write on the socket. When this method is returned, it does
     * not mean that the [msg] was sent.
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
    suspend fun send(msg: ByteBuffer, msgCtrl: MsgCtrl): Int {
        val timeoutInMs = (socket.getSockFlag(SockOpt.SNDTIMEO) as Int).toLong()

        return execute(EpollOpt.OUT, timeoutInMs) {
            socket.send(msg, msgCtrl)
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * It waits till it is possible to write on the socket. When this method is returned, it does
     * not mean that the [msg] was sent.
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
    suspend fun send(msg: ByteArray, offset: Int = 0, size: Int = msg.size): Int {
        val timeoutInMs = (socket.getSockFlag(SockOpt.SNDTIMEO) as Int).toLong()

        return execute(EpollOpt.OUT, timeoutInMs) {
            socket.send(msg, offset, size)
        }
    }

    /**
     * Sends a message to a remote party.
     *
     * It waits till it is possible to write on the socket. When this method is returned, it does
     * not mean that the [msg] was sent.
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
    suspend fun send(msg: ByteArray, offset: Int, size: Int, msgCtrl: MsgCtrl): Int {
        val timeoutInMs = (socket.getSockFlag(SockOpt.SNDTIMEO) as Int).toLong()

        return execute(EpollOpt.OUT, timeoutInMs) {
            socket.send(msg, offset, size, msgCtrl)
        }
    }

    /**
     * Received a message from a remote device
     *
     * It waits till it is possible to write on the socket.
     *
     * **See Also:** [srt_recv](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recv)
     *
     * @param size Size of the expected message.
     * @return the [ByteArray] that contains the received message.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     */
    suspend fun recv(size: Int): ByteArray {
        val timeoutInMs = (socket.getSockFlag(SockOpt.RCVTIMEO) as Int).toLong()

        return execute(EpollOpt.IN, timeoutInMs) {
            socket.recv(size)
        }
    }

    /**
     * Received a message from a remote device
     *
     * **See Also:** [srt_recv](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recv)
     *
     * @param buffer the [ByteArray] where received data are copied to.
     * @param offset the offset in the specified [buffer].
     * @param byteCount the size of the specified [buffer].
     * @return the number of bytes received.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     */
    suspend fun recv(
        buffer: ByteArray,
        offset: Int = 0,
        byteCount: Int = buffer.size
    ): Int {
        val timeoutInMs = (socket.getSockFlag(SockOpt.RCVTIMEO) as Int).toLong()

        return execute(EpollOpt.IN, timeoutInMs) {
            socket.recv(buffer, offset, byteCount)
        }
    }

    /**
     * Received a message from a remote device
     *
     * **See Also:** [srt_recvmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recvmsg2)
     *
     * @param size Size of the expected message.
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return the [ByteArray] that contains the received message.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     */
    suspend fun recv(size: Int, msgCtrl: MsgCtrl): ByteArray {
        val timeoutInMs = (socket.getSockFlag(SockOpt.RCVTIMEO) as Int).toLong()

        return execute(EpollOpt.IN, timeoutInMs) {
            socket.recv(size, msgCtrl)
        }
    }

    /**
     * Received a message from a remote device
     *
     * **See Also:** [srt_recvmsg2](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_recvmsg2)
     *
     * @param buffer the [ByteArray] where received data are copied to.
     * @param offset the offset in the specified [buffer].
     * @param byteCount the size of the specified [buffer].
     * @param msgCtrl the [MsgCtrl] that contains extra parameter
     * @return the number of bytes received.
     * @throws SocketException if it has failed to send message
     * @throws SocketTimeoutException if a timeout has been triggered
     */
    suspend fun recv(
        buffer: ByteArray,
        offset: Int = 0,
        byteCount: Int = buffer.size,
        msgCtrl: MsgCtrl
    ): Int {
        val timeoutInMs = (socket.getSockFlag(SockOpt.RCVTIMEO) as Int).toLong()

        return execute(EpollOpt.IN, timeoutInMs) {
            socket.recv(buffer, offset, byteCount, msgCtrl)
        }
    }

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
    suspend fun sendFile(path: String, offset: Long = 0, size: Long, block: Int = 364000): Long {
        var byteSent = 0L
        while (byteSent < size) {
            execute(EpollOpt.OUT) {
                byteSent += socket.sendFile(
                    path,
                    offset + byteSent,
                    min(size - byteSent, block.toLong()),
                    block
                )
            }
        }
        return byteSent
    }

    /**
     * Receives a file. File will be located at [path].
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
    suspend fun recvFile(path: String, offset: Long = 0, size: Long, block: Int = 364000): Long {
        var byteReceived = 0L
        while (byteReceived < size) {
            execute(EpollOpt.IN) {
                byteReceived += socket.recvFile(
                    path,
                    offset + byteReceived,
                    min(size - byteReceived, block.toLong()),
                    block
                )
            }
        }
        return byteReceived
    }

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
        get() = socket.rejectReason
        /**
         * Set detailed reason for a failed connection attempt. You can not set [InternalRejectReason].
         *
         * **See Also:** [srt_setrejectreason](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_setrejectreason)
         *
         * @param value the object describing the rejection reason. Could be either [InternalRejectReason], [PredefinedRejectReason] or [UserDefinedRejectReason]
         * @throws [SocketException] if action has failed
         */
        set(value) {
            socket.rejectReason = value
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
    fun bstats(clear: Boolean) = socket.bstats(clear)

    /**
     * Reports the current statistics.
     *
     * **See Also:** [srt_bistats](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bistats)
     *
     * @param clear true if the statistics should be cleared after retrieval
     * @param instantaneous true if the statistics should use instant data, not moving averages
     * @return the current [Stats]
     */
    fun bistats(clear: Boolean, instantaneous: Boolean) = socket.bistats(clear, instantaneous)

    // Time access
    /**
     * Gets the time when SRT socket was open to establish a connection.
     *
     * **See Also:** [srt_connection_time](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connection_time)
     *
     * @return the connection time in microseconds
     * @throws [SocketException] if SRT socket is not valid
     */
    val connectionTime: Long
        get() = socket.connectionTime

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
        get() = socket.receiveBufferSize
        /**
         * Sets the value of the [SockOpt.RCVBUF] option for this SRT socket.
         *
         * @param value receive buffer size in bytes
         * @throws IOException if can't set [SockOpt]
         */
        set(value) {
            socket.receiveBufferSize = value
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
        get() = socket.sendBufferSize
        /**
         * Sets the value of the [SockOpt.SNDBUF] option for this SRT socket.
         *
         * @param value send buffer size in bytes
         * @throws IOException if can't set [SockOpt]
         */
        set(value) {
            socket.sendBufferSize = value
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
        get() = socket.reuseAddress
        /**
         * Sets the value of the [SockOpt.REUSEADDR] option for this SRT socket.
         *
         * @param value true if it allows the SRT socket to use the binding address used already by another SRT socket in the same application, otherwise false
         * @throws IOException if can't set [SockOpt]
         */
        set(value) {
            socket.reuseAddress = value
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
        get() = socket.soLinger
        /**
         * Sets the value of the [SockOpt.LINGER] option for this SRT socket.
         *
         * @param value the linger time on close
         * @throws IOException if can't set [SockOpt]
         */
        set(value) {
            socket.soLinger = value
        }

    /**
     * Tests if the SRT socket is bound.
     *
     * @return true if the SRT socket is bound, otherwise false
     */
    val isBound: Boolean
        get() = socket.isBound

    /**
     * Tests if the SRT socket is closed.
     *
     * @return true if the SRT socket is closed, otherwise false
     */
    val isClose: Boolean
        get() = socket.isClose

    /**
     * Tests if the SRT socket is connected.
     *
     * @return true if the SRT socket is connected, otherwise false
     */
    val isConnected: Boolean
        get() = socket.isConnected

    /**
     * Get the size of the available data in the receive buffer.
     *
     * @return the size of the available data in the receive buffer
     * @throws IOException if can't get [SockOpt]
     */
    fun available(): Int = socket.available()

    override fun equals(other: Any?): Boolean {
        return other is CoroutineSrtSocket && socket == other.socket
    }

    override fun hashCode(): Int {
        return socket.hashCode()
    }

    private suspend fun <T> execute(
        epollOpt: EpollOpt,
        timeoutInMs: Long? = null,
        onContinuation: () -> Unit = {},
        block: () -> T
    ): T {
        val epoll = Epoll()
        //  flags = listOf(EpollFlag.ENABLE_EMPTY)
        epoll.addUSock(socket, listOf(EpollOpt.ERR, epollOpt))

        try {
            return withContext(coroutineContext) {
                if (timeoutInMs == null) {
                    executeEpoll(epoll, onContinuation, block)
                } else {
                    executeEpollWithTimeout(epoll, timeoutInMs, onContinuation, block)
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            epoll.clearUSock()
            epoll.release()
        }
    }

    private suspend fun <T> executeEpollWithTimeout(
        epoll: Epoll,
        timeoutInMs: Long,
        onContinuation: () -> Unit = {},
        block: () -> T
    ): T {
        return if (timeoutInMs >= 0) {
            withTimeout(timeoutInMs) {
                executeEpoll(epoll, onContinuation, block)
            }
        } else {
            executeEpoll(epoll, onContinuation, block)
        }
    }

    private suspend fun <T> executeEpoll(
        epoll: Epoll,
        onContinuation: () -> Unit = {},
        block: () -> T
    ): T {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                epoll.clearUSock()
            }
            onContinuation()
            while (isActive) {
                val epollEvents = try {
                    epoll.uWait(POLLING_TIMEOUT_IN_MS)
                } catch (e: Exception) {
                    if (SrtError.lastError != ErrorType.EPOLLEMPTY) {
                        continuation.resumeWithException(SocketException(SrtError.lastErrorMessage))
                    }
                    return@suspendCancellableCoroutine
                }
                if (epollEvents.isEmpty()) {
                    continue
                }
                val socketEvents = epollEvents.filter { it.socket == socket }
                if (socketEvents.isEmpty()) {
                    continue
                }
                epoll.addUSock(socket, null) // Unsubscribe from all events

                if (socketEvents.any { it.events.contains(EpollOpt.ERR) }) {
                    if (sockState == SockStatus.BROKEN) {
                        continuation.resumeWithException(SocketException("Socket is broken. Maybe due to timeout?"))
                    } else {
                        if (SrtError.lastError != ErrorType.SUCCESS) {
                            continuation.resumeWithException(SocketException(SrtError.lastErrorMessage))
                        } else {
                            continuation.resumeWithException(SocketException("Epoll returned an unknown error"))
                        }
                    }
                } else {
                    try {
                        if (socketEvents.any {
                                it.events.contains(EpollOpt.IN) || it.events.contains(
                                    EpollOpt.OUT
                                )
                            }) {
                            continuation.resumeWith(Result.success(block()))
                        }
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
                return@suspendCancellableCoroutine
            }
        }
    }

    companion object {
        private const val TAG = "CoroutineSocket"

        private const val POLLING_TIMEOUT_IN_MS = 1000L
    }

    /**
     * Listening socket data class.
     * Use to store the socket and the stream ID.
     */
    data class IncomingSocket(val socket: CoroutineSrtSocket, val streamId: String)
}