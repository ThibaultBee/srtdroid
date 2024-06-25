package io.github.thibaultbee.srtdroid.ktx.extensions

import io.github.thibaultbee.srtdroid.enums.SockStatus
import io.github.thibaultbee.srtdroid.ktx.CoroutineSocket
import java.io.File
import java.net.BindException
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException


/**
 * Tests if the SRT socket is connected.
 *
 * @return true if the SRT socket is connected, otherwise false
 */
val CoroutineSocket.isConnected: Boolean
    get() = sockState == SockStatus.CONNECTED


/**
 * Binds the socket to a local address.
 *
 * **See Also:** [srt_bind](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_bind)
 *
 * @param address the address to bind to
 * @param port the port to bind to
 * @throws BindException if bind has failed
 */
suspend fun CoroutineSocket.bind(address: String, port: Int) =
    bind(InetSocketAddress(address, port))

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
suspend fun CoroutineSocket.bind(address: InetAddress, port: Int) =
    bind(InetSocketAddress(address, port))

/**
 * Connects a socket to a specified address and port.
 *
 * **See Also:** [srt_connect](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect)
 *
 * @param address the address to connect to
 * @param port the port to connect to
 * @throws ConnectException if connection has failed
 */
suspend fun CoroutineSocket.connect(address: String, port: Int) =
    connect(InetSocketAddress(address, port))

/**
 * Connects a socket to a specified address and port.
 *
 * **See Also:** [srt_connect](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect)
 *
 * @param address the [InetAddress] to connect to
 * @param port the port to connect to
 * @throws ConnectException if connection has failed
 */
suspend fun CoroutineSocket.connect(address: InetAddress, port: Int) =
    connect(InetSocketAddress(address, port))


/**
 * Performs a rendezvous connection.
 *
 * **See Also:** [srt_rendezvous](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_rendezvous)
 *
 * @param localAddress the local address to bind to
 * @param remoteAddress the remote address to connect to
 * @throws SocketException if rendezvous connection has failed
 */
suspend fun CoroutineSocket.rendezVous(localAddress: String, remoteAddress: String, port: Int) =
    rendezVous(
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
suspend fun CoroutineSocket.rendezVous(
    localAddress: InetAddress,
    remoteAddress: InetAddress,
    port: Int
) = rendezVous(
    InetSocketAddress(localAddress, port),
    InetSocketAddress(remoteAddress, port)
)

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
suspend fun CoroutineSocket.send(msg: String) = send(msg.toByteArray())

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
suspend fun CoroutineSocket.sendFile(
    file: File,
    offset: Long = 0,
    size: Long = file.length(),
    block: Int = 364000
) = sendFile(file.path, offset, size, block)

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
suspend fun CoroutineSocket.recvFile(
    file: File,
    offset: Long = 0,
    size: Long,
    block: Int = 7280000
) = recvFile(file.path, offset, size, block)