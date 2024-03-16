package com.app.phonebook.base.helpers

import android.R.attr
import android.content.res.Resources
import android.text.Editable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.SingleLineTransformationMethod
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.TextView
import com.app.phonebook.R


class AutoFitHelper private constructor(view: TextView) {
    private val mTextView: TextView
    private val mPaint: TextPaint

    /**
     * Original textSize of the TextView.
     */
    private var mTextSize = 0f

    /**
     * @see TextView.getMaxLines
     */
    private var maxLines: Int

    /**
     * Returns the minimum size (in pixels) of the text.
     */
    var minTextSize: Float
        private set

    /**
     * Returns the maximum size (in pixels) of the text.
     */
    var maxTextSize: Float
        private set

    /**
     * Returns the amount of precision used to calculate the correct text size to fit within its
     * bounds.
     */
    var precision: Float
        private set

    /**
     * Returns whether or not automatically resizing text is enabled.
     */
    var isEnabled = false
        private set
    private var mIsAutoFitting = false
    private var mListeners: ArrayList<OnTextSizeChangeListener>? = null
    private val mTextWatcher: TextWatcher = AutoFitTextWatcher()
    private val mOnLayoutChangeListener: OnLayoutChangeListener = AutoFitOnLayoutChangeListener()

    init {
        val context = view.context
        val scaledDensity = context.resources.displayMetrics.density
        mTextView = view
        mPaint = TextPaint()
        setRawTextSize(view.textSize)
        maxLines = getMaxLines(view)
        minTextSize = scaledDensity * DEFAULT_MIN_TEXT_SIZE
        maxTextSize = mTextSize
        precision = DEFAULT_PRECISION
    }

    /**
     * Adds an [OnTextSizeChangeListener] to the list of those whose methods are called
     * whenever the [TextView]'s `textSize` changes.
     */
    fun addOnTextSizeChangeListener(listener: OnTextSizeChangeListener): AutoFitHelper {
        if (mListeners == null) {
            mListeners = ArrayList()
        }
        mListeners!!.add(listener)
        return this
    }

    /**
     * Removes the specified [OnTextSizeChangeListener] from the list of those whose methods
     * are called whenever the [TextView]'s `textSize` changes.
     */
    fun removeOnTextSizeChangeListener(listener: OnTextSizeChangeListener): AutoFitHelper {
        if (mListeners != null) {
            mListeners!!.remove(listener)
        }
        return this
    }

    /**
     * Set the amount of precision used to calculate the correct text size to fit within its
     * bounds. Lower precision is more precise and takes more time.
     *
     * @param precision The amount of precision.
     */
    fun setPrecision(precision: Float): AutoFitHelper {
        if (this.precision != precision) {
            this.precision = precision
            autofit()
        }
        return this
    }

