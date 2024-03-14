package com.app.phonebook.base.extension

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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
import com.app.phonebook.base.utils.PREFS_KEY
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.utils.TIME_FORMAT_12
import com.app.phonebook.base.utils.TIME_FORMAT_24
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.utils.isOnMainThread
import com.app.phonebook.base.utils.isQPlus
import com.app.phonebook.data.models.ContactSource
import com.app.phonebook.data.models.SIMAccount
import com.app.phonebook.data.models.SharedTheme
import com.app.phonebook.helpers.Config
import com.app.phonebook.helpers.ContactsHelper

val Context.config: Config get() = Config.newInstance(applicationContext)

fun ComponentActivity.handleBackPressed(action: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            action()
        }
    })
}

fun Context.getSharedPrefs(): SharedPreferences =
    getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

fun Context.getSharedTheme(callback: (sharedTheme: SharedTheme?) -> Unit) {
    val cursorLoader = getMyContentProviderCursorLoader()
    ensureBackgroundThread {
        callback(getSharedThemeSync(cursorLoader))
    }
}

//fun Context.isOrWasThankYouInstalled(): Boolean {
//    return when {
//        resources.getBoolean(R.bool.pretend_thank_you_installed) -> true
//        baseConfig.hadThankYouInstalled -> true
//        isThankYouInstalled() -> {
//            baseConfig.hadThankYouInstalled = true
//            true
//        }
//
//        else -> false
//    }
//}

//fun Context.addLockedLabelIfNeeded(stringId: Int): String {
//    return "${getString(stringId)} (${getString(R.string.feature_locked)})"
//
////    return if (isOrWasThankYouInstalled()) {
////        getString(stringId)
////    } else {
////        "${getString(stringId)} (${getString(R.string.feature_locked)})"
////    }
//}

//fun Context.isContactBlocked(contact: Contact, callback: (Boolean) -> Unit) {
//    val phoneNumbers = contact.phoneNumbers.map { PhoneNumberUtils.stripSeparators(it.value) }
//    getBlockedNumbersWithContact { blockedNumbersWithContact ->
//        val blockedNumbers = blockedNumbersWithContact.map { it.number }
//        val allNumbersBlocked = phoneNumbers.all { it in blockedNumbers }
//        callback(allNumbersBlocked)
//    }
//}

//fun Context.getBlockedNumbers(): java.util.ArrayList<BlockedNumber> {
//    val blockedNumbers = java.util.ArrayList<BlockedNumber>()
//    if (!isDefaultDialer()) {
//        return blockedNumbers
//    }
//
//    val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
//    val projection = arrayOf(
//        BlockedNumberContract.BlockedNumbers.COLUMN_ID,
//        BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
//        BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER
//    )
//
//    queryCursor(uri, projection) { cursor ->
//        val id = cursor.getLongValue(BlockedNumberContract.BlockedNumbers.COLUMN_ID)
//        val number =
//            cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER) ?: ""
//        val normalizedNumber =
//            cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER) ?: number
//        val comparableNumber = normalizedNumber.trimToComparableNumber()
//        val blockedNumber = BlockedNumber(id, number, normalizedNumber, comparableNumber)
//        blockedNumbers.add(blockedNumber)
//    }
//
//    return blockedNumbers
//}

//fun Context.isNumberBlockedByPattern(
//    number: String,
//    blockedNumbers: java.util.ArrayList<BlockedNumber> = getBlockedNumbers()
//): Boolean {
//    for (blockedNumber in blockedNumbers) {
//        val num = blockedNumber.number
//        if (num.isBlockedNumberPattern()) {
//            val pattern = num.replace("+", "\\+").replace("*", ".*")
//            if (number.matches(pattern.toRegex())) {
//                return true
//            }
//        }
//    }
//    return false
//}


//fun Context.isNumberBlocked(
//    number: String,
//    blockedNumbers: java.util.ArrayList<BlockedNumber> = getBlockedNumbers()
//): Boolean {
//    val numberToCompare = number.trimToComparableNumber()
//
//    return blockedNumbers.any {
//        numberToCompare == it.numberToCompare ||
//                numberToCompare == it.number ||
//                PhoneNumberUtils.stripSeparators(number) == it.number
//    } || isNumberBlockedByPattern(number, blockedNumbers)
//}

//fun Context.deleteBlockedNumber(number: String): Boolean {
//    val selection = "${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ?"
//    val selectionArgs = arrayOf(number)
//
//    return if (isNumberBlocked(number)) {
//        val deletedRowCount = contentResolver.delete(
//            BlockedNumberContract.BlockedNumbers.CONTENT_URI,
//            selection,
//            selectionArgs
//        )
//
//        deletedRowCount > 0
//    } else {
//        true
//    }
//}

//fun Context.unblockContact(contact: Contact): Boolean {
//    var contactUnblocked = true
//    ensureBackgroundThread {
//        contact.phoneNumbers.forEach {
//            val numberUnblocked = deleteBlockedNumber(PhoneNumberUtils.stripSeparators(it.value))
//            contactUnblocked = contactUnblocked && numberUnblocked
//        }
//    }
//
//    return contactUnblocked
//}

