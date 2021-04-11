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
package com.github.thibaultbee.srtdroid.interfaces

import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.models.Socket
import java.net.InetSocketAddress

/**
 * This interface is used by [Socket] to notify SRT socket events.
 */
interface SocketInterface {
    /**
     * Calls to handle the incoming connection on the listening socket (and is about to be returned by srt_accept), but before the connection has been accepted..
     *
     * **See Also:** [srt_listen_callback](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_listen_callback)
     *
     * @param ns the new SRT socket of the new connection
     * @param hsVersion the handshake version
     * @param peerAddress the address of the incomming connection
     * @param streamId the value set to [SockOpt.STREAMID] option set on the peer side
     * @return return 0, if the connection is to be accepted. If you return -1, this will be understood as a request to reject the incoming connection.
     */
    fun onListen(ns: Socket, hsVersion: Int, peerAddress: InetSocketAddress, streamId: String): Int
}