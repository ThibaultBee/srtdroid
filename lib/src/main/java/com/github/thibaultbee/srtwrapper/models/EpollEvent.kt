package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.EpollOpt

class EpollEvent(val socket: Socket, events: List<EpollOpt>) {
    val events = events.toTypedArray()
}