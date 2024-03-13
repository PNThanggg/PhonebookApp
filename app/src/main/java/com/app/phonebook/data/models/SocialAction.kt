package com.app.phonebook.data.models

data class SocialAction(
    var actionId: Int,
    var type: Int,
    var label: String,
    var mimetype: String,
    val dataId: Long,
    val packageName: String
)
