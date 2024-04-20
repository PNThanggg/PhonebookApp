package com.app.phonebook.presentation.dialog

import android.app.Dialog
import com.app.phonebook.R
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.utils.SHOW_ADDRESSES_FIELD
import com.app.phonebook.base.utils.SHOW_CONTACT_SOURCE_FIELD
import com.app.phonebook.base.utils.SHOW_EMAILS_FIELD
import com.app.phonebook.base.utils.SHOW_EVENTS_FIELD
import com.app.phonebook.base.utils.SHOW_FIRST_NAME_FIELD
import com.app.phonebook.base.utils.SHOW_GROUPS_FIELD
import com.app.phonebook.base.utils.SHOW_IMS_FIELD
import com.app.phonebook.base.utils.SHOW_MIDDLE_NAME_FIELD
import com.app.phonebook.base.utils.SHOW_NICKNAME_FIELD
import com.app.phonebook.base.utils.SHOW_NOTES_FIELD
import com.app.phonebook.base.utils.SHOW_ORGANIZATION_FIELD
import com.app.phonebook.base.utils.SHOW_PHONE_NUMBERS_FIELD
import com.app.phonebook.base.utils.SHOW_PREFIX_FIELD
import com.app.phonebook.base.utils.SHOW_RINGTONE_FIELD
import com.app.phonebook.base.utils.SHOW_SUFFIX_FIELD
import com.app.phonebook.base.utils.SHOW_SURNAME_FIELD
import com.app.phonebook.base.utils.SHOW_WEBSITES_FIELD
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.presentation.view.MyAppCompatCheckbox

class ManageVisibleFieldsDialog(
    val activity: BaseActivity<*>,
    val callback: (hasSomethingChanged: Boolean) -> Unit
) : Dialog(
    activity
) {
    private var view = activity.layoutInflater.inflate(R.layout.dialog_manage_visible_fields, null)
    private val fields = LinkedHashMap<Int, Int>()

    init {
        fields.apply {
            put(SHOW_PREFIX_FIELD, R.id.manage_visible_fields_prefix)
            put(SHOW_FIRST_NAME_FIELD, R.id.manage_visible_fields_first_name)
            put(SHOW_MIDDLE_NAME_FIELD, R.id.manage_visible_fields_middle_name)
            put(SHOW_SURNAME_FIELD, R.id.manage_visible_fields_surname)
            put(SHOW_SUFFIX_FIELD, R.id.manage_visible_fields_suffix)
            put(SHOW_NICKNAME_FIELD, R.id.manage_visible_fields_nickname)
            put(SHOW_PHONE_NUMBERS_FIELD, R.id.manage_visible_fields_phone_numbers)
            put(SHOW_EMAILS_FIELD, R.id.manage_visible_fields_emails)
            put(SHOW_ADDRESSES_FIELD, R.id.manage_visible_fields_addresses)
            put(SHOW_IMS_FIELD, R.id.manage_visible_fields_ims)
            put(SHOW_EVENTS_FIELD, R.id.manage_visible_fields_events)
            put(SHOW_NOTES_FIELD, R.id.manage_visible_fields_notes)
            put(SHOW_ORGANIZATION_FIELD, R.id.manage_visible_fields_organization)
            put(SHOW_WEBSITES_FIELD, R.id.manage_visible_fields_websites)
            put(SHOW_GROUPS_FIELD, R.id.manage_visible_fields_groups)
            put(SHOW_CONTACT_SOURCE_FIELD, R.id.manage_visible_fields_contact_source)
            put(SHOW_RINGTONE_FIELD, R.id.manage_ringtone)
        }

        val showContactFields = activity.config.showContactFields
        for ((key, value) in fields) {
            view.findViewById<MyAppCompatCheckbox>(value).isChecked = showContactFields and key != 0
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this)
            }
    }

    private fun dialogConfirmed() {
        var result = 0
        for ((key, value) in fields) {
            if (view.findViewById<MyAppCompatCheckbox>(value).isChecked) {
                result += key
            }
        }

        val hasSomethingChanged = activity.config.showContactFields != result
        activity.config.showContactFields = result

        if (hasSomethingChanged) {
            callback(true)
        }
    }
}