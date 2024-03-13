package com.app.phonebook.base.extension

import android.content.SharedPreferences
import android.telecom.PhoneAccountHandle
import com.app.phonebook.data.models.PhoneAccountHandleModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T> sharedPreferencesCallback(
    sendOnCollect: Boolean = false,
    value: () -> T?,
): Flow<T?> = callbackFlow {
    val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(value())
        }

    if (sendOnCollect) {
        trySend(value())
    }

//    registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
//    awaitClose { unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener) }
}

fun SharedPreferences.Editor.putPhoneAccountHandle(
    key: String,
    parcelable: PhoneAccountHandle
): SharedPreferences.Editor {
    val componentName = parcelable.componentName
    val myPhoneAccountHandleModel = PhoneAccountHandleModel(
        componentName.packageName, componentName.className, parcelable.id
    )
    val json = Gson().toJson(myPhoneAccountHandleModel)
    return putString(key, json)
}

inline fun <reified T : PhoneAccountHandleModel?> SharedPreferences.getPhoneAccountHandleModel(
    key: String,
    default: T
): T {
    val json = getString(key, null)
    return try {
        if (json != null) {
            Gson().fromJson(json, T::class.java)
        } else {
            default
        }
    } catch (_: JsonSyntaxException) {
        default
    }
}
