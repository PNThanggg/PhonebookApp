package com.app.phonebook.helpers

import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import com.app.phonebook.R
import com.app.phonebook.base.extension.getByteArray
import com.app.phonebook.base.extension.getDateTimeFromDateString
import com.app.phonebook.base.extension.showErrorToast
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.helpers.ExportResult
import com.app.phonebook.base.utils.CELL
import com.app.phonebook.base.utils.HANGOUTS
import com.app.phonebook.base.utils.HOME
import com.app.phonebook.base.utils.HOME_FAX
import com.app.phonebook.base.utils.JABBER
import com.app.phonebook.base.utils.MOBILE
import com.app.phonebook.base.utils.OTHER
import com.app.phonebook.base.utils.PAGER
import com.app.phonebook.base.utils.PREF
import com.app.phonebook.base.utils.QQ
import com.app.phonebook.base.utils.WORK
import com.app.phonebook.base.utils.WORK_FAX
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.VCardVersion
import ezvcard.parameter.ImageType
import ezvcard.property.Address
import ezvcard.property.Anniversary
import ezvcard.property.Birthday
import ezvcard.property.Categories
import ezvcard.property.Email
import ezvcard.property.FormattedName
import ezvcard.property.Impp
import ezvcard.property.Organization
import ezvcard.property.Photo
import ezvcard.property.StructuredName
import ezvcard.property.Telephone
import ezvcard.property.Title
import java.io.OutputStream
import java.util.Calendar


class VcfExporter {

    enum class ExportResult {
        EXPORT_FAIL, EXPORT_OK, EXPORT_PARTIAL
    }

    private var contactsExported = 0
    private var contactsFailed = 0

    fun exportContacts(
        activity: BaseActivity<*>,
        outputStream: OutputStream?,
        contacts: ArrayList<Contact>,
        showExportingToast: Boolean,
        version: VCardVersion = VCardVersion.V4_0,
        callback: (result: ExportResult) -> Unit
    ) {
        try {
            if (outputStream == null) {
                callback(ExportResult.EXPORT_FAIL)
                return
            }

            if (showExportingToast) {
                activity.toast(R.string.exporting)
            }

            val cards = ArrayList<VCard>()
            for (contact in contacts) {
                val card = VCard()

                val formattedName =
                    arrayOf(contact.prefix, contact.firstName, contact.middleName, contact.surname, contact.suffix)
                        .filter { it.isNotEmpty() }
                        .joinToString(separator = " ")
                card.formattedName = FormattedName(formattedName)

                StructuredName().apply {
                    prefixes.add(contact.prefix)
                    given = contact.firstName
                    additionalNames.add(contact.middleName)
                    family = contact.surname
                    suffixes.add(contact.suffix)
                    card.structuredName = this
                }

                if (contact.nickname.isNotEmpty()) {
                    card.setNickname(contact.nickname)
                }

                contact.phoneNumbers.forEach {
                    val phoneNumber = Telephone(it.value)
                    phoneNumber.parameters.addType(getPhoneNumberTypeLabel(it.type, it.label))
                    if (it.isPrimary) {
                        phoneNumber.parameters.addType(getPreferredType(1))
                    }
                    card.addTelephoneNumber(phoneNumber)
                }

                contact.emails.forEach {
                    val email = Email(it.value)
                    email.parameters.addType(getEmailTypeLabel(it.type, it.label))
                    card.addEmail(email)
                }

                contact.events.forEach { event ->
                    if (event.type == ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY || event.type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) {
                        val dateTime = event.value.getDateTimeFromDateString(false)
                        Calendar.getInstance().apply {
                            clear()
                            if (event.value.startsWith("--")) {
                                set(Calendar.YEAR, 1900)
                            } else {
                                set(Calendar.YEAR, dateTime.year)

                            }
                            set(Calendar.MONTH, dateTime.monthOfYear - 1)
                            set(Calendar.DAY_OF_MONTH, dateTime.dayOfMonth)
                            if (event.type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) {
                                card.birthdays.add(Birthday(time))
                            } else {
                                card.anniversaries.add(Anniversary(time))
                            }
                        }
                    }
                }

                contact.addresses.forEach {
                    val address = Address()
                    address.streetAddress = it.value
                    address.parameters.addType(getAddressTypeLabel(it.type, it.label))
                    card.addAddress(address)
                }

                contact.listIM.forEach {
                    val impp = when (it.type) {
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM -> Impp.aim(it.value)
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO -> Impp.yahoo(it.value)
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN -> Impp.msn(it.value)
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ -> Impp.icq(it.value)
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE -> Impp.skype(it.value)
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK -> Impp(HANGOUTS, it.value)
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ -> Impp(QQ, it.value)
                        ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER -> Impp(JABBER, it.value)
                        else -> Impp(it.label, it.value)
                    }

                    card.addImpp(impp)
                }

                if (contact.notes.isNotEmpty()) {
                    card.addNote(contact.notes)
                }

                if (contact.organization.isNotEmpty()) {
                    val organization = Organization()
                    organization.values.add(contact.organization.company)
                    card.organization = organization
                    card.titles.add(Title(contact.organization.jobPosition))
                }

                contact.websites.forEach {
                    card.addUrl(it)
                }

                if (contact.thumbnailUri.isNotEmpty()) {
                    val photoByteArray =
                        MediaStore.Images.Media.getBitmap(activity.contentResolver, Uri.parse(contact.thumbnailUri))
                            .getByteArray()
                    val photo = Photo(photoByteArray, ImageType.JPEG)
                    card.addPhoto(photo)
                }

                if (contact.groups.isNotEmpty()) {
                    val groupList = Categories()
                    contact.groups.forEach {
                        groupList.values.add(it.title)
                    }

                    card.categories = groupList
                }

                cards.add(card)
                contactsExported++
            }

            Ezvcard.write(cards).version(version).go(outputStream)
        } catch (e: Exception) {
            activity.showErrorToast(e)
        }

        callback(
            when {
                contactsExported == 0 -> ExportResult.EXPORT_FAIL
                contactsFailed > 0 -> ExportResult.EXPORT_PARTIAL
                else -> ExportResult.EXPORT_OK
            }
        )
    }

    private fun getPhoneNumberTypeLabel(type: Int, label: String) = when (type) {
        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> CELL
        ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> HOME
        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> WORK
        ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> PREF
        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK -> WORK_FAX
        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME -> HOME_FAX
        ContactsContract.CommonDataKinds.Phone.TYPE_PAGER -> PAGER
        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> OTHER
        else -> label
    }

    private fun getEmailTypeLabel(type: Int, label: String) = when (type) {
        ContactsContract.CommonDataKinds.Email.TYPE_HOME -> HOME
        ContactsContract.CommonDataKinds.Email.TYPE_WORK -> WORK
        ContactsContract.CommonDataKinds.Email.TYPE_MOBILE -> MOBILE
        ContactsContract.CommonDataKinds.Email.TYPE_OTHER -> OTHER
        else -> label
    }

    private fun getAddressTypeLabel(type: Int, label: String) = when (type) {
        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> HOME
        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> WORK
        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER -> OTHER
        else -> label
    }

    private fun getPreferredType(value: Int) = "$PREF=$value"
}
