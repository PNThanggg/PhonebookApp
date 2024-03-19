package com.app.phonebook.helpers

import android.content.Context
import android.net.Uri
import android.telecom.Call
import android.util.Log
import com.app.phonebook.R
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.base.extension.getPhoneNumberTypeText
import com.app.phonebook.base.extension.isConference
import com.app.phonebook.base.extension.telephonyManager
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.data.models.CallContact

object CallContactHelper {
    /**
     * Retrieves detailed contact information for a given call asynchronously.
     *
     * This function asynchronously fetches contact details for a specific call. If the call is identified as a
     * conference call, it immediately invokes the callback with a predefined `CallContact` object representing
     * a conference. Otherwise, it attempts to match the call to a contact in the device's address book or a list
     * of private contacts provided by `MyContactsContentProvider`.
     *
     * The function uses the call's handle to extract a phone number and searches the contacts database for a match.
     * If a match is found, the `CallContact` object is populated with the contact's name, photo URI, and, if
     * applicable, the label of the matched phone number. If no match is found, the `CallContact` object is populated
     * with the phone number as the name.
     *
     * @param context The context used for resource access and database queries.
     * @param call The call whose contact details are to be retrieved. Can be null, in which case the callback
     *        is invoked with an empty `CallContact` object.
     * @param callback A lambda function to be invoked with the resulting `CallContact` object once the contact
     *        details have been fetched or determined.
     */
    fun getCallContact(
        context: Context,
        call: Call?,
        callback: (CallContact) -> Unit
    ) {
        if (call.isConference()) {
            callback(
                CallContact(
                    context.getString(R.string.conference),
                    "",
                    "",
                    ""
                )
            )
            return
        }

        val privateCursor = context.getMyContactsCursor(
            favoritesOnly = false,
            withPhoneNumbersOnly = true
        )

        ensureBackgroundThread {
            val callContact = CallContact(
                "",
                "",
                "",
                ""
            )

            val handle = try {
                call?.details?.handle?.toString()
            } catch (e: NullPointerException) {
                Log.e(APP_NAME, "getCallContact: ${e.message}")
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
                            text = number,
                            telephonyManager = context.telephonyManager
                        )
                    }

                    if (contact != null) {
                        callContact.name = contact.getNameToDisplay()
                        callContact.photoUri = contact.photoUri

                        if (contact.phoneNumbers.size > 1) {
                            val specificPhoneNumber = contact.phoneNumbers.firstOrNull {
                                it.value == number
                            }

                            if (specificPhoneNumber != null) {
                                callContact.numberLabel = context.getPhoneNumberTypeText(
                                    specificPhoneNumber.type,
                                    specificPhoneNumber.label
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

