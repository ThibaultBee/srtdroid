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
package io.github.thibaultbee.srtdroid.enums

import io.github.thibaultbee.srtdroid.Srt

/**
 * SRT error codes.
 * Once it has been called, you must release Srt context with [Srt.cleanUp] when application leaves.
 *
 * **See Also:** [Error Codes](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#error-codes)
 */
enum class ErrorType {
    /**
     * Internal error when setting the right error code
     */
    EUNKNOWN,

    /**
     * SRT_SUCCESS 	The value set when the last error was cleared and no error has occurred since then
     */
    SUCCESS,

    /**
     * General setup error resulting from internal system state
     */
    ECONNSETUP,

    /**
     * Connection timed out while attempting to connect to the remote address
     */
    ENOSERVER,

    /**
     * Connection has been rejected
     */
    ECONNREJ,

    /**
     * An error occurred when trying to call a system function on an internally used UDP socket
     */
    ESOCKFAIL,

    /**
     * A possible tampering with the handshake packets was detected
     */
    ESECFAIL,

    /**
     * A socket that was vital for an operation called in blocking mode has been closed
     */
    ESCLOSED,

    /**
     * General connection failure of unknown details
     */

    ECONNFAIL,

    /**
     * The socket was properly connected, but the connection has been broken
     */
    ECONNLOST,

    /**
     * The socket is not connected
     */
    ENOCONN,

    /**
     * System or standard library error reported unexpectedly for unknown purpose
     */

    ERESOURCE,

    /**
     * System was unable to spawn a new thread when requried
     */
    ETHREAD,

    /**
     * System was unable to allocate memory for buffers
     */
    ENOBUF,

    /**
     * System was unable to allocate system specific objects
     */
    ESYSOBJ,

    /**
     * General filesystem error
     */

    EFILE,

    /**
     * Failure when trying to read from a given position in the file
     */
    EINVRDOFF,

    /**
     * Read permission was denied when trying to read from file
     */
    ERDPERM,

    /**
     * Failed to set position in the written file
     */
    EINVWROFF,

    /**
     * Write permission was denied when trying to write to a file
     */
    EWRPERM,

    /**
     * Invalid operation performed for the current state of a socket
     */
    EINVOP,

    /**
     * The socket is currently bound and the required operation cannot be performed in this state
     */
    EBOUNDSOCK,

    /**
     * The socket is currently connected and therefore performing the required operation is not possible
     */
    ECONNSOCK,

    /**
     * Call parameters for API functions have some requirements that were not satisfied
     */
    EINVPARAM,

    /**
     * The API function required an ID of an entity (socket or group) and it was invalid
     */
    EINVSOCK,

    /**
     * The operation to be performed on a socket requires that it first be explicitly bound
     */
    EUNBOUNDSOCK,

    /**
     * The socket passed for the operation is required to be in the listen state
     */
    ENOLISTEN,

    /**
     * The required operation cannot be performed when the socket is set to rendezvous mode
     */
    ERDVNOSERV,

    /**
     * An attempt was made to connect to a socket set to rendezvous mode that was not first bound
     */
    ERDVUNBOUND,

    /**
     * The function was used incorrectly in the message API
     */
    EINVALMSGAPI,

    /**
     * The function was used incorrectly in the stream (buffer) API
     */
    EINVALBUFFERAPI,

    /**
     * The port tried to be bound for listening is already bus
     */
    EDUPLISTEN,

    /**
     * Size exceeded
     */
    ELARGEMSG,

    /**
     * The epoll ID passed to an epoll function is invalid
     */
    EINVPOLLID,

    /**
     * The epoll container currently has no subscribed sockets
     */
    EPOLLEMPTY,

    /**
     * General asynchronous failure (not in use currently)
     */
    EASYNCFAIL,

    /**
     * Sending operation is not ready to perform
     */
    EASYNCSND,

    /**
     * Receiving operation is not ready to perform
     */
    EASYNCRCV,

    /**
     * The operation timed out
     */
    ETIMEOUT,

    /**
     * With [SockOpt.TSBPDMODE] and [SockOpt.TLPKTDROP] set to true, some packets were dropped by sender
     */
    ECONGEST,

    /**
     * Receiver peer is writing to a file that the agent is sending
     */
    EPEERERR;

    /**
     * Returns a string message that represents a given SRT error code
     *
     * **See Also:** [srt_strerror](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_strerror)
     *
     * @return the string message that represents a given SRT error code.
     */
    external override fun toString(): String

    companion object {
        init {
            Srt.startUp()
        }
    }
}