package com.app.phonebook.interfaces

import android.view.ActionMode


abstract class MyActionModeCallback : ActionMode.Callback {
    var isSelectable = false
}
