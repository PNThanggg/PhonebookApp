package com.app.phonebook.base.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.TransactionTooLargeException
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat
import com.app.phonebook.BuildConfig
import com.app.phonebook.R
import com.app.phonebook.base.utils.CONTACT_ID
import com.app.phonebook.base.utils.DEFAULT_FILE_NAME
import com.app.phonebook.base.utils.EXTRA_SHOW_ADVANCED
import com.app.phonebook.base.utils.IS_PRIVATE
import com.app.phonebook.base.utils.ON_CLICK_CALL_CONTACT
import com.app.phonebook.base.utils.ON_CLICK_EDIT_CONTACT
import com.app.phonebook.base.utils.ON_CLICK_VIEW_CONTACT
import com.app.phonebook.base.utils.OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
import com.app.phonebook.base.utils.OPEN_DOCUMENT_TREE_FOR_SDK_30
import com.app.phonebook.base.utils.OPEN_DOCUMENT_TREE_OTG
import com.app.phonebook.base.utils.OPEN_DOCUMENT_TREE_SD
import com.app.phonebook.base.utils.PERMISSION_READ_PHONE_STATE
import com.app.phonebook.base.utils.PERMISSION_READ_STORAGE
import com.app.phonebook.base.utils.SILENT
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.utils.isOnMainThread
import com.app.phonebook.base.utils.isRPlus
import com.app.phonebook.base.utils.isUpsideDownCakePlus
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.AlarmSound
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.FileDirItem
import com.app.phonebook.data.models.RadioItem
import com.app.phonebook.databinding.DialogTitleBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.SimpleContactsHelper
import com.app.phonebook.helpers.VcfExporter
import com.app.phonebook.presentation.activities.EditContactActivity
import com.app.phonebook.presentation.activities.ViewContactActivity
import com.app.phonebook.presentation.dialog.ConfirmationAdvancedDialog
import com.app.phonebook.presentation.dialog.RadioGroupDialog
import com.app.phonebook.presentation.dialog.SelectSIMDialog
import com.app.phonebook.presentation.dialog.WritePermissionDialog
import com.app.phonebook.presentation.view.MyTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ezvcard.VCardVersion
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

fun Activity.finishWithSlide() {
    finish()
    if (isUpsideDownCakePlus()) {
        overrideActivityTransition(
            OVERRIDE_TRANSITION_CLOSE, R.anim.slide_in_left, R.anim.slide_out_right, Color.TRANSPARENT
        )
    } else {
        @Suppress("DEPRECATION") overridePendingTransition(
            R.anim.slide_in_left, R.anim.slide_out_right
        )
    }
}

fun Activity.hideKeyboard() {
    if (isOnMainThread()) {
        hideKeyboardSync()
    } else {
        Handler(Looper.getMainLooper()).post {
            hideKeyboardSync()
        }
    }
}

fun Activity.hideKeyboardSync() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}


fun Activity.showKeyboard(editText: EditText) {
    editText.requestFocus()
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.launchCreateNewContactIntent() {
    Intent().apply {
        action = Intent.ACTION_INSERT
        data = ContactsContract.Contacts.CONTENT_URI
        launchActivityIntent(this)
    }
}

fun Activity.launchSendSMSIntent(recipient: String) {
    Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.fromParts("smsto", recipient, null)
        launchActivityIntent(this)
    }
}

fun Activity.launchViewContactIntent(uri: Uri) {
    Intent().apply {
        action = ContactsContract.QuickContact.ACTION_QUICK_CONTACT
        data = uri
        launchActivityIntent(this)
    }
}


