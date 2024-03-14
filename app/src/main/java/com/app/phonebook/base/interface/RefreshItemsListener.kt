package com.app.phonebook.base.`interface`

interface RefreshItemsListener {
    fun refreshItems(callback: (() -> Unit)? = null)
}
