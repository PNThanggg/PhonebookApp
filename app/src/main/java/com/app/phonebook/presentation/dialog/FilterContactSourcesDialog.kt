package com.app.phonebook.presentation.dialog

import androidx.appcompat.app.AlertDialog
import com.app.phonebook.R
import com.app.phonebook.adapter.FilterContactSourcesAdapter
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.base.extension.getVisibleContactSources
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.ContactSource
import com.app.phonebook.databinding.DialogFilterContactSourcesBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.MyContactsContentProvider

class FilterContactSourcesDialog(val activity: BaseActivity<*>, private val callback: () -> Unit) {
    private val binding = DialogFilterContactSourcesBinding.inflate(activity.layoutInflater)

    private var dialog: AlertDialog? = null
    private var contactSources = ArrayList<ContactSource>()
    private var contacts = ArrayList<Contact>()
    private var isContactSourcesReady = false
    private var isContactsReady = false

    init {
        val contactHelper = ContactsHelper(activity)
        contactHelper.getContactSources { contactSources ->
            contactSources.mapTo(this@FilterContactSourcesDialog.contactSources) { it.copy() }
            isContactSourcesReady = true
            processDataIfReady()
        }

        contactHelper.getContacts(getAll = true, showOnlyContactsWithNumbers = true) {
            it.mapTo(contacts) { contact -> contact.copy() }
            val privateCursor = activity.getMyContactsCursor(
                favoritesOnly = false,
                withPhoneNumbersOnly = true
            )
            val privateContacts = MyContactsContentProvider.getContacts(cursor = privateCursor)
            this.contacts.addAll(privateContacts)
            isContactsReady = true
            processDataIfReady()
        }
    }

    private fun processDataIfReady() {
        if (!isContactSourcesReady) {
            return
        }

        val contactSourcesWithCount = ArrayList<ContactSource>()
        for (contactSource in contactSources) {
            val count = if (isContactsReady) {
                contacts.filter { it.source == contactSource.name }.count()
            } else {
                -1
            }
            contactSourcesWithCount.add(contactSource.copy(count = count))
        }

        contactSources.clear()
        contactSources.addAll(contactSourcesWithCount)

        activity.runOnUiThread {
            val selectedSources = activity.getVisibleContactSources()
            binding.filterContactSourcesList.adapter =
                FilterContactSourcesAdapter(activity, contactSourcesWithCount, selectedSources)

            if (dialog == null) {
                activity.getAlertDialogBuilder().setPositiveButton(R.string.ok) { dialogInterface, i -> confirmContactSources() }
                    .setNegativeButton(R.string.cancel, null).apply {
                        activity.setupDialogStuff(binding.root, this) { alertDialog ->
                            dialog = alertDialog
                        }
                    }
            }
        }
    }

    private fun confirmContactSources() {
        val selectedContactSources =
            (binding.filterContactSourcesList.adapter as FilterContactSourcesAdapter).getSelectedContactSources()
        val ignoredContactSources = contactSources.filter { !selectedContactSources.contains(it) }.map {
            if (it.type == SMT_PRIVATE) SMT_PRIVATE else it.getFullIdentifier()
        }.toHashSet()

        if (activity.getVisibleContactSources() != ignoredContactSources) {
            activity.config.ignoredContactSources = ignoredContactSources
            callback()
        }
        dialog?.dismiss()
    }
}
