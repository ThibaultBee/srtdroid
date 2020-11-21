package com.github.thibaultbee.srtdroid.models

import com.github.thibaultbee.srtdroid.enums.EpollFlag
import com.github.thibaultbee.srtdroid.enums.EpollOpt

class Epoll {
    private var eid: Int

    private external fun create(): Int
    constructor() {
        eid = create()
    }

    external fun isValid(): Boolean

    external fun addUSock(socket: Socket, events: List<EpollOpt> = emptyList()): Int

    external fun updateUSock(socket: Socket, events: List<EpollOpt> = emptyList()): Int

    external fun removeUSock(socket: Socket): Int

    external fun wait(
        readFds: List<Socket> = emptyList(),
        writeFds: List<Socket> = emptyList(),
        timeOut: Long
    ): Int

    external fun uWait(fdsSet: List<EpollEvent>, timeOut: Long): Int

    external fun set(events: List<EpollFlag>): List<EpollFlag>

    external fun get(): List<EpollFlag>

    external fun release(): Int
}