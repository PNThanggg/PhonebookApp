package com.app.phonebook.base.extension

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.loader.content.CursorLoader
import com.app.phonebook.R
import com.app.phonebook.base.helpers.BaseConfig
import com.app.phonebook.base.helpers.ExternalStorageProviderHack
import com.app.phonebook.base.helpers.MyContentProvider
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.DARK_GREY
import com.app.phonebook.base.utils.DEFAULT_FILE_NAME
import com.app.phonebook.base.utils.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.app.phonebook.base.utils.FONT_SIZE_LARGE
import com.app.phonebook.base.utils.FONT_SIZE_MEDIUM
import com.app.phonebook.base.utils.FONT_SIZE_SMALL
import com.app.phonebook.base.utils.KEY_MAILTO
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
import com.app.phonebook.base.utils.SD_OTG_PATTERN
import com.app.phonebook.base.utils.SD_OTG_SHORT
import com.app.phonebook.base.utils.SIGNAL_PACKAGE
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.utils.TELEGRAM_PACKAGE
import com.app.phonebook.base.utils.TIME_FORMAT_12
import com.app.phonebook.base.utils.TIME_FORMAT_24
import com.app.phonebook.base.utils.VIBER_PACKAGE
import com.app.phonebook.base.utils.WHATSAPP_PACKAGE
import com.app.phonebook.base.utils.isOnMainThread
import com.app.phonebook.base.utils.isQPlus
import com.app.phonebook.base.utils.isRPlus
import com.app.phonebook.base.utils.isSPlus
import com.app.phonebook.base.utils.isTiramisuPlus
import com.app.phonebook.base.utils.isUpsideDownCakePlus
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.ContactSource
import com.app.phonebook.data.models.FileDirItem
import com.app.phonebook.data.models.SIMAccount
import com.app.phonebook.helpers.Config
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.LocalContactsHelper
import com.app.phonebook.helpers.MyContactsContentProvider
import com.app.phonebook.helpers.SimpleContactsHelper
import com.app.phonebook.presentation.view.MyAppCompatCheckbox
import com.app.phonebook.presentation.view.MyButton
import com.app.phonebook.presentation.view.MyCompatRadioButton
import com.app.phonebook.presentation.view.MyEditText
import com.app.phonebook.presentation.view.MyFloatingActionButton
import com.app.phonebook.presentation.view.MyTextInputLayout
import com.app.phonebook.presentation.view.MyTextView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

private const val ANDROID_DATA_DIR = "/Android/data/"
private const val ANDROID_OBB_DIR = "/Android/obb/"
val DIRS_ACCESSIBLE_ONLY_WITH_SAF = listOf(ANDROID_DATA_DIR, ANDROID_OBB_DIR)

private const val DOWNLOAD_DIR = "Download"
private const val ANDROID_DIR = "Android"
private val DIRS_INACCESSIBLE_WITH_SAF_SDK_30 = listOf(DOWNLOAD_DIR, ANDROID_DIR)
val Context.recycleBinPath: String get() = filesDir.absolutePath

val Context.internalStoragePath: String get() = baseConfig.internalStoragePath

fun Context.getStorageDirectories(): Array<String> {
    val paths = HashSet<String>()
    val rawExternalStorage = System.getenv("EXTERNAL_STORAGE")
    val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
    val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
    if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
        getExternalFilesDirs(null).filterNotNull().map { it.absolutePath }
            .mapTo(paths) { it.substring(0, it.indexOf("Android/data")) }
    } else {
        val path = Environment.getExternalStorageDirectory().absolutePath
        val folders = Pattern.compile("/").split(path)
        val lastFolder = folders[folders.size - 1]
        var isDigit = false
        try {
            Integer.valueOf(lastFolder)
            isDigit = true
        } catch (ignored: NumberFormatException) {
        }

        val rawUserId = if (isDigit) lastFolder else ""

        if (rawEmulatedStorageTarget != null) {
            if (TextUtils.isEmpty(rawUserId)) {
                paths.add(rawEmulatedStorageTarget)
            } else {
                paths.add(rawEmulatedStorageTarget + File.separator + rawUserId)
            }
        }

    }

    if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
        val rawSecondaryStorages =
            rawSecondaryStoragesStr!!.split(File.pathSeparator.toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        Collections.addAll(paths, *rawSecondaryStorages)
    }
    return paths.map { it.trimEnd('/') }.toTypedArray()
}

fun Context.getInternalStoragePath() =
    if (File("/storage/emulated/0").exists()) "/storage/emulated/0" else Environment.getExternalStorageDirectory().absolutePath.trimEnd(
        '/'
    )

