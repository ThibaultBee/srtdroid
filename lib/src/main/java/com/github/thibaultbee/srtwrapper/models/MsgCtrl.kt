package com.github.thibaultbee.srtwrapper.models

data class MsgCtrl(
    val flags: Int = 0,
    val ttl: Int = -1,
    val inOrder: Boolean = false,
    val boundary: Int,
    val srcTime: Long = 0,
    val pktSeq: Int,
    val no: Int
)