    /**
     * Set the minimum text size to the given value, interpreted as "scaled pixel" units. This size
     * is adjusted based on the current density and user font size preference.
     *
     * @param size The scaled pixel size.
     *
     * @attr ref me.grantland.R.styleable#AutofitTextView_minTextSize
     */
    fun setMinTextSize(size: Float): AutoFitHelper {
        return setMinTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    /**
     * Set the minimum text size to a given unit and value. See TypedValue for the possible
     * dimension units.
     *
     * @param unit The desired dimension unit.
     * @param size The desired size in the given units.
     *
     * @attr ref me.grantland.R.styleable#AutofitTextView_minTextSize
     */
    fun setMinTextSize(unit: Int, size: Float): AutoFitHelper {
        val context = mTextView.context
        var r = Resources.getSystem()
        if (context != null) {
            r = context.resources
        }
        setRawMinTextSize(TypedValue.applyDimension(unit, size, r.displayMetrics))
        return this
    }

    private fun setRawMinTextSize(size: Float) {
        if (size != minTextSize) {
            minTextSize = size
            autofit()
        }
    }

    /**
     * Set the maximum text size to the given value, interpreted as "scaled pixel" units. This size
     * is adjusted based on the current density and user font size preference.
     *
     * @param size The scaled pixel size.
     *
     * @attr ref android.R.styleable#TextView_textSize
     */
    fun setMaxTextSize(size: Float): AutoFitHelper {
        return setMaxTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    /**
     * Set the maximum text size to a given unit and value. See TypedValue for the possible
     * dimension units.
     *
     * @param unit The desired dimension unit.
     * @param size The desired size in the given units.
     *
     * @attr ref android.R.styleable#TextView_textSize
     */
    fun setMaxTextSize(unit: Int, size: Float): AutoFitHelper {
        val context = mTextView.context
        var r = Resources.getSystem()
        if (context != null) {
            r = context.resources
        }
        setRawMaxTextSize(TypedValue.applyDimension(unit, size, r.displayMetrics))
        return this
    }

    private fun setRawMaxTextSize(size: Float) {
        if (size != maxTextSize) {
            maxTextSize = size
            autofit()
        }
    }

    /**
     * @see TextView.setMaxLines
     */
    fun setMaxLines(lines: Int): AutoFitHelper {
        if (maxLines != lines) {
            maxLines = lines
            autofit()
        }
        return this
    }

    /**
     * Set the enabled state of automatically resizing text.
     */
    fun setEnabled(enabled: Boolean): AutoFitHelper {
        if (isEnabled != enabled) {
            isEnabled = enabled
            if (enabled) {
                mTextView.addTextChangedListener(mTextWatcher)
                mTextView.addOnLayoutChangeListener(mOnLayoutChangeListener)
                autofit()
            } else {
                mTextView.removeTextChangedListener(mTextWatcher)
                mTextView.removeOnLayoutChangeListener(mOnLayoutChangeListener)
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
            }
        }
        return this
    }

    var textSize: Float
        /**
         * Returns the original text size of the View.
         *
         * @see TextView.getTextSize
         */
        get() = mTextSize
        /**
         * Set the original text size of the View.
         *
         * @see TextView.setTextSize
         */
        set(size) {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        }

    /**
     * Set the original text size of the View.
     *
     * @see TextView.setTextSize
     */
    fun setTextSize(unit: Int, size: Float) {
        if (mIsAutoFitting) {
            return
        }

        val context = mTextView.context
        var r = Resources.getSystem()
        if (context != null) {
            r = context.resources
        }
        setRawTextSize(TypedValue.applyDimension(unit, size, r.displayMetrics))
    }

    private fun setRawTextSize(size: Float) {
        if (mTextSize != size) {
            mTextSize = size
        }
    }

    fun autofit() {
        val oldTextSize = mTextView.textSize
        mIsAutoFitting = true
        autoFit(
            mTextView, mPaint, minTextSize, maxTextSize, maxLines, precision
        )
        mIsAutoFitting = false
        val textSize: Float = mTextView.textSize
        if (textSize != oldTextSize) {
            sendTextSizeChange(textSize, oldTextSize)
        }
    }

    private fun sendTextSizeChange(textSize: Float, oldTextSize: Float) {
        if (mListeners == null) {
            return
        }
        for (listener in mListeners!!) {
            listener.onTextSizeChange(textSize, oldTextSize)
        }
    }

    inner class AutoFitTextWatcher : TextWatcher {
        override fun beforeTextChanged(
            charSequence: CharSequence, start: Int, count: Int, after: Int
        ) {
            // do nothing
        }

        override fun onTextChanged(
            charSequence: CharSequence, start: Int, before: Int, count: Int
        ) {
            autofit()
        }

        override fun afterTextChanged(editable: Editable) {
            // do nothing
        }
    }

    inner class AutoFitOnLayoutChangeListener : OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            autofit()
        }
    }

    /**
     * When an object of a type is attached to an `AutofitHelper`, its methods will be called
     * when the `textSize` is changed.
     */
    interface OnTextSizeChangeListener {
        /**
         * This method is called to notify you that the size of the text has changed to
         * `textSize` from `oldTextSize`.
         */
        fun onTextSizeChange(textSize: Float, oldTextSize: Float)
    }

    companion object {
        private const val TAG = "AutoFitTextHelper"
        private const val SPEW = false

        // Minimum size of the text in pixels
        private const val DEFAULT_MIN_TEXT_SIZE = 8 //sp

        // How precise we want to be when reaching the target textWidth size
        private const val DEFAULT_PRECISION = 0.5f
        /**
         * Creates a new instance of `AutofitHelper` that wraps a [TextView] and enables
         * automatically sizing the text to fit.
         */
        /**
         * Creates a new instance of `AutofitHelper` that wraps a [TextView] and enables
         * automatically sizing the text to fit.
         */
        /**
         * Creates a new instance of `AutofitHelper` that wraps a [TextView] and enables
         * automatically sizing the text to fit.
         */
        @JvmOverloads
        fun create(view: TextView, attrs: AttributeSet? = null, defStyle: Int = 0): AutoFitHelper {
            val helper = AutoFitHelper(view)
            var sizeToFit = true
            if (attrs != null) {
                val context = view.context
                var minTextSize = helper.minTextSize.toInt()
                var precision = helper.precision
                val ta = context.obtainStyledAttributes(
                    attrs, R.styleable.AutoFitTextView, defStyle, 0
                )
                sizeToFit = ta.getBoolean(R.styleable.AutoFitTextView_sizeToFit, sizeToFit)
                minTextSize = ta.getDimensionPixelSize(
                    R.styleable.AutoFitTextView_minTextSize, minTextSize
                )
                precision = ta.getFloat(R.styleable.AutoFitTextView_precision, precision)
                ta.recycle()
                helper.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, minTextSize.toFloat())
                    .setPrecision(precision)
            }
            helper.setEnabled(sizeToFit)
            return helper
        }

