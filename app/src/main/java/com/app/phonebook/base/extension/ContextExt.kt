package com.app.phonebook.base.extension

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.loader.content.CursorLoader
import com.app.phonebook.R
import com.app.phonebook.base.helpers.BaseConfig
import com.app.phonebook.base.helpers.MyContentProvider
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.DARK_GREY
import com.app.phonebook.base.utils.FONT_SIZE_LARGE
import com.app.phonebook.base.utils.FONT_SIZE_MEDIUM
import com.app.phonebook.base.utils.FONT_SIZE_SMALL
import com.app.phonebook.base.utils.PERMISSION_ACCESS_COARSE_LOCATION
import com.app.phonebook.base.utils.PERMISSION_ACCESS_FINE_LOCATION
import com.app.phonebook.base.utils.PERMISSION_CALL_PHONE
import com.app.phonebook.base.utils.PERMISSION_CAMERA
import com.app.phonebook.base.utils.PERMISSION_GET_ACCOUNTS
import com.app.phonebook.base.utils.PERMISSION_MEDIA_LOCATION
import com.app.phonebook.base.utils.PERMISSION_POST_NOTIFICATIONS
import com.app.phonebook.base.utils.PERMISSION_READ_CALENDAR
import com.app.phonebook.base.utils.PERMISSION_READ_CALL_LOG
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.base.utils.PERMISSION_READ_MEDIA_AUDIO
import com.app.phonebook.base.utils.PERMISSION_READ_MEDIA_IMAGES
import com.app.phonebook.base.utils.PERMISSION_READ_MEDIA_VIDEO
import com.app.phonebook.base.utils.PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED
import com.app.phonebook.base.utils.PERMISSION_READ_PHONE_STATE
import com.app.phonebook.base.utils.PERMISSION_READ_SMS
import com.app.phonebook.base.utils.PERMISSION_READ_STORAGE
import com.app.phonebook.base.utils.PERMISSION_READ_SYNC_SETTINGS
import com.app.phonebook.base.utils.PERMISSION_RECORD_AUDIO
import com.app.phonebook.base.utils.PERMISSION_SEND_SMS
import com.app.phonebook.base.utils.PERMISSION_WRITE_CALENDAR
import com.app.phonebook.base.utils.PERMISSION_WRITE_CALL_LOG
import com.app.phonebook.base.utils.PERMISSION_WRITE_CONTACTS
import com.app.phonebook.base.utils.PERMISSION_WRITE_STORAGE
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.utils.TIME_FORMAT_12
import com.app.phonebook.base.utils.TIME_FORMAT_24
import com.app.phonebook.base.utils.isOnMainThread
import com.app.phonebook.base.utils.isQPlus
import com.app.phonebook.base.utils.isSPlus
import com.app.phonebook.data.models.ContactSource
import com.app.phonebook.data.models.SIMAccount
import com.app.phonebook.helpers.Config
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.MyContactsContentProvider
import com.app.phonebook.presentation.view.MyButton
import com.app.phonebook.presentation.view.MyEditText
import com.app.phonebook.presentation.view.MyFloatingActionButton
import com.app.phonebook.presentation.view.MyTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lazily accessed property that provides an instance of `Config`.
 *
 * Note: It's important to use `applicationContext` when instantiating singletons or similar objects to
 * prevent memory leaks associated with shorter-lived context objects.
 */
val Context.config: Config get() = Config.newInstance(applicationContext)

/**
 * Retrieves a list of available SIM accounts on the device.
 *
 * This function queries the `TelecomManager` for call-capable phone accounts and constructs a list
 * of `SIMAccount` objects, each representing a SIM account. Each `SIMAccount` includes details such
 * as the account index, account handle, label, and address. The label is augmented with the SIM account's
 * address (phone number) if available, providing a more descriptive identifier for the account.
 *
 * @suppress Uses `@SuppressLint("MissingPermission")` to acknowledge that this function may be called
 * in a context where the necessary permissions (e.g., `READ_PHONE_STATE`) have been checked at a higher level
 * or are being managed through a different mechanism.
 *
 * @return A list of `SIMAccount` objects representing the call-capable SIM accounts found on the device.
 * If an error occurs during retrieval, the function logs the error and returns an empty list.
 *
 * Note: This function requires appropriate permissions (e.g., `READ_PHONE_STATE`) to access phone account
 * information. The caller is responsible for ensuring that these permissions are granted before invoking
 * this function. The use of `@SuppressLint("MissingPermission")` indicates that permission handling is
 * being addressed elsewhere.
 *
 * This function is particularly useful for applications that need to display or manage SIM account
 * information, providing a straightforward way to access this data.
 */
