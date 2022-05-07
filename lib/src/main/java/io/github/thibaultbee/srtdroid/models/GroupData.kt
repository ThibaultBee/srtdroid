package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.enums.MemberStatus
import io.github.thibaultbee.srtdroid.enums.SockStatus
import java.net.InetSocketAddress

/**
 * The group member status
 *
 * **See Also:** [SRT_SOCKGROUPDATA](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_sockgroupdata)
 */
data class GroupData(
    /**
     * The member socket
     */
    val socket: Socket,
    /**
     * The address to which the [socket] should be connected
     */
    val peer: InetSocketAddress,
    /**
     * Current connection status
     *
     * @see SockStatus
     */
    val sockStatus: SockStatus,
    /**
     * The current weight value set on the link
     */
    val weight: Short,
    /**
     * The current state of the member
     *
     * @See MemberStatus
     */
    val memberStatus: MemberStatus,
    /**
     * The result of the operation (if this operation recently updated this structure)
     */
    val result: Int,
    /**
     * The token value set for that connection
     */
    val token: Int
)
