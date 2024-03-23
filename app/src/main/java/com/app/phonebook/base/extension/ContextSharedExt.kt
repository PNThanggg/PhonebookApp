package com.app.phonebook.base.extension

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.app.phonebook.base.helpers.SharedHelper
import com.app.phonebook.base.utils.PREFS_KEY
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.data.models.SharedTheme

/**
 * Retrieves a `SharedPreferences` instance specific to the application.
 *
 * This extension function for `Context` simplifies the access to the application's shared preferences
 * by providing a direct method to retrieve a `SharedPreferences` instance. It uses a predefined key (`PREFS_KEY`)
 * to access the shared preferences file in private mode, ensuring that the preferences are accessible only
 * by the calling application.
 *
 * Private mode (`Context.MODE_PRIVATE`) is used to ensure that the shared preferences are not shared with other
 * applications, providing a secure way to store private information.
 *
 * @return The `SharedPreferences` instance associated with `PREFS_KEY`.
 *
 * Note: `PREFS_KEY` should be a constant defined in your application's code that uniquely identifies the
 * shared preferences file. It is recommended to use a specific string that relates to your application's domain
 * or package name to avoid conflicts with other applications.
 */
fun Context.getSharedPrefs(): SharedPreferences = getSharedPreferences(
    PREFS_KEY, Context.MODE_PRIVATE
)

/**
 * Asynchronously retrieves the shared theme settings.
 *
 * This extension function for `Context` asynchronously fetches the shared theme settings using a content provider.
 * It ensures that the operation is performed on a background thread to avoid blocking the UI thread, adhering to
 * best practices for smooth UI performance. Upon completion, the provided callback function is called with the
 * fetched `SharedTheme` object, or `null` if the theme settings could not be retrieved.
 *
 * The retrieval process uses a custom `CursorLoader` obtained from `getMyContentProviderCursorLoader`, tailored
 * to query the application's specific content provider for theme settings.
 *
 * @param callback A lambda function to be invoked after the shared theme has been retrieved. It accepts a
 *                 `SharedTheme?` as its parameter, allowing for nullable return types in case the theme settings
 *                 are unavailable or the operation fails.
 *
 * Note: This function should be called from a context where asynchronous operations are allowed. The callback
 * will be executed on the thread where `ensureBackgroundThread` completes, so any UI operations in the callback
 * must be run on the UI thread.
 */
fun Context.getSharedTheme(callback: (sharedTheme: SharedTheme?) -> Unit) {
    val cursorLoader = getMyContentProviderCursorLoader()
    ensureBackgroundThread {
        callback(SharedHelper.getSharedThemeSync(cursorLoader))
    }
}

/**
 * Checks if the system dark theme is currently enabled.
 *
 * This extension function for `Context` determines whether the system's dark theme is active based on the
 * UI mode configuration. It utilizes the `uiMode` property of the `Configuration` object, which contains UI
 * mode information about the current configuration. By performing a bitwise AND operation with `UI_MODE_NIGHT_YES`,
 * it checks if the night mode is currently set to yes.
 *
 * @return True if the system dark theme is enabled, false otherwise.
 *
 * This function can be particularly useful for adjusting the application's theme dynamically in response to
 * the system theme settings, enhancing the user experience by aligning the app's appearance with the user's
 * system-wide preferences.
 */
fun Context.isUsingSystemDarkTheme() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0
