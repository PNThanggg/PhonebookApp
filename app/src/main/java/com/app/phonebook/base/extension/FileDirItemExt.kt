package com.app.phonebook.base.extension

import android.content.Context
import com.app.phonebook.data.models.FileDirItem

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}