@SuppressLint("MissingPermission")
fun Context.getAvailableSIMCardLabels(): List<SIMAccount> {
    val simAccounts = mutableListOf<SIMAccount>()
    try {
        telecomManager.callCapablePhoneAccounts.forEachIndexed { index, account ->
            val phoneAccount = telecomManager.getPhoneAccount(account)
            var label = phoneAccount.label.toString()
            var address = phoneAccount.address.toString()
            if (address.startsWith("tel:") && address.substringAfter("tel:").isNotEmpty()) {
                address = Uri.decode(address.substringAfter("tel:"))
                label += " ($address)"
            }

            val sim = SIMAccount(
                index + 1, phoneAccount.accountHandle, label, address.substringAfter("tel:")
            )
            simAccounts.add(sim)
        }
    } catch (ignored: Exception) {
        Log.e(APP_NAME, "getAvailableSIMCardLabels: ${ignored.message}")
    }
    return simAccounts
}

fun Context.getContactsHasMap(
    withComparableNumbers: Boolean = false, callback: (HashMap<String, String>) -> Unit
) {
    ContactsHelper(this).getContacts(showOnlyContactsWithNumbers = true) { contactList ->
        val privateContacts: HashMap<String, String> = HashMap()
        for (contact in contactList) {
            for (phoneNumber in contact.phoneNumbers) {
                var number = PhoneNumberUtils.stripSeparators(phoneNumber.value)
                if (withComparableNumbers) {
                    number = number.trimToComparableNumber()
                }

                privateContacts[number] = contact.name
            }
        }
        callback(privateContacts)
    }
}

/**
 * Retrieves all contact sources available on the device, including both device and private contact sources.
 *
 * This function aggregates contact sources from the device and adds a private contact source specific to
 * the application or context in use. It leverages the `ContactsHelper` class to fetch existing contact
 * sources from the device's storage and then appends a private source defined by `getPrivateContactSource()`.
 * The combination of these sources provides a comprehensive list of contact origins that an application
 * can interact with or display to the user.
 *
 * @return An `ArrayList` of `ContactSource` objects representing all available contact sources on the device.
 * This list includes both system-wide contacts and those defined privately within the application.
 *
 * This utility function is particularly useful for applications that need to display a list of contacts
 * or contact sources to the user, allowing for a more nuanced understanding of where each contact originates.
 */
fun Context.getAllContactSources(): ArrayList<ContactSource> {
    val sources = ContactsHelper(this).getDeviceContactSources()
    sources.add(getPrivateContactSource())
    return sources.toMutableList() as ArrayList<ContactSource>
}

/**
 * Retrieves the names of all contact sources that are not ignored by the user's configuration settings.
 *
 * This function first gathers all available contact sources on the device by calling `getAllContactSources()`.
 * It then filters out any sources that have been marked as ignored in the user's configuration settings,
 * accessible via `baseConfig.ignoredContactSources`. The result is a list of contact source names that are
 * considered "visible" or active, according to the user's preferences.
 *
 * The visibility of contact sources can be configured by the user, allowing them to hide certain sources
 * from the application's view. This function respects those settings by excluding any sources the user has
 * chosen to ignore.
 *
 * This function is useful for applications that manage contacts or contact information, enabling them to
 * tailor the user interface and functionality to respect the user's preferences regarding which contact
 * sources should be displayed or interacted with.
 */
fun Context.getVisibleContactSources(): ArrayList<String> {
    val sources = getAllContactSources()
    val ignoredContactSources = baseConfig.ignoredContactSources
    return ArrayList(sources).filter { !ignoredContactSources.contains(it.getFullIdentifier()) }
        .map { it.name }.toMutableList() as ArrayList<String>
}

