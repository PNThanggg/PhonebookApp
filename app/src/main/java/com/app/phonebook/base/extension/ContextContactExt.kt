package com.app.phonebook.base.extension

import android.content.Context
import com.app.phonebook.base.utils.DEFAULT_MIMETYPE
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.base.utils.PERMISSION_WRITE_CONTACTS
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.data.dao.ContactsDao
import com.app.phonebook.data.dao.GroupsDao
import com.app.phonebook.data.database.ContactsDatabase
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.Organization

fun Context.hasContactPermissions() =
    hasPermission(PERMISSION_READ_CONTACTS) && hasPermission(PERMISSION_WRITE_CONTACTS)

val Context.contactsDB: ContactsDao
    get() = ContactsDatabase.getInstance(applicationContext).ContactsDao()

val Context.groupsDB: GroupsDao get() = ContactsDatabase.getInstance(applicationContext).GroupsDao()

fun Context.getEmptyContact(): Contact {
    val originalContactSource =
        if (hasContactPermissions()) baseConfig.lastUsedContactSource else SMT_PRIVATE
    val organization = Organization("", "")
    return Contact(
        0,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ArrayList(),
        ArrayList(),
        ArrayList(),
        ArrayList(),
        originalContactSource,
        0,
        0,
        "",
        null,
        "",
        ArrayList(),
        organization,
        ArrayList(),
        ArrayList(),
        DEFAULT_MIMETYPE,
        null
    )
}