package com.github.thibaultbee.srtdroid.models.rejectreason

import com.github.thibaultbee.srtdroid.enums.RejectReasonCode

data class InternalRejectReason(val code: RejectReasonCode): RejectReason()