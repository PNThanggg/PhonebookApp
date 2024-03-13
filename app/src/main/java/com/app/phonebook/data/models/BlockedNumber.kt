package com.app.phonebook.data.models
import kotlinx.serialization.Serializable

@Serializable
data class BlockedNumber(val id: Long, val number: String, val normalizedNumber: String, val numberToCompare: String, val contactName: String? = null)
