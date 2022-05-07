package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.enums.SockOpt
import java.io.Closeable
import java.io.IOException

/**
 * Group configuration object
 *
 * **See Also:** [SRT_SOCKOPT_CONFIG](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_create_config)
 */
class SockOptGroupConfig : Closeable {
    private val ptr: Long

    init {
        ptr = nativeCreate()
    }

    private external fun nativeCreate(): Long

    private external fun nativeAdd(opt: SockOpt, value: Any): Int

    /**
     * Adds a configuration option to the configuration object.
     *
     * **See Also:** [srt_config_add](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_config_add)
     *
     * @param opt the [SockOpt] to set
     * @param value the [SockOpt] value to set. Type depends of the specified [opt].
     * @throws IOException if can't set [SockOpt]
     */
    fun add(opt: SockOpt, value: Any) {
        if (nativeAdd(opt, value) != 0) {
            throw IOException(Error.lastErrorMessage)
        }
    }

    /**
     * Deletes the configuration object.
     */
    private external fun nativeDelete()
    override fun close() {
        nativeDelete()
    }
}
