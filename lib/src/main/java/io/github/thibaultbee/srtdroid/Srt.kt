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
package io.github.thibaultbee.srtdroid

/**
 * This class provides main SRT control. Before calling any other SRT API, you shall call [startUp].
 */
object Srt {
    init {
        System.loadLibrary("srtdroid")
    }

    /**
     * Uses to create an SRT context. It is automatically called by other class. You don't have to call it anymore.
     *
     * **See Also:** [srt_startup](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_startup)
     *
     * @return a value >= 0 on success, otherwise -1
     * @see [cleanUp]
     */
    external fun startUp(): Int

    /**
     * Cleans up all global SRT resources and shall be called just before exiting the application (for example, in your main activity onRelease()).
     *
     * **See Also:** [srt_cleanup](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_cleanup)
     *
     * @return a reserved for future use value
     * @see [startUp]
     */
    external fun cleanUp(): Int

    private external fun nativeGetVersion(): Int

    /**
     * Get SRT version
     *
     * **See Also:** [srt_getversion](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_getversion)
     *
     * @return hexadecimal version (0xXXYYZZ - x.y.z)
     */
    val version: Int
        get() = nativeGetVersion()

    /**
     * Set log level
     *
     * Log level value are the constant from <sys/syslog.h>:
     * - LOG_CRIT:	    2	critical conditions
     * - LOG_ERR:		3	error conditions
     * - LOG_WARNING:	4	warning conditions
     * - LOG_NOTICE:    5	normal but significant condition
     * - LOG_DEBUG:	    7	debug-level messages
     *
     * **See Also:** [srt_setloglevel](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_setloglevel)
     *
     * @param level log level
     */
    external fun setLogLevel(level: Int)
}