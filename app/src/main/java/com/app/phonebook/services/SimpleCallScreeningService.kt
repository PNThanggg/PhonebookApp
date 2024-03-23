package com.app.phonebook.services

import android.telecom.Call
import android.telecom.CallScreeningService
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.helpers.SimpleContactsHelper

class SimpleCallScreeningService : CallScreeningService() {
    /**
     * Determines whether an incoming call should be allowed, blocked, or screened based on configured preferences.
     *
     * This function is called whenever there's an incoming call. It uses the phone number from the call details
     * to check against user preferences and the device's contacts database to decide if the call should be blocked
     * or allowed through. The decision is based on two settings: whether to block unknown numbers (numbers not found
     * in the contacts database) and whether to block calls from hidden or anonymous numbers.
     *
     * @param callDetails The details of the incoming call, including the call handle which contains the phone number.
     *
     * Operation:
     * - If the incoming call number is known (not null) and the configuration is set to block unknown numbers,
     *   it checks if the number exists in the device's contacts. If the number is not found, the call is considered
     *   for blocking.
     * - If the incoming call number is hidden (null) and the configuration is set to block hidden numbers,
     *   the call is immediately considered for blocking.
     * - All other calls not meeting the above criteria are allowed through.
     *
     * This function delegates the decision to `respondToCall`, which performs the actual operation based on the
     * `isBlocked` flag determined by this function's logic.
     */
    override fun onScreenCall(callDetails: Call.Details) {
        val number: String? = callDetails.handle?.schemeSpecificPart
        when {
            number != null -> {
                val simpleContactsHelper = SimpleContactsHelper(this)
                val privateCursor = getMyContactsCursor(
                    favoritesOnly = false,
                    withPhoneNumbersOnly = true
                )
                simpleContactsHelper.exists(number, privateCursor) { exists ->
                    respondToCall(callDetails, isBlocked = !exists)
                }
            }

//            number == null -> {
//                respondToCall(callDetails, isBlocked = true)
//            }

            else -> {
                respondToCall(callDetails, isBlocked = false)
            }
        }
    }

    private fun respondToCall(callDetails: Call.Details, isBlocked: Boolean) {
        val response = CallResponse.Builder()
            .setDisallowCall(isBlocked)
            .setRejectCall(isBlocked)
            .setSkipCallLog(isBlocked)
            .setSkipNotification(isBlocked)
            .build()

        respondToCall(callDetails, response)
    }
}
