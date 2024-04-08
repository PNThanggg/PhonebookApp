package com.app.phonebook.presentation.dialog

import com.app.phonebook.R
import com.app.phonebook.base.extension.getAlertDialogBuilder
import com.app.phonebook.base.extension.setupDialogStuff
import com.app.phonebook.base.view.BaseActivity

class DateTimePatternInfoDialog(activity: BaseActivity<*>) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.datetime_pattern_info_layout, null)
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> run { } }
            .apply {
                activity.setupDialogStuff(view, this)
            }
    }
}
