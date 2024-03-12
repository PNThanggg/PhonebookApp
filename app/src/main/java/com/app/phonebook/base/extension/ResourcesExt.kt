package com.app.phonebook.base.extension

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable

@SuppressLint("UseCompatLoadingForDrawables")
fun Resources.getColoredBitmap(resourceId: Int, newColor: Int, context: Context): Bitmap {
    val drawable = getDrawable(resourceId, context.theme)
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.colorFilter = PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_IN)
    drawable.draw(canvas)
    return bitmap
}

fun Resources.getColoredDrawable(
    drawableId: Int,
    colorId: Int,
    alpha: Int = 255,
    context: Context
) = getColoredDrawableWithColor(
    drawableId = drawableId,
    color = getColor(
        colorId,
        context.theme
    ),
    alpha = alpha,
    context = context
)

@SuppressLint("UseCompatLoadingForDrawables")
fun Resources.getColoredDrawableWithColor(
    drawableId: Int,
    color: Int,
    alpha: Int = 255,
    context: Context
): Drawable {
    val drawable = getDrawable(drawableId, context.theme)
    drawable.mutate().applyColorFilter(color)
    drawable.mutate().alpha = alpha
    return drawable
}

@SuppressLint("DiscouragedApi")
fun Resources.hasNavBar(): Boolean {
    val id = getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && getBoolean(id)
}

@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun Resources.getNavBarHeight(): Int {
    val id = getIdentifier("navigation_bar_height", "dimen", "android")
    return if (id > 0 && hasNavBar()) {
        getDimensionPixelSize(id)
    } else {
        0
    }
}
