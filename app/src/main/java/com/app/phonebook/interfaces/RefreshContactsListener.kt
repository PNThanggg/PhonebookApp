package com.app.phonebook.interfaces

import com.app.phonebook.data.models.Contact

interface RefreshContactsListener {
    fun refreshContacts(refreshTabsMask: Int)

    fun contactClicked(contact: Contact)
}
