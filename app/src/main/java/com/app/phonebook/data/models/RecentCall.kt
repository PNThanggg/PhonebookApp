package com.app.phonebook.data.models

import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import com.app.phonebook.base.extension.normalizePhoneNumber
import com.app.phonebook.base.utils.isSPlus

@kotlinx.serialization.Serializable
data class RecentCall(
    val id: Int,
    val phoneNumber: String,
    val name: String,
    val photoUri: String,
    val startTS: Int,
    val duration: Int,
    val type: Int,
    val neighbourIDs: MutableList<Int>,
    val simID: Int,
    val specificNumber: String,
    val specificType: String,
    val isUnknownNumber: Boolean,
) {
    @Suppress("DEPRECATION")
    fun doesContainPhoneNumber(text: String, telephonyManager: TelephonyManager): Boolean {
        val normalizedText = text.normalizePhoneNumber()

        val isCompare = if (isSPlus()) {
            PhoneNumberUtils.areSamePhoneNumber(
                phoneNumber.normalizePhoneNumber(),
                normalizedText,
                telephonyManager.networkCountryIso
            )
        } else {
            PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText)
        }

        return isCompare || phoneNumber.contains(text) || phoneNumber.normalizePhoneNumber()
            .contains(normalizedText) || phoneNumber.contains(normalizedText)
    }
}