        /**
         * Re-sizes the textSize of the TextView so that the text fits within the bounds of the View.
         */
        fun autoFit(
            view: TextView,
            paint: TextPaint,
            minTextSize: Float,
            maxTextSize: Float,
            maxLines: Int,
            precision: Float
        ) {
            if (maxLines <= 0 || maxLines == Int.MAX_VALUE) {
                // Don't auto-size since there's no limit on lines.
                return
            }
            val targetWidth = view.width - view.paddingLeft - view.paddingRight
            if (targetWidth <= 0) {
                return
            }
            var text = view.text
            val method = view.transformationMethod
            if (method != null) {
                text = method.getTransformation(text, view)
            }
            val context = view.context
            var r = Resources.getSystem()
            var size = maxTextSize
            val high = size
            val low = 0f
            if (context != null) {
                r = context.resources
            }
            val displayMetrics: DisplayMetrics = r.displayMetrics
            paint.set(view.paint)
            paint.textSize = size
            if (maxLines == 1 && paint.measureText(
                    text,
                    0,
                    text.length
                ) > targetWidth || getLineCount(
                    text,
                    paint,
                    size,
                    targetWidth,
                    displayMetrics
                ) > maxLines
            ) {
                size = getAutoFitTextSize(
                    text,
                    paint,
                    targetWidth.toFloat(),
                    maxLines,
                    low,
                    high,
                    precision,
                    displayMetrics
                )
            }
            if (size < minTextSize) {
                size = minTextSize
            }
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        }

        /**
         * Recursive binary search to find the best size for the text.
         */
        private fun getAutoFitTextSize(
            text: CharSequence,
            paint: TextPaint,
            targetWidth: Float,
            maxLines: Int,
            low: Float,
            high: Float,
            precision: Float,
            displayMetrics: DisplayMetrics
        ): Float {
            val mid = (low + high) / 2.0f
            var lineCount = 1
            var layout: StaticLayout? = null
            paint.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX, mid, displayMetrics
            )
            if (maxLines != 1) {
                layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, attr.width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0.0f, 1.0f)
                    .setIncludePad(true).build()

                lineCount = layout.lineCount
            }
            if (SPEW) Log.d(
                TAG,
                "low=$low high=$high mid=$mid target=$targetWidth maxLines=$maxLines lineCount=$lineCount"
            )
            return if (lineCount > maxLines) {
                // For the case that `text` has more newline characters than `maxLines`.
                if (high - low < precision) {
                    low
                } else getAutoFitTextSize(
                    text, paint, targetWidth, maxLines, low, mid, precision, displayMetrics
                )
            } else if (lineCount < maxLines) {
                getAutoFitTextSize(
                    text, paint, targetWidth, maxLines, mid, high, precision, displayMetrics
                )
            } else {
                var maxLineWidth = 0f
                if (maxLines == 1) {
                    maxLineWidth = paint.measureText(text, 0, text.length)
                } else {
                    for (i in 0 until lineCount) {
                        if (layout!!.getLineWidth(i) > maxLineWidth) {
                            maxLineWidth = layout.getLineWidth(i)
                        }
                    }
                }
                if (high - low < precision) {
                    low
                } else if (maxLineWidth > targetWidth) {
                    getAutoFitTextSize(
                        text, paint, targetWidth, maxLines, low, mid, precision, displayMetrics
                    )
                } else if (maxLineWidth < targetWidth) {
                    getAutoFitTextSize(
                        text, paint, targetWidth, maxLines, mid, high, precision, displayMetrics
                    )
                } else {
                    mid
                }
            }
        }

        private fun getLineCount(
            text: CharSequence,
            paint: TextPaint,
            size: Float,
            width: Int,
            displayMetrics: DisplayMetrics
        ): Int {
            paint.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX, size, displayMetrics
            )

            val staticLayout: StaticLayout =
                StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0.0f, 1.0f)
                    .setIncludePad(true)
                    .build()

            return staticLayout.lineCount
        }

        fun getMaxLines(view: TextView): Int {
            val maxLines: Int  // No limit (Integer.MAX_VALUE also means no limit)
            val method = view.transformationMethod
            maxLines = if (method != null && method is SingleLineTransformationMethod) {
                1
            } else {
                view.maxLines
            }

            return maxLines
        }
    }
}

