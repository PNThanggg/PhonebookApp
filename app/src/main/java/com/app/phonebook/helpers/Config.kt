package com.app.phonebook.helpers

import android.content.ComponentName
import android.content.Context
import android.telecom.PhoneAccountHandle
import com.app.phonebook.base.extension.getPhoneAccountHandleModel
import com.app.phonebook.base.extension.putPhoneAccountHandle
import com.app.phonebook.base.helpers.BaseConfig
import com.app.phonebook.base.utils.ALL_TABS_MASK
import com.app.phonebook.base.utils.ALWAYS_SHOW_FULLSCREEN
import com.app.phonebook.base.utils.DIALPAD_BEEPS
import com.app.phonebook.base.utils.DIALPAD_VIBRATION
import com.app.phonebook.base.utils.DISABLE_PROXIMITY_SENSOR
import com.app.phonebook.base.utils.DISABLE_SWIPE_TO_ANSWER
import com.app.phonebook.base.utils.GROUP_SUBSEQUENT_CALLS
import com.app.phonebook.base.utils.HIDE_DIALPAD_NUMBERS
import com.app.phonebook.base.utils.OPEN_DIAL_PAD_AT_LAUNCH
import com.app.phonebook.base.utils.REMEMBER_SIM_PREFIX
import com.app.phonebook.base.utils.SHOW_TABS
import com.app.phonebook.base.utils.WAS_OVERLAY_SNACKBAR_CONFIRMED
import com.app.phonebook.data.models.SpeedDial
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    fun getSpeedDialValues(): ArrayList<SpeedDial> {
        val speedDialType = object : TypeToken<List<SpeedDial>>() {}.type
        val speedDialValues =
            Gson().fromJson<ArrayList<SpeedDial>>(speedDial, speedDialType) ?: ArrayList(1)

        for (i in 1..9) {
            val speedDial = SpeedDial(i, "", "")
            if (speedDialValues.firstOrNull { it.id == i } == null) {
                speedDialValues.add(speedDial)
            }
        }

        return speedDialValues
    }

    fun saveCustomSIM(number: String, handle: PhoneAccountHandle) {
        prefs.edit().putPhoneAccountHandle(REMEMBER_SIM_PREFIX + number, handle).apply()
    }

    fun getCustomSIM(number: String): PhoneAccountHandle? {
        val myPhoneAccountHandle =
            prefs.getPhoneAccountHandleModel(REMEMBER_SIM_PREFIX + number, null)
        return if (myPhoneAccountHandle != null) {
            val packageName = myPhoneAccountHandle.packageName
            val className = myPhoneAccountHandle.className
            val componentName = ComponentName(packageName, className)
            val id = myPhoneAccountHandle.id
            PhoneAccountHandle(componentName, id)
        } else {
            null
        }
    }

    fun removeCustomSIM(number: String) {
        prefs.edit().remove(REMEMBER_SIM_PREFIX + number).apply()
    }

    var showTabs: Int
        get() = prefs.getInt(SHOW_TABS, ALL_TABS_MASK)
        set(showTabs) = prefs.edit().putInt(SHOW_TABS, showTabs).apply()

    var groupSubsequentCalls: Boolean
        get() = prefs.getBoolean(GROUP_SUBSEQUENT_CALLS, true)
        set(groupSubsequentCalls) = prefs.edit()
            .putBoolean(GROUP_SUBSEQUENT_CALLS, groupSubsequentCalls).apply()

    var openDialPadAtLaunch: Boolean
        get() = prefs.getBoolean(OPEN_DIAL_PAD_AT_LAUNCH, false)
        set(openDialPad) = prefs.edit().putBoolean(OPEN_DIAL_PAD_AT_LAUNCH, openDialPad).apply()

    var disableProximitySensor: Boolean
        get() = prefs.getBoolean(DISABLE_PROXIMITY_SENSOR, false)
        set(disableProximitySensor) = prefs.edit()
            .putBoolean(DISABLE_PROXIMITY_SENSOR, disableProximitySensor).apply()

    var disableSwipeToAnswer: Boolean
        get() = prefs.getBoolean(DISABLE_SWIPE_TO_ANSWER, false)
        set(disableSwipeToAnswer) = prefs.edit()
            .putBoolean(DISABLE_SWIPE_TO_ANSWER, disableSwipeToAnswer).apply()

    var wasOverlaySnackbarConfirmed: Boolean
        get() = prefs.getBoolean(WAS_OVERLAY_SNACKBAR_CONFIRMED, false)
        set(wasOverlaySnackbarConfirmed) = prefs.edit()
            .putBoolean(WAS_OVERLAY_SNACKBAR_CONFIRMED, wasOverlaySnackbarConfirmed).apply()

    var dialpadVibration: Boolean
        get() = prefs.getBoolean(DIALPAD_VIBRATION, true)
        set(dialpadVibration) = prefs.edit().putBoolean(DIALPAD_VIBRATION, dialpadVibration).apply()

    var hideDialpadNumbers: Boolean
        get() = prefs.getBoolean(HIDE_DIALPAD_NUMBERS, false)
        set(hideDialpadNumbers) = prefs.edit().putBoolean(HIDE_DIALPAD_NUMBERS, hideDialpadNumbers)
            .apply()

    var dialpadBeeps: Boolean
        get() = prefs.getBoolean(DIALPAD_BEEPS, true)
        set(dialpadBeeps) = prefs.edit().putBoolean(DIALPAD_BEEPS, dialpadBeeps).apply()

    var alwaysShowFullscreen: Boolean
        get() = prefs.getBoolean(ALWAYS_SHOW_FULLSCREEN, false)
        set(alwaysShowFullscreen) = prefs.edit()
            .putBoolean(ALWAYS_SHOW_FULLSCREEN, alwaysShowFullscreen).apply()
}