// avoid these being set as SD card paths
private val physicalPaths = arrayListOf(
    "/storage/sdcard1", // Motorola Xoom
    "/storage/extsdcard", // Samsung SGS3
    "/storage/sdcard0/external_sdcard", // User request
    "/mnt/extsdcard", "/mnt/sdcard/external_sd", // Samsung galaxy family
    "/mnt/external_sd", "/mnt/media_rw/sdcard1", // 4.4.2 on CyanogenMod S3
    "/removable/microsd", // Asus transformer prime
    "/mnt/emmc", "/storage/external_SD", // LG
    "/storage/ext_sd", // HTC One Max
    "/storage/removable/sdcard1", // Sony Xperia Z1
    "/data/sdext", "/data/sdext2", "/data/sdext3", "/data/sdext4", "/sdcard1", // Sony Xperia Z
    "/sdcard2", // HTC One M8s
    "/storage/usbdisk0",
    "/storage/usbdisk1",
    "/storage/usbdisk2"
)

fun Context.getSDCardPath(): String {
    val directories = getStorageDirectories().filter {
        it != getInternalStoragePath() && !it.equals(
            "/storage/emulated/0",
            true
        ) && (baseConfig.OTGPartition.isEmpty() || !it.endsWith(baseConfig.OTGPartition))
    }

    val fullSDPattern = Pattern.compile(SD_OTG_PATTERN)
    var sdCardPath = directories.firstOrNull { fullSDPattern.matcher(it).matches() }
        ?: directories.firstOrNull { !physicalPaths.contains(it.lowercase(Locale.ROOT)) } ?: ""

    // on some devices no method retrieved any SD card path, so test if its not sdcard1 by any chance. It happened on an Android 5.1
    if (sdCardPath.trimEnd('/').isEmpty()) {
        val file = File("/storage/sdcard1")
        if (file.exists()) {
            return file.absolutePath
        }

        sdCardPath = directories.firstOrNull() ?: ""
    }

    if (sdCardPath.isEmpty()) {
        val sdPattern = Pattern.compile(SD_OTG_SHORT)
        try {
            File("/storage").listFiles()?.forEach {
                if (sdPattern.matcher(it.name).matches()) {
                    sdCardPath = "/storage/${it.name}"
                }
            }
        } catch (_: Exception) {
        }
    }

    val finalPath = sdCardPath.trimEnd('/')
    baseConfig.sdCardPath = finalPath
    return finalPath
}

val Context.sdCardPath: String get() = baseConfig.sdCardPath

fun Context.hasExternalSDCard() = sdCardPath.isNotEmpty()

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
fun Context.getAvailableSIMCardLabels(): List<SIMAccount> {
    val simAccounts = mutableListOf<SIMAccount>()
    try {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return simAccounts
        }

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
    return ArrayList(sources).filter { !ignoredContactSources.contains(it.getFullIdentifier()) }.map { it.name }
        .toMutableList() as ArrayList<String>
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
    SMT_PRIVATE, SMT_PRIVATE, getString(R.string.phone_storage_hidden)
)

fun Context.getMyContentProviderCursorLoader() = CursorLoader(this, MyContentProvider.MY_CONTENT_URI, null, null, null, null)


fun Context.getColoredMaterialStatusBarColor(): Int {
    return if (baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.status_bar_color, theme)
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

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

val Context.telecomManager: TelecomManager get() = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
val Context.telephonyManager: TelephonyManager get() = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

@Suppress("DEPRECATION")
val Context.usableScreenSize: Point
    get() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return size
    }

@Suppress("DEPRECATION")
val Context.realScreenSize: Point
    get() {
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)
        return size
    }

val Context.newNavigationBarHeight: Int
    @SuppressLint("DiscouragedApi", "InternalInsetResource") get() {
        var navigationBarHeight = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return navigationBarHeight
    }

