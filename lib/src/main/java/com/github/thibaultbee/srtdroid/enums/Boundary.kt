package com.github.thibaultbee.srtdroid.enums

import com.github.thibaultbee.srtdroid.models.MsgCtrl

/**
 * To be use as [MsgCtrl.boundary]
 */
enum class Boundary {
    /**
     * Middle packet of a message
     */
    SUBSEQUENT,

    /**
     * Last packet of a message
     */
    LAST,

    /**
     * First packet of a message
     */
    FIRST,

    /**
     * Solo message packet
     */
    SOLO
}