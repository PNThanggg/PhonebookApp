package com.app.phonebook.base.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.HapticFeedbackConstants
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

/**
 * Triggers haptic feedback for this view.
 *
 * This extension function for `View` invokes the system's haptic feedback mechanism specifically
 * for a virtual key press. It utilizes the `VIRTUAL_KEY` constant from `HapticFeedbackConstants`
 * to simulate the feeling of pressing a physical button, enhancing the user's tactile experience
 * with the UI.
 *
 * The function directly calls `performHapticFeedback` without additional flags, relying on the
 * system's default settings to respect the user's preferences regarding haptic feedback.
 *
 * Note: For this function to work, the calling view must be enabled and must have haptic feedback
 * enabled in its view hierarchy. Additionally, the device needs to support haptic feedback, and the
 * feature must be enabled in the user's device settings.
 */
fun View.performHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

val View.boundingBox
    get() = Rect().also { getGlobalVisibleRect(it) }