val Context.statusBarHeight: Int
    @SuppressLint("DiscouragedApi", "InternalInsetResource") get() {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

val Context.navigationBarSize: Point
    get() = when {
        navigationBarOnSide -> Point(newNavigationBarHeight, usableScreenSize.y)
        navigationBarOnBottom -> Point(usableScreenSize.x, newNavigationBarHeight)
        else -> Point()
    }

val Context.navigationBarOnSide: Boolean get() = usableScreenSize.x < realScreenSize.x && usableScreenSize.x > usableScreenSize.y

val Context.navigationBarOnBottom: Boolean get() = usableScreenSize.y < realScreenSize.y

val Context.navigationBarHeight: Int get() = if (navigationBarOnBottom && navigationBarSize.y != usableScreenSize.y) navigationBarSize.y else 0

@SuppressLint("DiscouragedApi")
fun Context.isUsingGestureNavigation(): Boolean {
    return try {
        val resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
        if (resourceId > 0) {
            resources.getInteger(resourceId) == 2
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

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

val Context.powerManager: PowerManager get() = getSystemService(Context.POWER_SERVICE) as PowerManager

val Context.audioManager: AudioManager get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager

val Context.isRTLLayout: Boolean get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

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

    val accentColor = when {
        isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
        else -> getProperPrimaryColor()
    }

    val cnt = viewGroup.childCount
    (0 until cnt).map { viewGroup.getChildAt(it) }.forEach {
        when (it) {
            is MyTextView -> it.setColors(textColor = textColor, accentColor = accentColor)
            is MyCompatRadioButton -> it.setColors(textColor, accentColor)
            is MyAppCompatCheckbox -> it.setColors(textColor, accentColor)
            is MyEditText -> it.setColors(textColor = textColor, accentColor = accentColor)
            is MyFloatingActionButton -> it.setColors(accentColor = accentColor)
            is MyButton -> it.setColors(textColor = textColor)
            is MyTextInputLayout -> it.setColors(textColor, accentColor)
            is ViewGroup -> updateTextColors(it)
        }
    }
}

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
    resources.getColor(R.color.neutral_text_color, theme)
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

fun Context.isDefaultDialer(): Boolean {
    return telecomManager.defaultDialerPackage == packageName
}

fun Context.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_SETTINGS)
    startActivity(intent)

//    if (isOreoPlus()) {
//        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
//        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
//        startActivity(intent)
//    } else {
//        // For Android versions below Oreo, you can't directly open the app's notification settings.
//        // You can open the general notification settings instead.
//        val intent = Intent(Settings.ACTION_SETTINGS)
//        startActivity(intent)
//    }
}

fun getCurrentFormattedDateTime(): String {
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

@SuppressLint("UseCompatLoadingForDrawables")
fun Context.getPackageDrawable(packageName: String): Drawable {
    return resources.getDrawable(
        when (packageName) {
            TELEGRAM_PACKAGE -> R.drawable.ic_telegram_rect_vector
            SIGNAL_PACKAGE -> R.drawable.ic_signal_rect_vector
            WHATSAPP_PACKAGE -> R.drawable.ic_whatsapp_rect_vector
            VIBER_PACKAGE -> R.drawable.ic_viber_rect_vector
            else -> R.drawable.ic_threema_rect_vector
        }, theme
    )
}

fun Context.sendSMSToContacts(contacts: ArrayList<Contact>) {
    val numbers = StringBuilder()
    contacts.forEach {
        val number = it.phoneNumbers.firstOrNull { it.type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE }
            ?: it.phoneNumbers.firstOrNull()

        if (number != null) {
            numbers.append("${Uri.encode(number.value)};")
        }
    }

    val uriString = "smsto:${numbers.toString().trimEnd(';')}"
    Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)).apply {
        launchActivityIntent(this)
    }
}

fun Context.sendEmailToContacts(contacts: ArrayList<Contact>) {
    val emails = ArrayList<String>()
    contacts.forEach {
        it.emails.forEach {
            if (it.value.isNotEmpty()) {
                emails.add(it.value)
            }
        }
    }

    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
        launchActivityIntent(this)
    }
}

fun Context.addContactsToGroup(contacts: ArrayList<Contact>, groupId: Long) {
    val publicContacts = contacts.filter { !it.isPrivate() }.toMutableList() as ArrayList<Contact>
    val privateContacts = contacts.filter { it.isPrivate() }.toMutableList() as ArrayList<Contact>
    if (publicContacts.isNotEmpty()) {
        ContactsHelper(this).addContactsToGroup(publicContacts, groupId)
    }

    if (privateContacts.isNotEmpty()) {
        LocalContactsHelper(this).addContactsToGroup(privateContacts, groupId)
    }
}

fun Context.sendEmailIntent(recipient: String) {
    Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.fromParts(KEY_MAILTO, recipient, null)
        launchActivityIntent(this)
    }
}

fun isEncodedContactUri(uri: Uri?): Boolean {
    if (uri == null) {
        return false
    }
    val lastPathSegment = uri.lastPathSegment ?: return false
    return lastPathSegment == "encoded"
}


fun getLookupKeyFromUri(lookupUri: Uri): String? {
    return if (!isEncodedContactUri(lookupUri)) {
        val segments = lookupUri.pathSegments
        if (segments.size < 3) null else Uri.encode(segments[2])
    } else {
        null
    }
}

