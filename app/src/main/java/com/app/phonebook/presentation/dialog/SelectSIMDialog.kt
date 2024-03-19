package com.app.phonebook.presentation.dialog

import android.annotation.SuppressLint
import android.telecom.PhoneAccountHandle
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.app.phonebook.R
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.getAvailableSIMCardLabels
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.DialogSelectSimBinding

@SuppressLint("MissingPermission", "SetTextI18n", "InflateParams")
class SelectSIMDialog(
    val activity: BaseActivity<*>,
    val phoneNumber: String,
    onDismiss: () -> Unit = {},
    val callback: (handle: PhoneAccountHandle?) -> Unit
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogSelectSimBinding.inflate(activity.layoutInflater, null, false)

    init {
        binding.selectSimRememberHolder.setOnClickListener {
            binding.selectSimRemember.toggle()
        }

        activity.getAvailableSIMCardLabels().forEachIndexed { index, simAccount ->
            val radioButton = (activity.layoutInflater.inflate(R.layout.radio_button, null) as RadioButton).apply {
                text = "${index + 1} - ${simAccount.label}"
                id = index
                setOnClickListener {
                    selectedSIM(simAccount.handle)
                }
            }
            binding.selectSimRadioGroup.addView(
                radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
        }

        activity.getAlertDialogBuilder().apply {
            activity.setupDialogStuff(binding.root, this) { alertDialog ->
                dialog = alertDialog
            }
        }

        dialog?.setOnDismissListener {
            onDismiss()
        }
    }

    private fun selectedSIM(handle: PhoneAccountHandle) {
        if (binding.selectSimRemember.isChecked) {
            activity.config.saveCustomSIM(phoneNumber, handle)
        }

        callback(handle)
        dialog?.dismiss()
    }
}