/**
 * Creates and returns a private contact source.
 *
 * This function generates a `ContactSource` object representing a private contact source, typically
 * used for contacts that are stored within the application's own storage space, rather than synced
 * with or imported from external sources. This private source is distinguished by using `SMT_PRIVATE`
 * as both the identifier and type, with a user-friendly name sourced from the application's string
 * resources (`R.string.phone_storage_hidden`), indicating that these contacts are hidden from other
 * applications and are managed privately by the app.
 *
 * The `SMT_PRIVATE` constant should be defined within your application to uniquely identify this
 * private contact source, ensuring it does not conflict with other contact sources on the device.
 *
 * @return A `ContactSource` object configured as a private contact source, with its name set to
 *         indicate it represents contacts stored privately by the application.
 *
 * This function is particularly useful for applications that manage a set of contacts independently
 * of the device's central contacts storage, allowing for separation between publicly shared contacts
 * and those meant to remain private within the app.
 */
fun Context.getPrivateContactSource() = ContactSource(
    SMT_PRIVATE,
    SMT_PRIVATE,
    getString(R.string.phone_storage_hidden)
)

fun Context.getMyContentProviderCursorLoader() =
    CursorLoader(this, MyContentProvider.MY_CONTENT_URI, null, null, null, null)


fun Context.getColoredMaterialStatusBarColor(): Int {
    return if (baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.you_status_bar_color, theme)
    } else {
        getProperPrimaryColor()
    }
}

fun Context.getTextSize() = when (baseConfig.fontSize) {
    FONT_SIZE_SMALL -> resources.getDimension(R.dimen.smaller_text_size)
    FONT_SIZE_MEDIUM -> resources.getDimension(R.dimen.bigger_text_size)
    FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
    else -> resources.getDimension(R.dimen.extra_big_text_size)
}

fun Context.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.app_name), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
    val toastText = String.format(getString(R.string.value_copied_to_clipboard_show), text)
    toast(toastText)
}

fun Context.getPhoneNumberTypeText(type: Int, label: String): String {
    return if (type == ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM) {
        label
    } else {
        getString(
            when (type) {
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> R.string.mobile
                ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> R.string.home
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> R.string.work
                ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> R.string.main_number
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK -> R.string.work_fax
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME -> R.string.home_fax
                ContactsContract.CommonDataKinds.Phone.TYPE_PAGER -> R.string.pager
                else -> R.string.other
            }
        )
    }
}


val Context.telecomManager: TelecomManager get() = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
val Context.telephonyManager: TelephonyManager get() = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

fun Context.getPopupMenuTheme(): Int {
    return if (isSPlus() && baseConfig.isUsingSystemTheme) {
        R.style.AppTheme_YouPopupMenuStyle
    } else if (isWhiteTheme()) {
        R.style.AppTheme_PopupMenuLightStyle
    } else {
        R.style.AppTheme_PopupMenuDarkStyle
    }
}

@SuppressLint("MissingPermission")
fun Context.areMultipleSIMsAvailable(): Boolean {
    return try {
        if (hasPermission(PERMISSION_READ_PHONE_STATE)) {
            telecomManager.callCapablePhoneAccounts.size > 1
        } else {
            false
        }
    } catch (ignored: Exception) {
        false
    }
}


fun Context.launchActivityIntent(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        toast(R.string.no_app_found)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}


fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(getString(id), length)
}

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (e: Exception) {
        Log.e(APP_NAME, "toast: ${e.message}")
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}

fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format(getString(R.string.error), msg), length)
}

fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}


val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)

@SuppressLint("NewApi")
fun Context.getBottomNavigationBackgroundColor(context: Context): Int {
    val baseColor = baseConfig.backgroundColor
    val bottomColor = when {
        baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_status_bar_color, theme)
        baseColor == Color.WHITE -> resources.getColor(
            R.color.bottom_tabs_light_background, context.theme
        )

        else -> baseConfig.backgroundColor.lightenColor(4)
    }
    return bottomColor
}