fun lookupContactUri(lookup: String, context: Context): Uri? {
    val lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookup)
    return try {
        ContactsContract.Contacts.lookupContact(context.contentResolver, lookupUri)
    } catch (e: Exception) {
        null
    }
}

fun Context.getLookupUriRawId(dataUri: Uri): Int {
    val lookupKey = getLookupKeyFromUri(dataUri)
    if (lookupKey != null) {
        val uri = lookupContactUri(lookupKey, this)
        if (uri != null) {
            return getContactUriRawId(uri)
        }
    }
    return -1
}

fun Context.getContactPublicUri(contact: Contact): Uri {
    val lookupKey = if (contact.isPrivate()) {
        "local_${contact.id}"
    } else {
        SimpleContactsHelper(this).getContactLookupKey(contact.id.toString())
    }
    return Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
}

fun Context.getPublicContactSource(source: String, callback: (String) -> Unit) {
    when (source) {
        SMT_PRIVATE -> callback(getString(R.string.phone_storage_hidden))
        else -> {
            ContactsHelper(this).getContactSources {
                var newSource = source
                for (contactSource in it) {
                    if (contactSource.name == source && contactSource.type == TELEGRAM_PACKAGE) {
                        newSource = getString(R.string.telegram)
                        break
                    } else if (contactSource.name == source && contactSource.type == VIBER_PACKAGE) {
                        newSource = getString(R.string.viber)
                        break
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    callback(newSource)
                }
            }
        }
    }
}


fun Context.getContactUriRawId(uri: Uri): Int {
    val projection = arrayOf(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor!!.moveToFirst()) {
            return cursor.getIntValue(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
        }
    } catch (ignored: Exception) {
    } finally {
        cursor?.close()
    }
    return -1
}

fun Context.getSAFOnlyDirs(): List<String> {
    return DIRS_ACCESSIBLE_ONLY_WITH_SAF.map { "$internalStoragePath$it" } +
            DIRS_ACCESSIBLE_ONLY_WITH_SAF.map { "$sdCardPath$it" }
}

fun Context.isSAFOnlyRoot(path: String): Boolean {
    return getSAFOnlyDirs().any { "${path.trimEnd('/')}/".startsWith(it) }
}

fun Context.isRestrictedSAFOnlyRoot(path: String): Boolean {
    return isRPlus() && isSAFOnlyRoot(path)
}

val Context.otgPath: String get() = baseConfig.OTGPath

fun Context.isPathOnSD(path: String) = sdCardPath.isNotEmpty() && path.startsWith(sdCardPath)

fun Context.isPathOnOTG(path: String) = otgPath.isNotEmpty() && path.startsWith(otgPath)

fun Context.getAndroidTreeUri(path: String): String {
    return when {
        isPathOnOTG(path) -> if (isAndroidDataDir(path)) baseConfig.otgAndroidDataTreeUri else baseConfig.otgAndroidObbTreeUri
        isPathOnSD(path) -> if (isAndroidDataDir(path)) baseConfig.sdAndroidDataTreeUri else baseConfig.sdAndroidObbTreeUri
        else -> if (isAndroidDataDir(path)) baseConfig.primaryAndroidDataTreeUri else baseConfig.primaryAndroidObbTreeUri
    }
}

fun Context.getFileSize(treeUri: Uri, documentId: String): Long {
    val projection = arrayOf(DocumentsContract.Document.COLUMN_SIZE)
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    return contentResolver.query(documentUri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getLongValue(DocumentsContract.Document.COLUMN_SIZE)
        } else {
            0L
        }
    } ?: 0L
}

fun Context.getAndroidSAFFileSize(path: String): Long {
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    return getFileSize(treeUri, documentId)
}

fun Context.getAndroidSAFDirectChildrenCount(path: String, countHidden: Boolean): Int {
    val treeUri = getAndroidTreeUri(path).toUri()
    if (treeUri == Uri.EMPTY) {
        return 0
    }

    val documentId = createAndroidSAFDocumentId(path)
    val rootDocId = getStorageRootIdForAndroidDir(path)
    return getDirectChildrenCount(rootDocId, treeUri, documentId, countHidden)
}

fun Context.getDirectChildrenCount(rootDocId: String, treeUri: Uri, documentId: String, shouldShowHidden: Boolean): Int {
    return try {
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        val rawCursor = contentResolver.query(childrenUri, projection, null, null, null)!!
        val cursor = ExternalStorageProviderHack.transformQueryResult(rootDocId, childrenUri, rawCursor)
        if (shouldShowHidden) {
            cursor.count
        } else {
            var count = 0
            cursor.use {
                while (cursor.moveToNext()) {
                    val docId = cursor.getStringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    if (docId != null) {
                        if (!docId.getFilenameFromPath().startsWith('.') || shouldShowHidden) {
                            count++
                        }
                    }
                }
            }
            count
        }
    } catch (e: Exception) {
        0
    }
}

