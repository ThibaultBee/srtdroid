package io.github.thibaultbee.srtdroid.core.interfaces

import io.github.thibaultbee.srtdroid.core.enums.SockOpt

/**
 * A convenient interface to get and set socket options
 */
interface ConfigurableSrtSocket {
    fun getSockFlag(opt: SockOpt): Any
    fun setSockFlag(opt: SockOpt, value: Any)
}