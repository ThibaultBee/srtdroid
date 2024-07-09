package io.github.thibaultbee.srtdroid.interfaces

import io.github.thibaultbee.srtdroid.enums.SockOpt

/**
 * A convenient interface to get and set socket options
 */
interface ConfigurableSrtSocket {
    fun getSockFlag(opt: SockOpt): Any
    fun setSockFlag(opt: SockOpt, value: Any)
}