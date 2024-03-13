package com.app.phonebook.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Event(var value: String, var type: Int)
