package com.app.phonebook.base.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.StyleRes
import com.app.phonebook.R
import com.app.phonebook.base.utils.SHORT_ANIMATION_DURATION

fun View.beInvisibleIf(beInvisible: Boolean) = if (beInvisible) beInvisible() else beVisible()

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

fun View.beInvisible() {
    visibility = View.INVISIBLE
}

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (viewTreeObserver != null) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                callback()
            }
        }
    })
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.isGone() = visibility == View.GONE

//fun View.performHapticFeedback() = performHapticFeedback(
//    HapticFeedbackConstants.VIRTUAL_KEY,
//    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
//)

fun View.fadeIn() {
    animate().alpha(1f).setDuration(SHORT_ANIMATION_DURATION).withStartAction { beVisible() }
        .start()
}

fun View.fadeOut() {
    animate().alpha(0f).setDuration(SHORT_ANIMATION_DURATION).withEndAction { beGone() }.start()
}

fun View.throwIfMissingAttrs(@StyleRes styleRes: Int, block: () -> Unit) {
    try {
        block()
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(
            "This ${this::class.java.simpleName} is missing an attribute. " +
                    "Add it to its style, or make the style inherit from " +
                    "${resources.getResourceName(styleRes)}.",
            e
        )
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
fun View.setupViewBackground(context: Context) {
    background = if (context.baseConfig.isUsingSystemTheme) {
        resources.getDrawable(R.drawable.selector_clickable_you, context.theme)
    } else {
        resources.getDrawable(R.drawable.selector_clickable, context.theme)
    }
}

val View.boundingBox
    get() = Rect().also { getGlobalVisibleRect(it) }