fun Context.getTempFile(filename: String = DEFAULT_FILE_NAME): File? {
    val folder = File(cacheDir, "contacts")
    if (!folder.exists()) {
        if (!folder.mkdir()) {
            toast(R.string.unknown_error_occurred)
            return null
        }
    }

    return File(folder, filename)
}

fun isAndroidDataDir(path: String): Boolean {
    val resolvedPath = "${path.trimEnd('/')}/"
    return resolvedPath.contains(ANDROID_DATA_DIR)
}

fun Context.getStorageRootIdForAndroidDir(path: String) =
    getAndroidTreeUri(path).removeSuffix(if (isAndroidDataDir(path)) "%3AAndroid%2Fdata" else "%3AAndroid%2Fobb")
        .substringAfterLast('/').trimEnd('/')


fun Context.createAndroidSAFDocumentId(path: String): String {
    val basePath = path.getBasePath(this)
    val relativePath = path.substring(basePath.length).trim('/')
    val storageId = getStorageRootIdForAndroidDir(path)
    return "$storageId:$relativePath"
}


fun Context.getAndroidSAFUri(path: String): Uri {
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}


fun Context.getDocumentFile(path: String): DocumentFile? {
    val isOTG = isPathOnOTG(path)
    var relativePath = path.substring(if (isOTG) otgPath.length else sdCardPath.length)
    if (relativePath.startsWith(File.separator)) {
        relativePath = relativePath.substring(1)
    }

    return try {
        val treeUri = Uri.parse(if (isOTG) baseConfig.OTGTreeUri else baseConfig.sdTreeUri)
        var document = DocumentFile.fromTreeUri(applicationContext, treeUri)
        val parts = relativePath.split("/").filter { it.isNotEmpty() }
        for (part in parts) {
            document = document?.findFile(part)
        }
        document
    } catch (ignored: Exception) {
        null
    }
}

fun Context.removeContactsFromGroup(contacts: ArrayList<Contact>, groupId: Long) {
    val publicContacts = contacts.filter { !it.isPrivate() }.toMutableList() as ArrayList<Contact>
    val privateContacts = contacts.filter { it.isPrivate() }.toMutableList() as ArrayList<Contact>
    if (publicContacts.isNotEmpty() && hasContactPermissions()) {
        ContactsHelper(this).removeContactsFromGroup(publicContacts, groupId)
    }

    if (privateContacts.isNotEmpty()) {
        LocalContactsHelper(this).removeContactsFromGroup(privateContacts, groupId)
    }
}

fun Context.storeAndroidTreeUri(path: String, treeUri: String) {
    return when {
        isPathOnOTG(path) -> if (isAndroidDataDir(path)) baseConfig.otgAndroidDataTreeUri =
            treeUri else baseConfig.otgAndroidObbTreeUri = treeUri

        isPathOnSD(path) -> if (isAndroidDataDir(path)) baseConfig.sdAndroidDataTreeUri =
            treeUri else baseConfig.sdAndroidObbTreeUri = treeUri

        else -> if (isAndroidDataDir(path)) baseConfig.primaryAndroidDataTreeUri =
            treeUri else baseConfig.primaryAndroidObbTreeUri = treeUri
    }
}

fun Context.hasProperStoredAndroidTreeUri(path: String): Boolean {
    val uri = getAndroidTreeUri(path)
    val hasProperUri = contentResolver.persistedUriPermissions.any { it.uri.toString() == uri }
    if (!hasProperUri) {
        storeAndroidTreeUri(path, "")
    }
    return hasProperUri
}

fun Context.createAndroidDataOrObbPath(fullPath: String): String {
    return if (isAndroidDataDir(fullPath)) {
        fullPath.getBasePath(this).trimEnd('/').plus(ANDROID_DATA_DIR)
    } else {
        fullPath.getBasePath(this).trimEnd('/').plus(ANDROID_OBB_DIR)
    }
}

fun Context.updateOTGPathFromPartition() {
    val otgPath = "/storage/${baseConfig.OTGPartition}"
    baseConfig.OTGPath = if (getOTGFastDocumentFile(otgPath, otgPath)?.exists() == true) {
        "/storage/${baseConfig.OTGPartition}"
    } else {
        "/mnt/media_rw/${baseConfig.OTGPartition}"
    }
}


