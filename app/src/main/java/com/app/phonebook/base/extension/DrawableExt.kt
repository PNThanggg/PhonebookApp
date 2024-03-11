package com.app.phonebook.base.extension

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build

@Suppress("DEPRECATION")
fun Drawable.applyColorFilter(color: Int) {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mutate().colorFilter = BlendModeColorFilter(
            color, BlendMode.SRC_IN
        )
    } else {
        mutate().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }
}

fun Drawable.convertToBitmap(): Bitmap {
    val bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    } else {
        Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    }

    if (this is BitmapDrawable) {
        if (this.bitmap != null) {
            return this.bitmap
        }
    }

    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}
