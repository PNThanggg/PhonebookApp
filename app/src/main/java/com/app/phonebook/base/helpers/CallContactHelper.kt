package com.app.phonebook.base.helpers

import android.content.Context
import android.net.Uri
import android.telecom.Call
import com.app.phonebook.R
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.base.extension.getPhoneNumberTypeText
import com.app.phonebook.base.extension.isConference
import com.app.phonebook.base.extension.telephonyManager
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.data.models.CallContact
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.MyContactsContentProvider

object CallContactHelper {
    fun getCallContact(context: Context, call: Call?, callback: (CallContact) -> Unit) {
        if (call.isConference()) {
            callback(CallContact(context.getString(R.string.conference), "", "", ""))
            return
        }

        val privateCursor = context.getMyContactsCursor(false, true)
        ensureBackgroundThread {
            val callContact = CallContact("", "", "", "")
            val handle = try {
                call?.details?.handle?.toString()
            } catch (e: NullPointerException) {
                null
            }

            if (handle == null) {
                callback(callContact)
                return@ensureBackgroundThread
            }

            val uri = Uri.decode(handle)
            if (uri.startsWith("tel:")) {
                val number = uri.substringAfter("tel:")
                ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
                    val privateContacts = MyContactsContentProvider.getContacts(privateCursor)
                    if (privateContacts.isNotEmpty()) {
                        contacts.addAll(privateContacts)
                    }

                    val contactsWithMultipleNumbers = contacts.filter { it.phoneNumbers.size > 1 }
                    val numbersToContactIDMap = HashMap<String, Int>()
                    contactsWithMultipleNumbers.forEach { contact ->
                        contact.phoneNumbers.forEach { phoneNumber ->
                            numbersToContactIDMap[phoneNumber.value] = contact.contactId
                            numbersToContactIDMap[phoneNumber.normalizedNumber] = contact.contactId
                        }
                    }

                    callContact.number = number
                    val contact = contacts.firstOrNull {
                        it.doesHavePhoneNumber(
                            number, context.telephonyManager
                        )
                    }
                    if (contact != null) {
                        callContact.name = contact.getNameToDisplay()
                        callContact.photoUri = contact.photoUri

                        if (contact.phoneNumbers.size > 1) {
                            val specificPhoneNumber =
                                contact.phoneNumbers.firstOrNull { it.value == number }
                            if (specificPhoneNumber != null) {
                                callContact.numberLabel = context.getPhoneNumberTypeText(
                                    specificPhoneNumber.type, specificPhoneNumber.label
                                )
                            }
                        }
                    } else {
                        callContact.name = number
                    }
                    callback(callContact)
                }
            }
        }
    }
}

