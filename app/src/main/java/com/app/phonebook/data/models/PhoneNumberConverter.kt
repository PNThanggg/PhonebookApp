package com.app.phonebook.data.models

import androidx.annotation.Keep

@Keep
data class PhoneNumberConverter(
    var a: String, var b: Int, var c: String, var d: String, var e: Boolean = false
)