fun Activity.startContactDetailsIntent(contact: Contact) {
//    if (contact.rawId > 1000000 && contact.contactId > 1000000 && contact.rawId == contact.contactId) {
//        Intent().apply {
//            action = Intent.ACTION_VIEW
//            putExtra(CONTACT_ID, contact.rawId)
//            putExtra(IS_PRIVATE, true)
//            setDataAndType(
//                ContactsContract.Contacts.CONTENT_LOOKUP_URI, "vnd.android.cursor.dir/person"
//            )
//            launchActivityIntent(this)
//        }
//    } else {
//        ensureBackgroundThread {
//            val lookupKey = SimpleContactsHelper(this).getContactLookupKey((contact).rawId.toString())
//            val publicUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
//            runOnUiThread {
//                launchViewContactIntent(publicUri)
//            }
//        }
//    }

    handleGenericContactClick(contact)
}

@SuppressLint("UseCompatLoadingForDrawables")
fun Activity.setupDialogStuff(
    view: View,
    dialog: AlertDialog.Builder,
    titleId: Int = 0,
    titleText: String = "",
    cancelOnTouchOutside: Boolean = true,
    callback: ((alertDialog: AlertDialog) -> Unit)? = null
) {
    if (isDestroyed || isFinishing) {
        return
    }

    val textColor = getProperTextColor()
    val primaryColor = getProperPrimaryColor()
    if (view is ViewGroup) {
        updateTextColors(view)
    } else if (view is MyTextView) {
        view.setColors(textColor, primaryColor)
    }

    if (dialog is MaterialAlertDialogBuilder) {
        dialog.create().apply {
            if (titleId != 0) {
                setTitle(titleId)
            } else if (titleText.isNotEmpty()) {
                setTitle(titleText)
            }

            setView(view)
            setCancelable(cancelOnTouchOutside)
            if (!isFinishing) {
                show()
            }
            getButton(Dialog.BUTTON_POSITIVE)?.setTextColor(primaryColor)
            getButton(Dialog.BUTTON_NEGATIVE)?.setTextColor(primaryColor)
            getButton(Dialog.BUTTON_NEUTRAL)?.setTextColor(primaryColor)
            callback?.invoke(this)
        }
    } else {
        var title: DialogTitleBinding? = null
        if (titleId != 0 || titleText.isNotEmpty()) {
            title = DialogTitleBinding.inflate(layoutInflater, null, false)
            title.dialogTitleTextview.apply {
                if (titleText.isNotEmpty()) {
                    text = titleText
                } else {
                    setText(titleId)
                }
                setTextColor(textColor)
            }
        }

        // if we use the same primary and background color, use the text color for dialog confirmation buttons
        val dialogButtonColor = if (primaryColor == baseConfig.backgroundColor) {
            textColor
        } else {
            primaryColor
        }

        dialog.create().apply {
            setView(view)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCustomTitle(title?.root)
            setCanceledOnTouchOutside(cancelOnTouchOutside)
            if (!isFinishing) {
                show()
            }
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dialogButtonColor)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialogButtonColor)
            getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(dialogButtonColor)

            val bgDrawable = when {
                isBlackAndWhiteTheme() -> resources.getDrawable(
                    R.drawable.black_dialog_background, theme
                )

                baseConfig.isUsingSystemTheme -> resources.getDrawable(
                    R.drawable.dialog_you_background, theme
                )

                else -> resources.getColoredDrawableWithColor(
                    drawableId = R.drawable.dialog_bg, color = baseConfig.backgroundColor, context = context
                )
            }

            window?.setBackgroundDrawable(bgDrawable)
            callback?.invoke(this)
        }
    }
}

fun Activity.getAlertDialogBuilder() = if (baseConfig.isUsingSystemTheme) {
    MaterialAlertDialogBuilder(this)
} else {
    AlertDialog.Builder(this)
}

fun Activity.onApplyWindowInsets(callback: (WindowInsetsCompat) -> Unit) {
    window.decorView.setOnApplyWindowInsetsListener { view, insets ->
        callback(WindowInsetsCompat.toWindowInsetsCompat(insets))
        view.onApplyWindowInsets(insets)
        insets
    }
}

