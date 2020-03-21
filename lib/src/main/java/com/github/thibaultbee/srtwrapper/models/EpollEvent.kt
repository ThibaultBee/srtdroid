package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.EpollOpt

class EpollEvent(val socket: Socket, val events: List<EpollOpt>)