package com.app.phonebook.interfaces

import com.app.phonebook.data.models.Contact

interface RemoveFromGroupListener {
    fun removeFromGroup(contacts: ArrayList<Contact>)
}
