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
package com.github.thibaultbee.srtdroid.enums

/**
 * The defined encryption state as performed by the Key Material Exchange, used by
 * [SockOpt.RCVKMSTATE], [SockOpt.SNDKMSTATE] and [SockOpt.KMSTATE] options
 *
 * **See Also:** [srt_km_state](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md#srt_km_state)
 */
enum class KMState {
    /**
     * No encryption
     */
    KM_S_UNSECURED,

    /**
     * Stream encrypted, exchanging Keying Material
     */
    KM_S_SECURING,

    /**
     * Stream encrypted, keying Material exchanged, decrypting ok.
     */
    KM_S_SECURED,

    /**
     * Stream encrypted and no secret to decrypt Keying Material
     */
    KM_S_NOSECRET,

    /**
     * Stream encrypted and wrong secret, cannot decrypt Keying Material
     */
    KM_S_BADSECRET
}