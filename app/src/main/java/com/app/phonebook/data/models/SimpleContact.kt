package com.app.phonebook.data.models

import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import com.app.phonebook.base.extension.normalizePhoneNumber
import com.app.phonebook.base.extension.normalizeString
import com.app.phonebook.base.utils.SORT_BY_FULL_NAME
import com.app.phonebook.base.utils.SORT_DESCENDING
import com.app.phonebook.base.utils.isSPlus

data class SimpleContact(
    val rawId: Int,
    val contactId: Int,
    var name: String? = "",
    var photoUri: String? = "",
    var phoneNumbers: ArrayList<PhoneNumber>,
    var birthdays: ArrayList<String>,
    var anniversaries: ArrayList<String>
) : Comparable<SimpleContact> {

    companion object {
        var sorting = -1
    }

    override fun compareTo(other: SimpleContact): Int {
        if (sorting == -1) {
            return compareByFullName(other)
        }

        var result = when {
            sorting and SORT_BY_FULL_NAME != 0 -> compareByFullName(other)
            else -> rawId.compareTo(other.rawId)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }

    private fun compareByFullName(other: SimpleContact): Int {
        val firstString = name?.normalizeString()
        val secondString = other.name?.normalizeString()

        if (firstString == null) {
            return -1
        }

        if (secondString == null) {
            return -1
        }

        return if (firstString.firstOrNull()?.isLetter() == true && secondString.firstOrNull()
                ?.isLetter() == false
        ) {
            -1
        } else if (firstString.firstOrNull()?.isLetter() == false && secondString.firstOrNull()
                ?.isLetter() == true
        ) {
            1
        } else {
            if (firstString.isEmpty() && secondString.isNotEmpty()) {
                1
            } else if (firstString.isNotEmpty() && secondString.isEmpty()) {
                -1
            } else {
                firstString.compareTo(secondString, true)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun doesContainPhoneNumber(text: String, telephonyManager: TelephonyManager): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    phoneNumber.contains(text)
                }
            } else {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    val isCompare = if (isSPlus()) {
                        PhoneNumberUtils.areSamePhoneNumber(
                            phoneNumber.normalizePhoneNumber(),
                            normalizedText,
                            telephonyManager.networkCountryIso
                        )
                    } else {
                        PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText)
                    }

                    isCompare || phoneNumber.contains(text) || phoneNumber.normalizePhoneNumber()
                        .contains(normalizedText) || phoneNumber.contains(normalizedText)
                }
            }
        } else {
            false
        }
    }

    @Suppress("DEPRECATION")
    fun doesHavePhoneNumber(text: String, telephonyManager: TelephonyManager): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    phoneNumber == text
                }
            } else {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    val isCompare = if (isSPlus()) {
                        PhoneNumberUtils.areSamePhoneNumber(
                            phoneNumber.normalizePhoneNumber(),
                            normalizedText,
                            telephonyManager.networkCountryIso
                        )
                    } else {
                        PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText)
                    }

                    isCompare || phoneNumber == text || phoneNumber.normalizePhoneNumber() == normalizedText || phoneNumber == normalizedText
                }
            }
        } else {
            false
        }
    }
}
