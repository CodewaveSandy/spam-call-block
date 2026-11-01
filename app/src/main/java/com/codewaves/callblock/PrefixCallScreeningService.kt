package com.codewaves.callblock

import android.telecom.Call
import android.telecom.CallScreeningService

class PrefixCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return
        val handle = callDetails.handle
        val block = handle?.scheme == "tel" && FixedPrefixRule.matches(handle.schemeSpecificPart)
        // Respond immediately on-device, before Android starts ringing.
        respondToCall(callDetails, CallResponse.Builder()
            .setDisallowCall(block)
            .setRejectCall(block)
            .setSkipNotification(block)
            .setSkipCallLog(false)
            .build())
    }
}
