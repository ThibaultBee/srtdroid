package io.github.thibaultbee.srtdroid.enums

/**
 * SRT group types.
 *
 * **See Also:** [Group types](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_group_type)
 */
enum class GroupType {
    /**
     * Broadcast type, all links are actively used at once
     */
    BROADCAST,

    /**
     * Backup type, idle links take over connection on disturbance
     */
    BACKUP,

    /**
     * Balancing type, share bandwidth usage between links
     */
    //BALANCING
}