fun Context.updateTextColors(viewGroup: ViewGroup) {
    val textColor = when {
        baseConfig.isUsingSystemTheme -> getProperTextColor()
        else -> baseConfig.textColor
    }

    val backgroundColor = baseConfig.backgroundColor
    val accentColor = when {
        isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
        else -> getProperPrimaryColor()
    }

    val cnt = viewGroup.childCount
    (0 until cnt).map { viewGroup.getChildAt(it) }.forEach {
        when (it) {
            is MyTextView -> it.setColors(textColor = textColor, accentColor = accentColor)
//            is MyAppCompatSpinner -> it.setColors(textColor, accentColor, backgroundColor)
//            is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
//            is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
            is MyEditText -> it.setColors(textColor = textColor, accentColor = accentColor)
//            is MyAutoCompleteTextView -> it.setColors(textColor, accentColor, backgroundColor)
            is MyFloatingActionButton -> it.setColors(accentColor = accentColor)
//            is MySeekBar -> it.setColors(textColor, accentColor, backgroundColor)
            is MyButton -> it.setColors(textColor = textColor)
//            is MyTextInputLayout -> it.setColors(textColor, accentColor, backgroundColor)
            is ViewGroup -> updateTextColors(it)
        }
    }
}


fun Context.getAppIconColors() =
    resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())


fun Context.getTimeFormat() = if (baseConfig.use24HourFormat) TIME_FORMAT_24 else TIME_FORMAT_12


fun Context.getProperBackgroundColor() = if (baseConfig.isUsingSystemTheme) {
    resources.getColor(R.color.you_background_color, theme)
} else {
    baseConfig.backgroundColor
}

/**
 * Updates the colors of a bottom tab item based on its active state.
 *
 * This extension function for the `Context` class allows for the dynamic updating of the colors and optional drawable
 * of a bottom tab item within a navigation bar. It modifies the color of the tab item icon and label to indicate
 * its active or inactive state. Additionally, if a drawable resource ID is provided, it updates the tab item icon
 * with the specified drawable.
 *
 * The color change is determined by whether the tab item is active or not:
 * - For active tab items, it uses the primary color of the app's theme, fetched via `getProperPrimaryColor`.
 * - For inactive tab items, it uses a text color appropriate for less emphasis, fetched via `getProperTextColor`.
 *
 * @param view The view containing the tab item's icon and label. If null, no operation is performed.
 * @param isActive Indicates whether the tab item is currently active. This affects the color used for the icon and label.
 * @param drawableId Optional. The resource ID of a drawable to set as the icon of the tab item. If null, the icon's drawable is not updated.
 *
 * Usage of this function enhances the visual feedback in navigation bars by clearly distinguishing between active
 * and inactive states, contributing to a more intuitive user interface.
 */
fun Context.updateBottomTabItemColors(view: View?, isActive: Boolean, drawableId: Int? = null) {
    val color = if (isActive) {
        getProperPrimaryColor()
    } else {
        getProperTextColor()
    }

    if (drawableId != null) {
        val drawable = ResourcesCompat.getDrawable(resources, drawableId, theme)
        view?.findViewById<ImageView>(R.id.tab_item_icon)?.setImageDrawable(drawable)
    }

    view?.findViewById<ImageView>(R.id.tab_item_icon)?.applyColorFilter(color)
    view?.findViewById<TextView>(R.id.tab_item_label)?.setTextColor(color)
}


fun Context.getProperTextColor() = if (baseConfig.isUsingSystemTheme) {
    resources.getColor(R.color.you_neutral_text_color, theme)
} else {
    baseConfig.textColor
}

fun Context.getProperPrimaryColor() = when {
    baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_primary_color, theme)
    isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
    else -> baseConfig.primaryColor
}

fun Context.isBlackAndWhiteTheme() =
    baseConfig.textColor == Color.WHITE && baseConfig.primaryColor == Color.BLACK && baseConfig.backgroundColor == Color.BLACK

fun Context.isWhiteTheme() =
    baseConfig.textColor == DARK_GREY && baseConfig.primaryColor == Color.WHITE && baseConfig.backgroundColor == Color.WHITE