// used at devices with multiple SIM cards
@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
fun BaseActivity<*>.getHandleToUse(intent: Intent?, phoneNumber: String, callback: (handle: PhoneAccountHandle?) -> Unit) {
    handlePermission(PERMISSION_READ_PHONE_STATE) {
        if (it) {
            val defaultHandle = telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL)
            when {
                intent?.hasExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE) == true -> callback(
                    intent.getParcelableExtra(
                        TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE
                    )!!
                )

                config.getCustomSIM(phoneNumber) != null -> {
                    callback(config.getCustomSIM(phoneNumber))
                }

                defaultHandle != null -> callback(defaultHandle)
                else -> {
                    SelectSIMDialog(this, phoneNumber, onDismiss = {
//                        if (this is DialerActivity) {
//                            finish()
//                        }
                    }) { handle ->
                        callback(handle)
                    }
                }
            }
        }
    }
}

fun Activity.handleGenericContactClick(contact: Contact) {
    when (config.onContactClick) {
        ON_CLICK_CALL_CONTACT -> callContact(contact)
        ON_CLICK_VIEW_CONTACT -> viewContact(contact)
        ON_CLICK_EDIT_CONTACT -> editContact(contact)
    }
}

fun Activity.callContact(contact: Contact) {
    hideKeyboard()
//    if (contact.phoneNumbers.isNotEmpty()) {
//        tryInitiateCall(contact) { startCallIntent(it) }
//    } else {
//        toast(R.string.no_phone_number_found)
//    }
}

fun Activity.viewContact(contact: Contact) {
    hideKeyboard()
    Intent(applicationContext, ViewContactActivity::class.java).apply {
        putExtra(CONTACT_ID, contact.id)
        putExtra(IS_PRIVATE, contact.isPrivate())
        startActivity(this)
    }
}

fun Activity.editContact(contact: Contact) {
    hideKeyboard()
    Intent(applicationContext, EditContactActivity::class.java).apply {
        putExtra(CONTACT_ID, contact.id)
        putExtra(IS_PRIVATE, contact.isPrivate())
        startActivity(this)
    }
}

fun BaseActivity<*>.isShowingOTGDialog(path: String): Boolean {
    return if (!isRPlus() && isPathOnOTG(path) && (baseConfig.OTGTreeUri.isEmpty() || !hasProperStoredTreeUri(true))) {
        showOTGPermissionDialog(path)
        true
    } else {
        false
    }
}

