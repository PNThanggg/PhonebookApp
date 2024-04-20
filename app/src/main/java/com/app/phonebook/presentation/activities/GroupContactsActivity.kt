package com.app.phonebook.presentation.activities;

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.app.phonebook.R
import com.app.phonebook.adapter.ContactsAdapter
import com.app.phonebook.base.extension.addContactsToGroup
import com.app.phonebook.base.extension.areSystemAnimationsEnabled
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.getProperPrimaryColor
import com.app.phonebook.base.extension.handleGenericContactClick
import com.app.phonebook.base.extension.navigationBarHeight
import com.app.phonebook.base.extension.removeContactsFromGroup
import com.app.phonebook.base.extension.sendEmailToContacts
import com.app.phonebook.base.extension.sendSMSToContacts
import com.app.phonebook.base.extension.showErrorToast
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.extension.underlineText
import com.app.phonebook.base.extension.updateTextColors
import com.app.phonebook.base.utils.GROUP
import com.app.phonebook.base.utils.NavigationIcon
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.Group
import com.app.phonebook.databinding.ActivityGroupContactsBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.presentation.dialog.SelectContactsDialog

class GroupContactsActivity : BaseActivity<ActivityGroupContactsBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityGroupContactsBinding {
        return ActivityGroupContactsBinding.inflate(inflater)
    }

    companion object {
        private const val INTENT_SELECT_RINGTONE = 600
    }

    private var allContacts = ArrayList<Contact>()
    private var groupContacts = ArrayList<Contact>()
    private var wasInit = false

    lateinit var group: Group


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        updateTextColors(binding.groupContactsCoordinator)
        setupOptionsMenu()

        updateMaterialActivityViews(
            binding.groupContactsCoordinator, binding.groupContactsList, useTransparentNavigation = true, useTopSearchMenu = false
        )
        setupMaterialScrollListener(binding.groupContactsList, binding.groupContactsToolbar)

        group = intent.extras?.getSerializable(GROUP) as Group
        binding.groupContactsToolbar.title = group.title

        binding.groupContactsFab.setOnClickListener {
            if (wasInit) {
                fabClicked()
            }
        }

        binding.groupContactsPlaceholder2.setOnClickListener {
            fabClicked()
        }

        val properPrimaryColor = getProperPrimaryColor()
//        binding.groupContactsFastscroller.updateColors(properPrimaryColor)
        binding.groupContactsPlaceholder2.underlineText()
        binding.groupContactsPlaceholder2.setTextColor(properPrimaryColor)
    }

    override fun onResume() {
        super.onResume()
        refreshContacts()
        setupToolbar(binding.groupContactsToolbar, NavigationIcon.Arrow)
        (binding.groupContactsFab.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin =
            navigationBarHeight + resources.getDimension(R.dimen.activity_margin).toInt()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == INTENT_SELECT_RINGTONE && resultCode == Activity.RESULT_OK && resultData != null) {
            val extras = resultData.extras
            if (extras?.containsKey(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) == true) {
                val uri = extras.getParcelable<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) ?: return
                try {
                    setRingtoneOnSelected(uri)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }

    private fun setupOptionsMenu() {
        binding.groupContactsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.send_sms_to_group -> sendSMSToGroup()
                R.id.send_email_to_group -> sendEmailToGroup()
                R.id.assign_ringtone_to_group -> assignRingtoneToGroup()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun sendSMSToGroup() {
        if (groupContacts.isEmpty()) {
            toast(R.string.no_contacts_found)
        } else {
            sendSMSToContacts(groupContacts)
        }
    }

    private fun sendEmailToGroup() {
        if (groupContacts.isEmpty()) {
            toast(R.string.no_contacts_found)
        } else {
            sendEmailToContacts(groupContacts)
        }
    }

    private fun getDefaultRingtoneUri() = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)

    private fun getRingtonePickerIntent(): Intent {
        val defaultRingtoneUri = getDefaultRingtoneUri()

        return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultRingtoneUri)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultRingtoneUri)
        }
    }

    private fun assignRingtoneToGroup() {
        val ringtonePickerIntent = getRingtonePickerIntent()
        try {
            startActivityForResult(ringtonePickerIntent, INTENT_SELECT_RINGTONE)
        } catch (e: Exception) {
            toast(e.toString())
        }
    }

    private fun fabClicked() {
        SelectContactsDialog(this, allContacts, true, false, groupContacts) { addedContacts, removedContacts ->
            ensureBackgroundThread {
                addContactsToGroup(addedContacts, group.id!!)
                removeContactsFromGroup(removedContacts, group.id!!)
                refreshContacts()
            }
        }
    }

    private fun refreshContacts() {
        ContactsHelper(this).getContacts {
            wasInit = true
            allContacts = it

            groupContacts = it.filter { contact: Contact ->
                contact.groups.map { group: Group ->
                    group.id
                }.contains(group.id)
            } as ArrayList<Contact>

            binding.groupContactsPlaceholder2.beVisibleIf(groupContacts.isEmpty())
            binding.groupContactsPlaceholder.beVisibleIf(groupContacts.isEmpty())
            binding.groupContactsFastscroller.beVisibleIf(groupContacts.isNotEmpty())
            updateContacts(groupContacts)
        }
    }

    private fun updateContacts(contacts: ArrayList<Contact>) {
        val currAdapter = binding.groupContactsList.adapter
        if (currAdapter == null) {
            ContactsAdapter(
                this,
                contacts = contacts,
                recyclerView = binding.groupContactsList,
                highlightText = "",
            ) {
                contactClicked(it as Contact)
            }.apply {
                binding.groupContactsList.adapter = this
            }

            if (areSystemAnimationsEnabled) {
                binding.groupContactsList.scheduleLayoutAnimation()
            }
        } else {
            (currAdapter as ContactsAdapter).updateItems(contacts)
        }
    }

    private fun contactClicked(contact: Contact) {
        handleGenericContactClick(contact)
    }

    private fun setRingtoneOnSelected(uri: Uri) {
        groupContacts.forEach {
            ContactsHelper(this).updateRingtone(it.contactId.toString(), uri.toString())
        }
    }
}