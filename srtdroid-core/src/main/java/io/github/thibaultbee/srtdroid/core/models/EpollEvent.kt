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
package io.github.thibaultbee.srtdroid.core.models

import io.github.thibaultbee.srtdroid.core.enums.EpollOpt

/**
 * This class represents SRT socket and their events.
 *
 * **See Also:** [srt_epoll_uwait](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_uwait)
 *
 * @param socket the SRT socket
 * @param events list of [EpollOpt] that report readiness of this socket
 */
class EpollEvent(val socket: SrtSocket, val events: List<EpollOpt>)