package com.app.phonebook.helpers

import android.content.Context
import com.app.phonebook.base.extension.telephonyManager
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.RecentCall


// hide private contacts from recent calls
fun List<RecentCall>.hidePrivateContacts(
    privateContacts: List<Contact>, shouldHide: Boolean
): List<RecentCall> {
    return if (shouldHide) {
        filterNot { recent ->
            val privateNumbers = privateContacts.flatMap { it.phoneNumbers }.map { it.value }
            recent.phoneNumber in privateNumbers
        }
    } else {
        this
    }
}

fun List<RecentCall>.setNamesIfEmpty(
    context: Context, contacts: List<Contact>, privateContacts: List<Contact>
): ArrayList<RecentCall> {
    val contactsWithNumbers = contacts.filter { it.phoneNumbers.isNotEmpty() }
    return map { recent ->
        if (recent.phoneNumber == recent.name) {
            val privateContact = privateContacts.firstOrNull {
                it.doesContainPhoneNumber(
                    recent.phoneNumber, telephonyManager = context.telephonyManager
                )
            }
            val contact =
                contactsWithNumbers.firstOrNull { it.phoneNumbers.first().normalizedNumber == recent.phoneNumber }

            when {
                privateContact != null -> recent.copy(name = privateContact.getNameToDisplay())
                contact != null -> recent.copy(name = contact.getNameToDisplay())
                else -> recent
            }
        } else {
            recent
        }
    } as ArrayList
}