fun Context.getProperStatusBarColor() = when {
    baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_status_bar_color, theme)
    else -> getProperBackgroundColor()
}

val Context.areSystemAnimationsEnabled: Boolean
    get() = Settings.Global.getFloat(
        contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f
    ) > 0f

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    showErrors: Boolean = false,
    callback: (cursor: Cursor) -> Unit
) {
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    callback(cursor)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        if (showErrors) {
            showErrorToast(e)
        }
    }
}


fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(
    this, getPermissionString(permId)
) == PackageManager.PERMISSION_GRANTED

//fun Context.isDefaultDialer(): Boolean {
//    return if (!packageName.startsWith("com.simplemobiletools.contacts") && !packageName.startsWith(
//            "com.simplemobiletools.dialer"
//        )
//    ) {
//        true
//    } else if ((packageName.startsWith("com.simplemobiletools.contacts") || packageName.startsWith("com.simplemobiletools.dialer")) && isQPlus()) {
//        val roleManager = getSystemService(RoleManager::class.java)
//        roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
//    } else {
//        telecomManager.defaultDialerPackage == packageName
//    }
//}

fun Context.getCurrentFormattedDateTime(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
    return simpleDateFormat.format(Date(System.currentTimeMillis()))
}

val Context.shortcutManager: ShortcutManager get() = getSystemService(ShortcutManager::class.java) as ShortcutManager

fun Context.getMyContactsCursor(favoritesOnly: Boolean, withPhoneNumbersOnly: Boolean) = try {
    val getFavoritesOnly = if (favoritesOnly) "1" else "0"
    val getWithPhoneNumbersOnly = if (withPhoneNumbersOnly) "1" else "0"
    val args = arrayOf(getFavoritesOnly, getWithPhoneNumbersOnly)
    CursorLoader(
        this, MyContactsContentProvider.CONTACTS_CONTENT_URI, null, null, args, null
    ).loadInBackground()
} catch (e: Exception) {
    Log.e(APP_NAME, "getMyContactsCursor: ${e.message}")
    null
}


fun Context.getPermissionString(id: Int) = when (id) {
    PERMISSION_READ_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
    PERMISSION_WRITE_STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
    PERMISSION_CAMERA -> Manifest.permission.CAMERA
    PERMISSION_RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
    PERMISSION_READ_CONTACTS -> Manifest.permission.READ_CONTACTS
    PERMISSION_WRITE_CONTACTS -> Manifest.permission.WRITE_CONTACTS
    PERMISSION_READ_CALENDAR -> Manifest.permission.READ_CALENDAR
    PERMISSION_WRITE_CALENDAR -> Manifest.permission.WRITE_CALENDAR
    PERMISSION_CALL_PHONE -> Manifest.permission.CALL_PHONE
    PERMISSION_READ_CALL_LOG -> Manifest.permission.READ_CALL_LOG
    PERMISSION_WRITE_CALL_LOG -> Manifest.permission.WRITE_CALL_LOG
    PERMISSION_GET_ACCOUNTS -> Manifest.permission.GET_ACCOUNTS
    PERMISSION_READ_SMS -> Manifest.permission.READ_SMS
    PERMISSION_SEND_SMS -> Manifest.permission.SEND_SMS
    PERMISSION_READ_PHONE_STATE -> Manifest.permission.READ_PHONE_STATE
    PERMISSION_MEDIA_LOCATION -> if (isQPlus()) Manifest.permission.ACCESS_MEDIA_LOCATION else ""
    PERMISSION_POST_NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
    PERMISSION_READ_MEDIA_IMAGES -> Manifest.permission.READ_MEDIA_IMAGES
    PERMISSION_READ_MEDIA_VIDEO -> Manifest.permission.READ_MEDIA_VIDEO
    PERMISSION_READ_MEDIA_AUDIO -> Manifest.permission.READ_MEDIA_AUDIO
    PERMISSION_ACCESS_COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
    PERMISSION_ACCESS_FINE_LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
    PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED -> Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    PERMISSION_READ_SYNC_SETTINGS -> Manifest.permission.READ_SYNC_SETTINGS
    else -> ""
}