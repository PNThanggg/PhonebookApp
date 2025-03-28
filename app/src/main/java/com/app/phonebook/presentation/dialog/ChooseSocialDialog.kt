package com.app.phonebook.presentation.dialog

import android.app.Activity
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.getPackageDrawable
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.data.models.SocialAction
import com.app.phonebook.databinding.DialogChooseSocialBinding
import com.app.phonebook.databinding.ItemChooseSocialBinding

class ChooseSocialDialog(
    val activity: Activity, actions: ArrayList<SocialAction>, val callback: (action: SocialAction) -> Unit
) {
    private lateinit var dialog: AlertDialog

    init {
        val binding = DialogChooseSocialBinding.inflate(activity.layoutInflater)

        actions.sortBy { it.type }

        actions.forEach { action ->
            val item = ItemChooseSocialBinding.inflate(activity.layoutInflater).apply {
                itemSocialLabel.text = action.label
                root.setOnClickListener {
                    callback(action)
                    dialog.dismiss()
                }

                val drawable = activity.getPackageDrawable(action.packageName)
                itemSocialImage.setImageDrawable(drawable)
            }

            binding.dialogChooseSocial.addView(
                item.root, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
        }

        val builder = activity.getAlertDialogBuilder()

        builder.apply {
            activity.setupDialogStuff(binding.root, this) { alertDialog ->
                dialog = alertDialog
            }
        }
    }
}
