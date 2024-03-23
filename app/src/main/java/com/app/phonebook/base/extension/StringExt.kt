package com.app.phonebook.base.extension

import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import com.app.phonebook.base.utils.normalizeRegex
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

val ILLEGAL_CHARACTERS = charArrayOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')

fun String.isAValidFilename(): Boolean {
    ILLEGAL_CHARACTERS.forEach {
        if (contains(it)) return false
    }
    return true
}

fun String.highlightTextPart(
    textToHighlight: String, color: Int, highlightAll: Boolean = false, ignoreCharsBetweenDigits: Boolean = false
): SpannableString {
    val spannableString = SpannableString(this)
    if (textToHighlight.isEmpty()) {
        return spannableString
    }

    var startIndex = normalizeString().indexOf(textToHighlight, 0, true)
    val indexes = ArrayList<Int>()
    while (startIndex >= 0) {
        indexes.add(startIndex)

        startIndex = normalizeString().indexOf(textToHighlight, startIndex + textToHighlight.length, true)
        if (!highlightAll) {
            break
        }
    }

    // handle cases when we search for 643, but in reality the string contains it like 6-43
    if (ignoreCharsBetweenDigits && indexes.isEmpty()) {
        try {
            val regex = TextUtils.join("(\\D*)", textToHighlight.toCharArray().toTypedArray())
            val pattern = Pattern.compile(regex)
            val result = pattern.matcher(normalizeString())
            if (result.find()) {
                spannableString.setSpan(
                    ForegroundColorSpan(color), result.start(), result.end(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
        } catch (ignored: Exception) {
        }

        return spannableString
    }

    indexes.forEach {
        val endIndex = (it + textToHighlight.length).coerceAtMost(length)
        try {
            spannableString.setSpan(
                ForegroundColorSpan(color), it, endIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        } catch (ignored: IndexOutOfBoundsException) {
        }
    }

    return spannableString
}

fun String.normalizeString() = Normalizer.normalize(this, Normalizer.Form.NFD).replace(normalizeRegex, "")

// checks if string is a phone number
fun String.isPhoneNumber(): Boolean {
    return this.matches("^[0-9+\\-)( *#]+\$".toRegex())
}

// if we are comparing phone numbers, compare just the last 9 digits
fun String.trimToComparableNumber(): String {
    // don't trim if it's not a phone number
    if (!this.isPhoneNumber()) {
        return this
    }
    val normalizedNumber = this.normalizeString()
    val startIndex = 0.coerceAtLeast(normalizedNumber.length - 9)
    return normalizedNumber.substring(startIndex)
}

// get the contact names first letter at showing the placeholder without image
fun String.getNameLetter() = normalizeString().toCharArray().getOrNull(0)?.toString()?.uppercase(Locale.getDefault()) ?: "A"

fun String.normalizePhoneNumber(): String? = PhoneNumberUtils.normalizeNumber(this)

fun String.highlightTextFromNumbers(textToHighlight: String, primaryColor: Int): SpannableString {
    val spannableString = SpannableString(this)
    val digits = PhoneNumberUtils.convertKeypadLettersToDigits(this)
    if (digits.contains(textToHighlight)) {
        val startIndex = digits.indexOf(textToHighlight, 0, true)
        val endIndex = (startIndex + textToHighlight.length).coerceAtMost(length)
        try {
            spannableString.setSpan(
                ForegroundColorSpan(primaryColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        } catch (ignored: IndexOutOfBoundsException) {
        }
    }

    return spannableString
}