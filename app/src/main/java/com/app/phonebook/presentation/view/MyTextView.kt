package com.app.phonebook.presentation.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.app.phonebook.R

class MyTextView : AppCompatTextView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    )

    init {
        val fontRes = R.font.sofia_pro_regular
        typeface = ResourcesCompat.getFont(context, fontRes)
    }

    fun setColors(textColor: Int, accentColor: Int) {
        setTextColor(textColor)
        setLinkTextColor(accentColor)
    }
}
