package com.app.phonebook.presentation.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import com.app.phonebook.base.extension.adjustAlpha

class MyAppCompatCheckbox : AppCompatCheckBox {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    @SuppressLint("RestrictedApi")
    fun setColors(textColor: Int, accentColor: Int) {
        setTextColor(textColor)
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(textColor.adjustAlpha(0.6f), accentColor)
        )
        supportButtonTintList = colorStateList
    }
}
