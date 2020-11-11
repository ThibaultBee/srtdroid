package com.github.thibaultbee.srtwrapper.enums

enum class RejectReasonCode {
    UNKNOWN,     // initial set when in progress
    SYSTEM,      // broken due to system function error
    PEER,        // connection was rejected by peer
    RESOURCE,    // internal problem with resource allocation
    ROGUE,       // incorrect data in handshake messages
    BACKLOG,     // listener's backlog exceeded
    IPE,         // internal program error
    CLOSE,       // socket is closing
    VERSION,     // peer is older version than agent's minimum set
    RDVCOOKIE,   // rendezvous cookie collision
    BADSECRET,   // wrong password
    UNSECURE,    // password required or unexpected
    MESSAGEAPI,  // streamapi/messageapi collision
    CONGESTION,  // incompatible congestion-controller type
    FILTER,      // incompatible packet filter
    GROUP,       // incompatible group
    TIMEOUT;     // connection timeout

    external override fun toString(): String

    companion object {
        val PREDEFINED_OFFSET = 1000 // SRT_REJC_PREDEFINED - Standard server error codes
        val USERDEFINED_OFFSET = 2000 //  SRT_REJC_USERDEFINED - User defined error codes
    }
}