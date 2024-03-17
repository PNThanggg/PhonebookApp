package com.app.phonebook.presentation.dialog

import androidx.appcompat.app.AlertDialog
import com.app.phonebook.adapter.RecentCallsAdapter
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.RecentCall
import com.app.phonebook.databinding.DialogShowGroupedCallsBinding
import com.app.phonebook.helpers.RecentHelper

class ShowGroupedCallsDialog(val activity: BaseActivity<*>, callIds: ArrayList<Int>) {
    private var dialog: AlertDialog? = null
    private val binding = DialogShowGroupedCallsBinding.inflate(activity.layoutInflater, null, false)

    init {
        RecentHelper(activity).getRecentCalls(false) { allRecent ->
            val listRecentCall = allRecent.filter { callIds.contains(it.id) }.toMutableList() as ArrayList<RecentCall>
            activity.runOnUiThread {
                RecentCallsAdapter(activity, listRecentCall, binding.selectGroupedCallsList, null, false) {}.apply {
                    binding.selectGroupedCallsList.adapter = this
                }
            }
        }

        activity.getAlertDialogBuilder().apply {
            activity.setupDialogStuff(binding.root, this) { alertDialog ->
                dialog = alertDialog
            }
        }
    }
}
