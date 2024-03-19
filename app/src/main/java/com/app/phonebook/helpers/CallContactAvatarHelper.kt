package com.app.phonebook.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import com.app.phonebook.R
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.isQPlus
import com.app.phonebook.data.models.CallContact

class CallContactAvatarHelper(private val context: Context) {
    /**
     * Retrieves the avatar of a call contact as a circular bitmap.
     *
     * This function attempts to fetch the call contact's photo specified by the photoUri in the CallContact object.
     * For Android 10 (API level 29) and above, it uses the ContentResolver's loadThumbnail method to get a thumbnail
     * of the specified dimensions. For older versions, it falls back to using MediaStore.Images.Media.getBitmap.
     * After fetching the image, it transforms the bitmap into a circular shape.
     *
     * @param callContact The call contact whose avatar is to be retrieved. Can be null.
     * @return A circular bitmap of the contact's avatar, or null if the photoUri is empty, an error occurs, or the contact is null.
     */
    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    fun getCallContactAvatar(callContact: CallContact?): Bitmap? {
        var bitmap: Bitmap? = null
        if (callContact?.photoUri?.isNotEmpty() == true) {
            val photoUri = Uri.parse(callContact.photoUri)
            try {
                val contentResolver = context.contentResolver
                bitmap = if (isQPlus()) {
                    val tmbSize = context.resources.getDimension(R.dimen.list_avatar_size).toInt()
                    contentResolver.loadThumbnail(photoUri, Size(tmbSize, tmbSize), null)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                }
                bitmap = getCircularBitmap(bitmap!!)
            } catch (ignored: Exception) {
                Log.e(APP_NAME, "getCallContactAvatar: ${ignored.message}")
                return null
            }
        }

        return bitmap
    }

    /**
     * Converts a rectangular bitmap into a circular bitmap.
     *
     * This function creates a new bitmap with the same width as the original but transforms the image into a circle.
     * It is useful for UI elements like avatars where a circular image is preferred over a rectangular one. The process
     * involves drawing the original bitmap into a canvas with a circular clip, effectively cropping the image into a
     * circle.
     *
     * @param bitmap The original rectangular bitmap to be transformed.
     * @return A new bitmap where the original bitmap's content is cropped into a circle.
     *
     * Note: The returned bitmap will have the same width and height, which are set equal to the width of the original
     * bitmap, thus assuming the original bitmap is square. If the original bitmap is not square, it will be scaled
     * to fit the circle, potentially distorting the image.
     */
    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.width, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val radius = bitmap.width / 2.toFloat()

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(radius, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }
}
