package com.github.thibaultbee.srtdroid.models

import com.github.thibaultbee.srtdroid.enums.EpollOpt

class EpollEvent(val socket: Socket, val events: List<EpollOpt>)