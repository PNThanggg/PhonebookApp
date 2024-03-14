package com.app.phonebook.base.extension

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.app.phonebook.base.utils.TIME_FORMAT_12
import com.app.phonebook.base.utils.TIME_FORMAT_24
import com.app.phonebook.base.utils.appIconColorStrings
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.utils.isQPlus
import com.app.phonebook.data.models.SharedTheme
import com.app.phonebook.helpers.Config

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


val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)

fun Context.isUsingSystemDarkTheme() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

fun Context.checkAppIconColor() {
    val appId = baseConfig.appId
    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.appIconColor) {
        getAppIconColors().forEachIndexed { index, color ->
            toggleAppIconColor(appId, index, color, false)
        }

        getAppIconColors().forEachIndexed { index, color ->
            if (baseConfig.appIconColor == color) {
                toggleAppIconColor(appId, index, color, true)
            }
        }
    }
}

fun Context.toggleAppIconColor(appId: String, colorIndex: Int, color: Int, enable: Boolean) {
    val className =
        "${appId.removeSuffix(".debug")}.activities.SplashActivity${appIconColorStrings[colorIndex]}"
    val state =
        if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    try {
        packageManager.setComponentEnabledSetting(
            ComponentName(appId, className), state, PackageManager.DONT_KILL_APP
        )
        if (enable) {
            baseConfig.lastIconColor = color
        }
    } catch (e: Exception) {
        Log.e(APP_NAME, "toggleAppIconColor: ${e.message}")
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
        contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        0f
    ) > 0f

fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(
    this,
    getPermissionString(permId)
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