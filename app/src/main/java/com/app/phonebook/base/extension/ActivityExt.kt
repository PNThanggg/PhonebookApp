package com.app.phonebook.base.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.graphics.Color
import com.app.phonebook.R
import com.app.phonebook.base.utils.SIDELOADING_FALSE
import com.app.phonebook.base.utils.SIDELOADING_TRUE

fun Activity.finishWithSlide() {
    finish()
    if (isSdk34()) {
        overrideActivityTransition(
            OVERRIDE_TRANSITION_CLOSE,
            R.anim.slide_in_left,
            R.anim.slide_out_right,
            Color.TRANSPARENT
        )
    } else {
        @Suppress("DEPRECATION") overridePendingTransition(
            R.anim.slide_in_left, R.anim.slide_out_right
        )
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
fun Activity.isAppSideLoaded(): Boolean {
    return try {
        getDrawable(R.drawable.ic_camera_vector)
        false
    } catch (e: Exception) {
        true
    }
}



