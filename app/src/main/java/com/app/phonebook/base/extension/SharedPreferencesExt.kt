package com.app.phonebook.base.extension

import android.content.SharedPreferences
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
