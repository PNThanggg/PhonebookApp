package com.app.phonebook.presentation.dialog

import android.view.View
import androidx.appcompat.app.AlertDialog
import com.app.phonebook.R
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.getPrivateContactSource
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.extension.showKeyboard
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.extension.value
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.ContactSource
import com.app.phonebook.data.models.Group
import com.app.phonebook.data.models.RadioItem
import com.app.phonebook.databinding.DialogCreateNewGroupBinding
import com.app.phonebook.helpers.ContactsHelper

class CreateNewGroupDialog(
    val activity: BaseActivity<*>,
    val callback: (newGroup: Group) -> Unit
) {
    init {
        val binding = DialogCreateNewGroupBinding.inflate(activity.layoutInflater)

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.create_new_group) { alertDialog ->
                    alertDialog.showKeyboard(binding.groupName)

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                        val name = binding.groupName.value
                        if (name.isEmpty()) {
                            activity.toast(R.string.empty_name)
                            return@OnClickListener
                        }

                        ContactsHelper(activity).getContactSources { contactSources ->
                            contactSources.filter { it.type.contains("google", true) }
                                .mapTo(contactSources) { ContactSource(it.name, it.type, it.name) }

                            contactSources.add(activity.getPrivateContactSource())

                            val items = ArrayList<RadioItem>()
                            contactSources.forEachIndexed { index, contactSource ->
                                items.add(RadioItem(index, contactSource.publicName))
                            }

                            activity.runOnUiThread {
                                if (items.size == 1) {
                                    createGroupUnder(name, contactSources.first(), alertDialog)
                                } else {
                                    RadioGroupDialog(activity, items, titleId = R.string.create_group_under_account) { value ->
                                        val contactSource = contactSources[value as Int]
                                        createGroupUnder(name, contactSource, alertDialog)
                                    }
                                }
                            }
                        }
                    })
                }
            }
    }

    private fun createGroupUnder(name: String, contactSource: ContactSource, dialog: AlertDialog) {
        ensureBackgroundThread {
            val newGroup = ContactsHelper(activity).createNewGroup(name, contactSource.name, contactSource.type)
            activity.runOnUiThread {
                if (newGroup != null) {
                    callback(newGroup)
                }
                dialog.dismiss()
            }
        }
    }
}