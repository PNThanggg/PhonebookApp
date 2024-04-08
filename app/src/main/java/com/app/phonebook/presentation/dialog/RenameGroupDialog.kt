package com.app.phonebook.presentation.dialog

import androidx.appcompat.app.AlertDialog
import com.app.phonebook.R
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.groupsDB
import com.app.phonebook.base.extension.isAValidFilename
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.extension.showKeyboard
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.extension.value
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Group
import com.app.phonebook.databinding.DialogRenameGroupBinding
import com.app.phonebook.helpers.ContactsHelper

class RenameGroupDialog(
    val activity: BaseActivity<*>,
    val group: Group, val
    callback: () -> Unit
) {
    init {
        val binding = DialogRenameGroupBinding.inflate(activity.layoutInflater).apply {
            renameGroupTitle.setText(group.title)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.rename) { alertDialog ->
                    alertDialog.showKeyboard(binding.renameGroupTitle)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val newTitle = binding.renameGroupTitle.value
                        if (newTitle.isEmpty()) {
                            activity.toast(R.string.empty_name)
                            return@setOnClickListener
                        }

                        if (!newTitle.isAValidFilename()) {
                            activity.toast(R.string.invalid_name)
                            return@setOnClickListener
                        }

                        group.title = newTitle
                        group.contactsCount = 0
                        ensureBackgroundThread {
                            if (group.isPrivateSecretGroup()) {
                                activity.groupsDB.insertOrUpdate(group)
                            } else {
                                ContactsHelper(activity).renameGroup(group)
                            }

                            activity.runOnUiThread {
                                callback()
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }
}