fun BaseActivity<*>.showOTGPermissionDialog(path: String) {
    runOnUiThread {
        if (!isDestroyed && !isFinishing) {
            WritePermissionDialog(this, WritePermissionDialog.WritePermissionDialogMode.Otg) {
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    try {
                        startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                        checkedDocumentPath = path
                        return@apply
                    } catch (e: Exception) {
                        type = "*/*"
                    }

                    try {
                        startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                        checkedDocumentPath = path
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
    }
}

fun BaseActivity<*>.isShowingSAFDialog(path: String): Boolean {
    return if ((!isRPlus() && isPathOnSD(path) && !isSDCardSetAsDefaultStorage() && (baseConfig.sdTreeUri.isEmpty() || !hasProperStoredTreeUri(
            false
        )))
    ) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                WritePermissionDialog(this, WritePermissionDialog.WritePermissionDialogMode.SdCard) {
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        putExtra(EXTRA_SHOW_ADVANCED, true)
                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_SD)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_SD)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

fun BaseActivity<*>.getFileOutputStream(
    fileDirItem: FileDirItem, allowCreatingNewFile: Boolean = false, callback: (outputStream: OutputStream?) -> Unit
) {
    val targetFile = File(fileDirItem.path)
    when {
        isRestrictedSAFOnlyRoot(fileDirItem.path) -> {
            handleAndroidSAFDialog(fileDirItem.path) {
                if (!it) {
                    return@handleAndroidSAFDialog
                }

                val uri = getAndroidSAFUri(fileDirItem.path)
                if (!getDoesFilePathExist(fileDirItem.path)) {
                    createAndroidSAFFile(fileDirItem.path)
                }
                callback.invoke(applicationContext.contentResolver.openOutputStream(uri, "wt"))
            }
        }

        needsStupidWritePermissions(fileDirItem.path) -> {
            handleSAFDialog(fileDirItem.path) {
                if (!it) {
                    return@handleSAFDialog
                }

                var document = getDocumentFile(fileDirItem.path)
                if (document == null && allowCreatingNewFile) {
                    document = getDocumentFile(fileDirItem.getParentPath())
                }

                if (document == null) {
                    showFileCreateError(fileDirItem.path)
                    callback(null)
                    return@handleSAFDialog
                }

                if (!getDoesFilePathExist(fileDirItem.path)) {
                    document = getDocumentFile(fileDirItem.path) ?: document.createFile("", fileDirItem.name)
                }

                if (document?.exists() == true) {
                    try {
                        callback(applicationContext.contentResolver.openOutputStream(document.uri, "wt"))
                    } catch (e: FileNotFoundException) {
                        showErrorToast(e)
                        callback(null)
                    }
                } else {
                    showFileCreateError(fileDirItem.path)
                    callback(null)
                }
            }
        }

        isAccessibleWithSAFSdk30(fileDirItem.path) -> {
            handleSAFDialogSdk30(fileDirItem.path) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }

                callback.invoke(
                    try {
                        val uri = createDocumentUriUsingFirstParentTreeUri(fileDirItem.path)
                        if (!getDoesFilePathExist(fileDirItem.path)) {
                            createSAFFileSdk30(fileDirItem.path)
                        }
                        applicationContext.contentResolver.openOutputStream(uri, "wt")
                    } catch (e: Exception) {
                        null
                    } ?: createCasualFileOutputStream(this, targetFile)
                )
            }
        }

        isRestrictedWithSAFSdk30(fileDirItem.path) -> {
            callback.invoke(
                try {
                    val fileUri = getFileUrisFromFileDirItems(arrayListOf(fileDirItem))
                    applicationContext.contentResolver.openOutputStream(fileUri.first(), "wt")
                } catch (e: Exception) {
                    null
                } ?: createCasualFileOutputStream(this, targetFile)
            )
        }

        else -> {
            callback.invoke(createCasualFileOutputStream(this, targetFile))
        }
    }
}

private fun createCasualFileOutputStream(activity: BaseActivity<*>, targetFile: File): OutputStream? {
    if (targetFile.parentFile?.exists() == false) {
        targetFile.parentFile?.mkdirs()
    }

    return try {
        FileOutputStream(targetFile)
    } catch (e: Exception) {
        activity.showErrorToast(e)
        null
    }
}

fun BaseActivity<*>.isShowingAndroidSAFDialog(path: String): Boolean {
    return if (isRestrictedSAFOnlyRoot(path) && (getAndroidTreeUri(path).isEmpty() || !hasProperStoredAndroidTreeUri(path))) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                ConfirmationAdvancedDialog(
                    this, "", R.string.confirm_storage_access_android_text, R.string.ok, R.string.cancel
                ) { success ->
                    if (success) {
                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            putExtra(EXTRA_SHOW_ADVANCED, true)
                            putExtra(DocumentsContract.EXTRA_INITIAL_URI, createAndroidDataOrObbUri(path))
                            try {
                                startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB)
                                checkedDocumentPath = path
                                return@apply
                            } catch (e: Exception) {
                                type = "*/*"
                            }

                            try {
                                startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB)
                                checkedDocumentPath = path
                            } catch (e: ActivityNotFoundException) {
                                toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                            } catch (e: Exception) {
                                toast(R.string.unknown_error_occurred)
                            }
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

@SuppressLint("RestrictedApi")
fun BaseActivity<*>.shareContacts(contacts: ArrayList<Contact>) {
    val filename = if (contacts.size == 1) {
        "${contacts.first().getNameToDisplay()}.vcf"
    } else {
        DEFAULT_FILE_NAME
    }

    val file = getTempFile(filename)
    if (file == null) {
        toast(R.string.unknown_error_occurred)
        return
    }

    getFileOutputStream(file.toFileDirItem(this), true) {
        // whatsApp does not support vCard version 4.0 yet
        VcfExporter().exportContacts(this, it, contacts, false, version = VCardVersion.V3_0) { result ->
            if (result == VcfExporter.ExportResult.EXPORT_OK) {
                sharePathIntent(file.absolutePath, BuildConfig.APPLICATION_ID)
            } else {
                showErrorToast("$result")
            }
        }
    }
}

fun Activity.getFinalUriFromPath(path: String, applicationId: String): Uri? {
    val uri = try {
        ensurePublicUri(path, applicationId)
    } catch (e: Exception) {
        showErrorToast(e)
        return null
    }

    if (uri == null) {
        toast(R.string.unknown_error_occurred)
        return null
    }

    return uri
}

fun Activity.sharePathIntent(path: String, applicationId: String) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, newUri)
            type = getUriMimeType(path, newUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            grantUriPermission("android", newUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                startActivity(Intent.createChooser(this, getString(R.string.share_via)))
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: RuntimeException) {
                if (e.cause is TransactionTooLargeException) {
                    toast(R.string.maximum_share_reached)
                } else {
                    showErrorToast(e)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

@SuppressLint("InlinedApi")
fun BaseActivity<*>.isShowingSAFDialogSdk30(path: String): Boolean {
    return if (isAccessibleWithSAFSdk30(path) && !hasProperStoredFirstParentUri(path)) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                val level = getFirstParentLevel(path)
                WritePermissionDialog(
                    this,
                    WritePermissionDialog.WritePermissionDialogMode.OpenDocumentTreeSDK30(path.getFirstParentPath(this, level))
                ) {
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        putExtra(EXTRA_SHOW_ADVANCED, true)
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, createFirstParentTreeUriUsingRootTree(path))
                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_SDK_30)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_SDK_30)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}


fun BaseActivity<*>.showContactSourcePicker(currentSource: String, callback: (newSource: String) -> Unit) {
    ContactsHelper(this).getSaveableContactSources { sources ->
        val items = ArrayList<RadioItem>()
        var sourceNames = sources.map { it.name }
        var currentSourceIndex = sourceNames.indexOfFirst { it == currentSource }
        sourceNames = sources.map { it.publicName }

        sourceNames.forEachIndexed { index, account ->
            items.add(RadioItem(index, account))
            if (currentSource == SMT_PRIVATE && account == getString(R.string.phone_storage_hidden)) {
                currentSourceIndex = index
            }
        }

        runOnUiThread {
            RadioGroupDialog(this, items, currentSourceIndex) {
                callback(sources[it as Int].name)
            }
        }
    }
}

fun BaseActivity<*>.getAlarmSounds(type: Int, callback: (java.util.ArrayList<AlarmSound>) -> Unit) {
    val alarms = ArrayList<AlarmSound>()
    val manager = RingtoneManager(this)
    manager.setType(type)

    try {
        val cursor = manager.cursor
        var curId = 1
        val silentAlarm = AlarmSound(curId++, getString(R.string.no_sound), SILENT)
        alarms.add(silentAlarm)

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            var uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
            val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            if (!uri.endsWith(id)) {
                uri += "/$id"
            }

            val alarmSound = AlarmSound(curId++, title, uri)
            alarms.add(alarmSound)
        }
        callback(alarms)
    } catch (e: Exception) {
        if (e is SecurityException) {
            handlePermission(PERMISSION_READ_STORAGE) {
                if (it) {
                    getAlarmSounds(type, callback)
                } else {
                    showErrorToast(e)
                    callback(ArrayList())
                }
            }
        } else {
            showErrorToast(e)
            callback(ArrayList())
        }
    }
}