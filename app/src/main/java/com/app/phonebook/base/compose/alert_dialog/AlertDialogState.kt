package com.app.phonebook.base.compose.alert_dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue

@Composable
fun rememberAlertDialogState(
    isShownInitially: Boolean = false
) = remember { AlertDialogState(isShownInitially) }

@Stable
class AlertDialogState(isShownInitially: Boolean = false) {
    companion object {
        val SAVER = object : Saver<AlertDialogState, Boolean> {
            override fun restore(value: Boolean): AlertDialogState = AlertDialogState(value)
            override fun SaverScope.save(value: AlertDialogState): Boolean = value.isShown
        }
    }

    var isShown by mutableStateOf(isShownInitially)
        private set

    fun show() {
        if (isShown) {
            isShown = false
        }
        isShown = true
    }

    fun hide() {
        isShown = false
    }
}