fun Context.getOTGFastDocumentFile(path: String, otgPathToUse: String? = null): DocumentFile? {
    if (baseConfig.OTGTreeUri.isEmpty()) {
        return null
    }

    val otgPath = otgPathToUse ?: baseConfig.OTGPath
    if (baseConfig.OTGPartition.isEmpty()) {
        baseConfig.OTGPartition = baseConfig.OTGTreeUri.removeSuffix("%3A").substringAfterLast('/').trimEnd('/')
        updateOTGPathFromPartition()
    }

    val relativePath = Uri.encode(path.substring(otgPath.length).trim('/'))
    val fullUri = "${baseConfig.OTGTreeUri}/document/${baseConfig.OTGPartition}%3A$relativePath"
    return DocumentFile.fromSingleUri(this, Uri.parse(fullUri))
}

fun Context.getDoesFilePathExist(path: String, otgPathToUse: String? = null): Boolean {
    val otgPath = otgPathToUse ?: baseConfig.OTGPath
    return when {
        isRestrictedSAFOnlyRoot(path) -> getFastAndroidSAFDocument(path)?.exists() ?: false
        otgPath.isNotEmpty() && path.startsWith(otgPath) -> getOTGFastDocumentFile(path)?.exists() ?: false
        else -> File(path).exists()
    }
}

fun Context.getFastAndroidSAFDocument(path: String): DocumentFile? {
    val treeUri = getAndroidTreeUri(path)
    if (treeUri.isEmpty()) {
        return null
    }

    val uri = getAndroidSAFUri(path)
    return DocumentFile.fromSingleUri(this, uri)
}


fun Context.getSAFStorageId(fullPath: String): String {
    return if (fullPath.startsWith('/')) {
        when {
            fullPath.startsWith(internalStoragePath) -> "primary"
            else -> fullPath.substringAfter("/storage/", "").substringBefore('/')
        }
    } else {
        fullPath.substringBefore(':', "").substringAfterLast('/')
    }
}

fun Context.createDocumentUriFromRootTree(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)

    val relativePath = when {
        fullPath.startsWith(internalStoragePath) -> fullPath.substring(internalStoragePath.length).trim('/')
        else -> fullPath.substringAfter(storageId).trim('/')
    }

    val treeUri = DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, "$storageId:")
    val documentId = "${storageId}:$relativePath"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}


fun Context.createAndroidDataOrObbUri(fullPath: String): Uri {
    val path = createAndroidDataOrObbPath(fullPath)
    return createDocumentUriFromRootTree(path)
}

