package com.app.phonebook.presentation.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.app.phonebook.R
import com.app.phonebook.base.helpers.AutoFitHelper
import java.util.WeakHashMap

class AutoFitLayout : FrameLayout {
    private var mEnabled = false
    private var mMinTextSize = 0f
    private var mPrecision = 0f
    private val mHelpers: WeakHashMap<View, AutoFitHelper> = WeakHashMap<View, AutoFitHelper>()

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(context, attrs, defStyle)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        var sizeToFit = true
        var minTextSize = -1
        var precision = -1f
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(
                attrs, R.styleable.AutoFitTextView, defStyle, 0
            )
            sizeToFit = ta.getBoolean(R.styleable.AutoFitTextView_sizeToFit, sizeToFit)
            minTextSize = ta.getDimensionPixelSize(
                R.styleable.AutoFitTextView_minTextSize, minTextSize
            )
            precision = ta.getFloat(R.styleable.AutoFitTextView_precision, precision)
            ta.recycle()
        }
        mEnabled = sizeToFit
        mMinTextSize = minTextSize.toFloat()
        mPrecision = precision
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        val textView = child as TextView
        val helper: AutoFitHelper = AutoFitHelper.create(textView).setEnabled(mEnabled)
        if (mPrecision > 0) {
            helper.setPrecision(mPrecision)
        }
        if (mMinTextSize > 0) {
            helper.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, mMinTextSize)
        }
        mHelpers[textView] = helper
    }

    fun getAutoFitHelper(textView: TextView): AutoFitHelper? {
        return mHelpers[textView]
    }

    fun getAutoFitHelper(index: Int): AutoFitHelper? {
        return mHelpers[getChildAt(index)]
    }
}

