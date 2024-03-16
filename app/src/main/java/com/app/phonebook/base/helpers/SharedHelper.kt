package com.app.phonebook.base.helpers

import android.util.Log
import androidx.loader.content.CursorLoader
import com.app.phonebook.base.extension.getIntValue
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.data.models.SharedTheme

object SharedHelper {
    /**
     * Synchronously retrieves the shared theme settings using a cursor loader.
     *
     * This function fetches theme settings such as text color, background color, primary color, accent color,
     * app icon color, and the timestamp of the last update, from a cursor loader provided as an argument.
     * It is designed to run synchronously, making it suitable for background thread operations where
     * immediate return of data is necessary.
     *
     * The function queries the content provider specified by the cursor loader, `cursorLoader.loadInBackground()`,
     * and attempts to read the theme settings from the first row of the cursor. If successful, it constructs
     * and returns a `SharedTheme` object containing these settings. In case of failure (e.g., due to an exception
     * while reading the cursor), it logs the error and returns `null`, indicating that the theme settings could
     * not be retrieved.
     *
     * @param cursorLoader The `CursorLoader` used to query the content provider for theme settings.
     * @return A `SharedTheme?` object containing the retrieved theme settings, or `null` if the settings
     *         could not be retrieved or an error occurred during the operation.
     *
     * Note: This function should only be called from a background thread to avoid blocking the main UI thread,
     * as it performs I/O operations that can be time-consuming.
     */
    fun getSharedThemeSync(cursorLoader: CursorLoader): SharedTheme? {
        val cursor = cursorLoader.loadInBackground()
        cursor?.use {
            if (cursor.moveToFirst()) {
                try {
                    val textColor = cursor.getIntValue(MyContentProvider.COL_TEXT_COLOR)
                    val backgroundColor = cursor.getIntValue(MyContentProvider.COL_BACKGROUND_COLOR)
                    val primaryColor = cursor.getIntValue(MyContentProvider.COL_PRIMARY_COLOR)
                    val accentColor = cursor.getIntValue(MyContentProvider.COL_ACCENT_COLOR)
                    val appIconColor = cursor.getIntValue(MyContentProvider.COL_APP_ICON_COLOR)
                    val lastUpdatedTS = cursor.getIntValue(MyContentProvider.COL_LAST_UPDATED_TS)
                    return SharedTheme(
                        textColor,
                        backgroundColor,
                        primaryColor,
                        appIconColor,
                        lastUpdatedTS,
                        accentColor
                    )
                } catch (e: Exception) {
                    Log.e(APP_NAME, "getSharedThemeSync: ${e.message}")
                }
            }
        }

        return null
    }
}