fun Context.createAndroidSAFDirectory(path: String): Boolean {
    return try {
        val treeUri = getAndroidTreeUri(path).toUri()
        val parentPath = path.getParentPath()
        if (!getDoesFilePathExist(parentPath)) {
            createAndroidSAFDirectory(parentPath)
        }
        val documentId = createAndroidSAFDocumentId(parentPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(
            contentResolver,
            parentUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            path.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {
        showErrorToast(e)
        false
    }
}

fun Context.createAndroidSAFFile(path: String): Boolean {
    return try {
        val treeUri = getAndroidTreeUri(path).toUri()
        val parentPath = path.getParentPath()
        if (!getDoesFilePathExist(parentPath)) {
            createAndroidSAFDirectory(parentPath)
        }

        val documentId = createAndroidSAFDocumentId(path.getParentPath())
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(contentResolver, parentUri, path.getMimeType(), path.getFilenameFromPath()) != null
    } catch (e: IllegalStateException) {
        showErrorToast(e)
        false
    }
}


fun getPermissionString(id: Int) = when (id) {
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
    PERMISSION_POST_NOTIFICATIONS -> if (isTiramisuPlus()) Manifest.permission.POST_NOTIFICATIONS else ""
    PERMISSION_READ_MEDIA_IMAGES -> if (isTiramisuPlus()) Manifest.permission.READ_MEDIA_IMAGES else ""
    PERMISSION_READ_MEDIA_VIDEO -> if (isTiramisuPlus()) Manifest.permission.READ_MEDIA_VIDEO else ""
    PERMISSION_READ_MEDIA_AUDIO -> if (isTiramisuPlus()) Manifest.permission.READ_MEDIA_AUDIO else ""
    PERMISSION_ACCESS_COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
    PERMISSION_ACCESS_FINE_LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
    PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED -> if (isUpsideDownCakePlus()) Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED else ""
    PERMISSION_READ_SYNC_SETTINGS -> Manifest.permission.READ_SYNC_SETTINGS
    else -> ""
}

fun Context.isSDCardSetAsDefaultStorage() =
    sdCardPath.isNotEmpty() && Environment.getExternalStorageDirectory().absolutePath.equals(sdCardPath, true)

fun Context.needsStupidWritePermissions(path: String) =
    (!isRPlus() && isPathOnSD(path) && !isSDCardSetAsDefaultStorage()) || isPathOnOTG(path)

fun Context.hasProperStoredTreeUri(isOTG: Boolean): Boolean {
    val uri = if (isOTG) baseConfig.OTGTreeUri else baseConfig.sdTreeUri
    val hasProperUri = contentResolver.persistedUriPermissions.any { it.uri.toString() == uri }
    if (!hasProperUri) {
        if (isOTG) {
            baseConfig.OTGTreeUri = ""
        } else {
            baseConfig.sdTreeUri = ""
        }
    }
    return hasProperUri
}

fun Context.getHumanReadablePath(path: String): String {
    return getString(
        when (path) {
            "/" -> R.string.root
            internalStoragePath -> R.string.internal
            otgPath -> R.string.usb
            else -> R.string.sd_card
        }
    )
}

fun Context.humanizePath(path: String): String {
    val trimmedPath = path.trimEnd('/')
    val basePath = path.getBasePath(this)
    return when (basePath) {
        "/" -> "${getHumanReadablePath(basePath)}$trimmedPath"
        else -> trimmedPath.replaceFirst(basePath, getHumanReadablePath(basePath))
    }
}

fun Context.showFileCreateError(path: String) {
    val error = String.format(getString(R.string.could_not_create_file), path)
    baseConfig.sdTreeUri = ""
    showErrorToast(error)
}

fun isExternalStorageManager(): Boolean {
    return isRPlus() && Environment.isExternalStorageManager()
}

fun Context.isInAndroidDir(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(this, 0)
    return firstParentDir.equals(ANDROID_DIR, true)
}

fun Context.isInSubFolderInDownloadDir(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(this, 1)
    return if (firstParentDir == null) {
        false
    } else {
        val startsWithDownloadDir = firstParentDir.startsWith(DOWNLOAD_DIR, true)
        val hasAtLeast1PathSegment = firstParentDir.split("/").filter { it.isNotEmpty() }.size > 1
        val firstParentPath = path.getFirstParentPath(this, 1)
        startsWithDownloadDir && hasAtLeast1PathSegment && File(firstParentPath).isDirectory
    }
}

fun Context.getFirstParentLevel(path: String): Int {
    return when {
        isRPlus() && (isInAndroidDir(path) || isInSubFolderInDownloadDir(path)) -> 1
        else -> 0
    }
}


fun Context.isAccessibleWithSAFSdk30(path: String): Boolean {
    if (path.startsWith(recycleBinPath) || isExternalStorageManager()) {
        return false
    }

    val level = getFirstParentLevel(path)
    val firstParentDir = path.getFirstParentDirName(this, level)
    val firstParentPath = path.getFirstParentPath(this, level)

    val isValidName = firstParentDir != null
    val isDirectory = File(firstParentPath).isDirectory
    val isAnAccessibleDirectory = DIRS_INACCESSIBLE_WITH_SAF_SDK_30.all { !firstParentDir.equals(it, true) }
    return isRPlus() && isValidName && isDirectory && isAnAccessibleDirectory
}

fun Context.isRestrictedWithSAFSdk30(path: String): Boolean {
    if (path.startsWith(recycleBinPath) || isExternalStorageManager()) {
        return false
    }

    val level = getFirstParentLevel(path)
    val firstParentDir = path.getFirstParentDirName(this, level)
    val firstParentPath = path.getFirstParentPath(this, level)

    val isInvalidName = firstParentDir == null
    val isDirectory = File(firstParentPath).isDirectory
    val isARestrictedDirectory = DIRS_INACCESSIBLE_WITH_SAF_SDK_30.any { firstParentDir.equals(it, true) }
    return isRPlus() && (isInvalidName || (isDirectory && isARestrictedDirectory))
}

fun getMediaStoreIds(context: Context): HashMap<String, Long> {
    val ids = HashMap<String, Long>()
    val projection = arrayOf(
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media._ID
    )

    val uri = MediaStore.Files.getContentUri("external")

    try {
        context.queryCursor(uri, projection) { cursor ->
            try {
                val id = cursor.getLongValue(MediaStore.Images.Media._ID)
                if (id != 0L) {
                    val path = cursor.getStringValue(MediaStore.Images.Media.DATA)
                    ids[path] = id
                }
            } catch (_: Exception) {
            }
        }
    } catch (_: Exception) {
    }

    return ids
}

fun getFileUri(path: String): Uri = when {
    path.isImageSlow() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    path.isVideoSlow() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    path.isAudioSlow() -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    else -> MediaStore.Files.getContentUri("external")
}

fun Context.getUrisPathsFromFileDirItems(fileDirItems: List<FileDirItem>): Pair<java.util.ArrayList<String>, java.util.ArrayList<Uri>> {
    val fileUris = java.util.ArrayList<Uri>()
    val successfulFilePaths = java.util.ArrayList<String>()
    val allIds = getMediaStoreIds(this)
    val filePaths = fileDirItems.map { it.path }
    filePaths.forEach { path ->
        for ((filePath, mediaStoreId) in allIds) {
            if (filePath.lowercase() == path.lowercase()) {
                val baseUri = getFileUri(filePath)
                val uri = ContentUris.withAppendedId(baseUri, mediaStoreId)
                fileUris.add(uri)
                successfulFilePaths.add(path)
            }
        }
    }

    return Pair(successfulFilePaths, fileUris)
}

fun Context.getFileUrisFromFileDirItems(fileDirItems: List<FileDirItem>): List<Uri> {
    val fileUris = getUrisPathsFromFileDirItems(fileDirItems).second
    if (fileUris.isEmpty()) {
        fileDirItems.map { fileDirItem ->
            fileUris.add(fileDirItem.assembleContentUri())
        }
    }

    return fileUris
}

private fun Context.createCasualFileOutputStream(targetFile: File): OutputStream? {
    if (targetFile.parentFile?.exists() == false) {
        targetFile.parentFile?.mkdirs()
    }

    return try {
        FileOutputStream(targetFile)
    } catch (e: Exception) {
        showErrorToast(e)
        null
    }
}

fun Context.createFirstParentTreeUri(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)
    val level = getFirstParentLevel(fullPath)
    val rootParentDirName = fullPath.getFirstParentDirName(this, level)
    val firstParentId = "$storageId:$rootParentDirName"
    return DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, firstParentId)
}

fun Context.createDocumentUriUsingFirstParentTreeUri(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)
    val relativePath = when {
        fullPath.startsWith(internalStoragePath) -> fullPath.substring(internalStoragePath.length).trim('/')
        else -> fullPath.substringAfter(storageId).trim('/')
    }
    val treeUri = createFirstParentTreeUri(fullPath)
    val documentId = "${storageId}:$relativePath"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

fun Context.createSAFFileSdk30(path: String): Boolean {
    return try {
        val treeUri = createFirstParentTreeUri(path)
        val parentPath = path.getParentPath()
        if (!getDoesFilePathExistSdk30(parentPath)) {
            createSAFDirectorySdk30(parentPath)
        }

        val documentId = getSAFDocumentId(parentPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(contentResolver, parentUri, path.getMimeType(), path.getFilenameFromPath()) != null
    } catch (e: IllegalStateException) {
        showErrorToast(e)
        false
    }
}

fun Context.createSAFDirectorySdk30(path: String): Boolean {
    return try {
        val treeUri = createFirstParentTreeUri(path)
        val parentPath = path.getParentPath()
        if (!getDoesFilePathExistSdk30(parentPath)) {
            createSAFDirectorySdk30(parentPath)
        }

        val documentId = getSAFDocumentId(parentPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(contentResolver, parentUri, DocumentsContract.Document.MIME_TYPE_DIR, path.getFilenameFromPath()) != null
    } catch (e: IllegalStateException) {
        showErrorToast(e)
        false
    }
}

fun Context.getFastDocumentSdk30(path: String): DocumentFile? {
    val uri = createDocumentUriUsingFirstParentTreeUri(path)
    return DocumentFile.fromSingleUri(this, uri)
}

fun Context.getDoesFilePathExistSdk30(path: String): Boolean {
    return when {
        isAccessibleWithSAFSdk30(path) -> getFastDocumentSdk30(path)?.exists() ?: false
        else -> File(path).exists()
    }
}

fun Context.getSAFDocumentId(path: String): String {
    val basePath = path.getBasePath(this)
    val relativePath = path.substring(basePath.length).trim('/')
    val storageId = getSAFStorageId(path)
    return "$storageId:$relativePath"
}

fun Context.getIsPathDirectory(path: String): Boolean {
    return when {
        isRestrictedSAFOnlyRoot(path) -> getFastAndroidSAFDocument(path)?.isDirectory ?: false
        isPathOnOTG(path) -> getOTGFastDocumentFile(path)?.isDirectory ?: false
        else -> File(path).isDirectory
    }
}