//fun Context.getBlockedNumbersWithContact(callback: (ArrayList<BlockedNumber>) -> Unit) {
//    getContactsHasMap(true) { contacts ->
//        val blockedNumbers = java.util.ArrayList<BlockedNumber>()
//        if (!isDefaultDialer()) {
//            callback(blockedNumbers)
//        }
//
//        val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
//        val projection = arrayOf(
//            BlockedNumberContract.BlockedNumbers.COLUMN_ID,
//            BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
//            BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER,
//        )
//
//        queryCursor(uri, projection) { cursor ->
//            val id = cursor.getLongValue(BlockedNumberContract.BlockedNumbers.COLUMN_ID)
//            val number =
//                cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
//                    ?: ""
//            val normalizedNumber =
//                cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER)
//                    ?: number
//            val comparableNumber = normalizedNumber.trimToComparableNumber()
//
//            val contactName = contacts[comparableNumber]
//            val blockedNumber =
//                BlockedNumber(id, number, normalizedNumber, comparableNumber, contactName)
//            blockedNumbers.add(blockedNumber)
//        }
//
//        val blockedNumbersPair = blockedNumbers.partition { it.contactName != null }
//        val blockedNumbersWithNameSorted = blockedNumbersPair.first.sortedBy { it.contactName }
//        val blockedNumbersNoNameSorted = blockedNumbersPair.second.sortedBy { it.number }
//
//        callback(ArrayList(blockedNumbersWithNameSorted + blockedNumbersNoNameSorted))
//    }
//}


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
                index + 1,
                phoneAccount.accountHandle,
                label,
                address.substringAfter("tel:")
            )
            simAccounts.add(sim)
        }
    } catch (ignored: Exception) {
        Log.e(APP_NAME, "getAvailableSIMCardLabels: ${ignored.message}")
    }
    return simAccounts
}

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

fun Context.getContactsHasMap(
    withComparableNumbers: Boolean = false,
    callback: (HashMap<String, String>) -> Unit
) {
    ContactsHelper<Any?>(this).getContacts(showOnlyContactsWithNumbers = true) { contactList ->
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

fun Context.getAllContactSources(): ArrayList<ContactSource> {
    val sources = ContactsHelper<Any?>(this).getDeviceContactSources()
    sources.add(getPrivateContactSource())
    return sources.toMutableList() as ArrayList<ContactSource>
}

fun Context.getVisibleContactSources(): ArrayList<String> {
    val sources = getAllContactSources()
    val ignoredContactSources = baseConfig.ignoredContactSources
    return ArrayList(sources).filter { !ignoredContactSources.contains(it.getFullIdentifier()) }
        .map { it.name }.toMutableList() as ArrayList<String>
}

fun Context.getPrivateContactSource() =
    ContactSource(SMT_PRIVATE, SMT_PRIVATE, getString(R.string.phone_storage_hidden))

fun Context.getMyContentProviderCursorLoader() =
    CursorLoader(this, MyContentProvider.MY_CONTENT_URI, null, null, null, null)

fun Context.getSharedThemeSync(cursorLoader: CursorLoader): SharedTheme? {
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

fun Context.getTextSize() = when (baseConfig.fontSize) {
    FONT_SIZE_SMALL -> resources.getDimension(R.dimen.smaller_text_size)
    FONT_SIZE_MEDIUM -> resources.getDimension(R.dimen.bigger_text_size)
    FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
    else -> resources.getDimension(R.dimen.extra_big_text_size)
}

val Context.telecomManager: TelecomManager get() = getSystemService(Context.TELECOM_SERVICE) as TelecomManager

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

fun Context.isUsingSystemDarkTheme() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

//fun Context.checkAppIconColor() {
//    val appId = baseConfig.appId
//    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.appIconColor) {
//        getAppIconColors().forEachIndexed { index, color ->
//            toggleAppIconColor(appId, index, color, false)
//        }
//
//        getAppIconColors().forEachIndexed { index, color ->
//            if (baseConfig.appIconColor == color) {
//                toggleAppIconColor(appId, index, color, true)
//            }
//        }
//    }
//}
//
//fun Context.toggleAppIconColor(appId: String, colorIndex: Int, color: Int, enable: Boolean) {
//    val className =
//        "${appId.removeSuffix(".debug")}.activities.SplashActivity${appIconColorStrings[colorIndex]}"
//    val state =
//        if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
//    try {
//        packageManager.setComponentEnabledSetting(
//            ComponentName(appId, className), state, PackageManager.DONT_KILL_APP
//        )
//        if (enable) {
//            baseConfig.lastIconColor = color
//        }
//    } catch (e: Exception) {
//        Log.e(APP_NAME, "toggleAppIconColor: ${e.message}")
//    }
//}


fun Context.getAppIconColors() =
    resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())


fun Context.getTimeFormat() = if (baseConfig.use24HourFormat) TIME_FORMAT_24 else TIME_FORMAT_12


fun Context.getProperBackgroundColor() = if (baseConfig.isUsingSystemTheme) {
    resources.getColor(R.color.you_background_color, theme)
} else {
    baseConfig.backgroundColor
}

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