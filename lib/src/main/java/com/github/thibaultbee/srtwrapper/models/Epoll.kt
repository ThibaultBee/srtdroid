package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.EpollFlag
import com.github.thibaultbee.srtwrapper.enums.EpollOpt

class Epoll {
    private var eid: Int

    private external fun create(): Int
    constructor() {
        eid = create()
    }

    external fun isValid(): Boolean

    private external fun addUSock(socket: Socket, events: Array<EpollOpt>): Int
    fun addUSock(socket: Socket, events: List<EpollOpt> = emptyList()) =
        addUSock(socket, events.toTypedArray())

    private external fun updateUSock(socket: Socket, events: Array<EpollOpt>): Int
    fun updateUSock(socket: Socket, events: List<EpollOpt> = emptyList()) =
        updateUSock(socket, events.toTypedArray())

    external fun removeUSock(socket: Socket): Int

    private external fun wait(
        readFds: Array<Socket>,
        writeFds: Array<Socket>,
        timeOut: Long
    ): Int
    fun wait(
        readFds: List<Socket> = emptyList(),
        writeFds: List<Socket> = emptyList(),
        timeOut: Long
    ) = wait(readFds.toTypedArray(), writeFds.toTypedArray(), timeOut)

    private external fun uWait(fdsSet: Array<EpollEvent>, timeOut: Long): Int
    fun uWait(fdsSet: List<EpollEvent>, timeOut: Long) =
        uWait(fdsSet.toTypedArray(), timeOut)

    private external fun set(events: Array<EpollFlag>): Array<EpollFlag>
    fun set(flags: List<EpollFlag>): List<EpollFlag> {
        return set(flags.toTypedArray()).toList()
    }

    private external fun getArray(): Array<EpollFlag>
    fun get(): List<EpollFlag> {
        return getArray().toList()
    }

    external fun release(): Int
}