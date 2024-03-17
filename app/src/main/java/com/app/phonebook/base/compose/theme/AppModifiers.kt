package com.app.phonebook.base.compose.theme

import android.app.Activity
import android.content.Context
import com.app.phonebook.base.compose.extensions.getActivity
import com.app.phonebook.base.utils.APP_LAUNCHER_NAME

fun Activity.getAppLauncherName(): String = intent.getStringExtra(APP_LAUNCHER_NAME).orEmpty()

private fun Context.getAppLauncherName(): String = getActivity().getAppLauncherName()

