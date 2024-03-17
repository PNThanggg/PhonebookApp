package com.app.phonebook.presentation.dialog

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import com.app.phonebook.R
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.getCurrentFormattedDateTime
import com.app.phonebook.base.extension.isAValidFilename
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.extension.value
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.DialogExportCallHistoryBinding

@SuppressLint("SetTextI18n")
class ExportCallHistoryDialog(val activity: BaseActivity<*>, callback: (filename: String) -> Unit) {

    init {
        val binding = DialogExportCallHistoryBinding.inflate(activity.layoutInflater).apply {
            exportCallHistoryFilename.setText("call_history_${activity.getCurrentFormattedDateTime()}")
        }

        activity.getAlertDialogBuilder().setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).apply {
            activity.setupDialogStuff(binding.root, this, R.string.export_call_history) { alertDialog ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                    val filename = binding.exportCallHistoryFilename.value
                    when {
                        filename.isEmpty() -> activity.toast(R.string.empty_name)
                        filename.isAValidFilename() -> {
                            callback(filename)
                            alertDialog.dismiss()
                        }

                        else -> activity.toast(R.string.invalid_name)
                    }
                }
            }
        }
    }
}
