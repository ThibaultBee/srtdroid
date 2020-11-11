package com.github.thibaultbee.srtwrapper.models.rejectreason

import com.github.thibaultbee.srtwrapper.enums.RejectReasonCode

data class InternalRejectReason(val code: RejectReasonCode): RejectReason()