package com.app.phonebook.presentation.dialog

import androidx.appcompat.app.AlertDialog
import com.app.phonebook.R
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.getProperPrimaryColor
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Group
import com.app.phonebook.databinding.DialogSelectGroupsBinding
import com.app.phonebook.databinding.ItemCheckboxBinding
import com.app.phonebook.databinding.ItemTextviewBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.presentation.view.MyAppCompatCheckbox

class SelectGroupsDialog(
    val activity: BaseActivity<*>,
    private val selectedGroups: ArrayList<Group>,
    val callback: (newGroups: ArrayList<Group>) -> Unit
) {
    private val binding = DialogSelectGroupsBinding.inflate(activity.layoutInflater)
    private val checkboxes = ArrayList<MyAppCompatCheckbox>()
    private var groups = ArrayList<Group>()
    private var dialog: AlertDialog? = null

    init {
        ContactsHelper(activity).getStoredGroups {
            groups = it
            activity.runOnUiThread {
                initDialog()
            }
        }
    }

    private fun initDialog() {
        groups.sortedBy { it.title }.forEach {
            addGroupCheckbox(it)
        }

        addCreateNewGroupButton()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun addGroupCheckbox(group: Group) {
        ItemCheckboxBinding.inflate(activity.layoutInflater, null, false).apply {
            checkboxes.add(itemCheckbox)
            itemCheckboxHolder.setOnClickListener {
                itemCheckbox.toggle()
            }

            itemCheckbox.apply {
                isChecked = selectedGroups.contains(group)
                text = group.title
                tag = group.id
                setColors(
                    textColor = activity.getProperTextColor(),
                    accentColor = activity.getProperPrimaryColor()
                )
            }
            binding.dialogGroupsHolder.addView(this.root)
        }
    }

    private fun addCreateNewGroupButton() {
        val newGroup = Group(0, activity.getString(R.string.create_new_group))
        ItemTextviewBinding.inflate(activity.layoutInflater, null, false).itemTextview.apply {
            text = newGroup.title
            tag = newGroup.id
            setTextColor(activity.getProperTextColor())
            binding.dialogGroupsHolder.addView(this)
            setOnClickListener {
                CreateNewGroupDialog(activity) {
                    selectedGroups.add(it)
                    groups.add(it)
                    binding.dialogGroupsHolder.removeViewAt(binding.dialogGroupsHolder.childCount - 1)
                    addGroupCheckbox(it)
                    addCreateNewGroupButton()
                }
            }
        }
    }

    private fun dialogConfirmed() {
        val selectedGroups = ArrayList<Group>()
        checkboxes.filter { it.isChecked }.forEach {
            val groupId = it.tag as Long
            groups.firstOrNull { group: Group ->
                group.id == groupId
            }?.apply {
                selectedGroups.add(this)
            }
        }

        callback(selectedGroups)
    }
}
