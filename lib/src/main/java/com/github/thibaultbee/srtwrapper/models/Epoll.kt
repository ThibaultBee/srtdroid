package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.EpollFlag
import com.github.thibaultbee.srtwrapper.enums.EpollOpt

class Epoll {
    private external fun nativeEpollCreate(): Int
    private external fun nativeEpollAddUSock(socket: Socket, events: Array<EpollOpt>?): Int
    private external fun nativeEpollUpdateUSock(socket: Socket, events: Array<EpollOpt>?): Int
    private external fun nativeEpollRemoveUSock(socket: Socket): Int
    private external fun nativeEpollWait(
        readFds: Array<Socket>?,
        writeFds: Array<Socket>?,
        timeOut: Long
    ): Int

    private external fun nativeEpollUWait(fdsSet: Array<EpollEvent>, timeOut: Long): Int
    private external fun nativeEpollSet(events: Array<EpollFlag>): Array<EpollFlag>
    private external fun nativeEpollGet(): Array<EpollFlag>
    private external fun nativeEpollRelease(): Int

    private var eid: Int

    companion object {
        const val INVALID_EPOLL = -1
    }

    constructor() {
        eid = nativeEpollCreate()
    }

    fun isValid() = eid > INVALID_EPOLL

    fun addUSock(socket: Socket, events: List<EpollOpt>?) =
        nativeEpollAddUSock(socket, events?.toTypedArray())

    fun updateUSock(socket: Socket, events: List<EpollOpt>?) =
        nativeEpollUpdateUSock(socket, events?.toTypedArray())

    fun removeUSock(socket: Socket) = nativeEpollRemoveUSock(socket)

    fun wait(readFds: List<Socket>? = null, writeFds: List<Socket>? = null, timeOut: Long) =
        nativeEpollWait(readFds?.toTypedArray(), writeFds?.toTypedArray(), timeOut)

    fun uWait(fdsSet: List<EpollEvent>, timeOut: Long) =
        nativeEpollUWait(fdsSet.toTypedArray(), timeOut)

    fun set(flags: List<EpollFlag>): List<EpollFlag> {
        return nativeEpollSet(flags.toTypedArray()).toList()
    }

    fun get(): List<EpollFlag> {
        return nativeEpollGet().toList()
    }

    fun release() = nativeEpollRelease()
}