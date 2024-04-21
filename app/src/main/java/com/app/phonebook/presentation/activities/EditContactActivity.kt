package com.app.phonebook.presentation.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.MediaStore
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.app.phonebook.R
import com.app.phonebook.adapter.AutoCompleteTextViewAdapter
import com.app.phonebook.base.extension.applyColorFilter
import com.app.phonebook.base.extension.beGone
import com.app.phonebook.base.extension.beInvisible
import com.app.phonebook.base.extension.beVisible
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getCachePhoto
import com.app.phonebook.base.extension.getCachePhotoUri
import com.app.phonebook.base.extension.getContactPublicUri
import com.app.phonebook.base.extension.getContactUriRawId
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getDateTimeFromDateString
import com.app.phonebook.base.extension.getDefaultAlarmSound
import com.app.phonebook.base.extension.getEmptyContact
import com.app.phonebook.base.extension.getFilenameFromPath
import com.app.phonebook.base.extension.getLookupUriRawId
import com.app.phonebook.base.extension.getNameLetter
import com.app.phonebook.base.extension.getPhoneNumberTypeText
import com.app.phonebook.base.extension.getProperPrimaryColor
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.extension.getPublicContactSource
import com.app.phonebook.base.extension.getVisibleContactSources
import com.app.phonebook.base.extension.hasContactPermissions
import com.app.phonebook.base.extension.hideKeyboard
import com.app.phonebook.base.extension.isVisible
import com.app.phonebook.base.extension.launchActivityIntent
import com.app.phonebook.base.extension.normalizePhoneNumber
import com.app.phonebook.base.extension.onGlobalLayout
import com.app.phonebook.base.extension.realScreenSize
import com.app.phonebook.base.extension.shareContacts
import com.app.phonebook.base.extension.showContactSourcePicker
import com.app.phonebook.base.extension.showErrorToast
import com.app.phonebook.base.extension.showKeyboard
import com.app.phonebook.base.extension.statusBarHeight
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.extension.updateTextColors
import com.app.phonebook.base.extension.value
import com.app.phonebook.base.utils.ADD_NEW_CONTACT_NUMBER
import com.app.phonebook.base.utils.CONTACT_ID
import com.app.phonebook.base.utils.DEFAULT_ADDRESS_TYPE
import com.app.phonebook.base.utils.DEFAULT_EMAIL_TYPE
import com.app.phonebook.base.utils.DEFAULT_EVENT_TYPE
import com.app.phonebook.base.utils.DEFAULT_IM_TYPE
import com.app.phonebook.base.utils.DEFAULT_PHONE_NUMBER_TYPE
import com.app.phonebook.base.utils.IS_FROM_SIMPLE_CONTACTS
import com.app.phonebook.base.utils.IS_PRIVATE
import com.app.phonebook.base.utils.KEY_EMAIL
import com.app.phonebook.base.utils.KEY_NAME
import com.app.phonebook.base.utils.KEY_PHONE
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.base.utils.PERMISSION_WRITE_CONTACTS
import com.app.phonebook.base.utils.PHOTO_ADDED
import com.app.phonebook.base.utils.PHOTO_CHANGED
import com.app.phonebook.base.utils.PHOTO_REMOVED
import com.app.phonebook.base.utils.PHOTO_UNCHANGED
import com.app.phonebook.base.utils.SAVE_DISCARD_PROMPT_INTERVAL
import com.app.phonebook.base.utils.SHOW_ADDRESSES_FIELD
import com.app.phonebook.base.utils.SHOW_CONTACT_SOURCE_FIELD
import com.app.phonebook.base.utils.SHOW_EMAILS_FIELD
import com.app.phonebook.base.utils.SHOW_EVENTS_FIELD
import com.app.phonebook.base.utils.SHOW_FIRST_NAME_FIELD
import com.app.phonebook.base.utils.SHOW_GROUPS_FIELD
import com.app.phonebook.base.utils.SHOW_IMS_FIELD
import com.app.phonebook.base.utils.SHOW_MIDDLE_NAME_FIELD
import com.app.phonebook.base.utils.SHOW_NICKNAME_FIELD
import com.app.phonebook.base.utils.SHOW_NOTES_FIELD
import com.app.phonebook.base.utils.SHOW_ORGANIZATION_FIELD
import com.app.phonebook.base.utils.SHOW_PHONE_NUMBERS_FIELD
import com.app.phonebook.base.utils.SHOW_PREFIX_FIELD
import com.app.phonebook.base.utils.SHOW_RINGTONE_FIELD
import com.app.phonebook.base.utils.SHOW_SUFFIX_FIELD
import com.app.phonebook.base.utils.SHOW_SURNAME_FIELD
import com.app.phonebook.base.utils.SHOW_WEBSITES_FIELD
import com.app.phonebook.base.utils.SILENT
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.utils.letterBackgroundColors
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Address
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.Email
import com.app.phonebook.data.models.Event
import com.app.phonebook.data.models.Group
import com.app.phonebook.data.models.IM
import com.app.phonebook.data.models.Organization
import com.app.phonebook.data.models.PhoneNumber
import com.app.phonebook.data.models.RadioItem
import com.app.phonebook.databinding.ActivityEditContactBinding
import com.app.phonebook.databinding.ItemEditAddressBinding
import com.app.phonebook.databinding.ItemEditEmailBinding
import com.app.phonebook.databinding.ItemEditGroupBinding
import com.app.phonebook.databinding.ItemEditImBinding
import com.app.phonebook.databinding.ItemEditPhoneNumberBinding
import com.app.phonebook.databinding.ItemEditWebsiteBinding
import com.app.phonebook.databinding.ItemEventBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.presentation.dialog.ConfirmationAdvancedDialog
import com.app.phonebook.presentation.dialog.ConfirmationDialog
import com.app.phonebook.presentation.dialog.CustomLabelDialog
import com.app.phonebook.presentation.dialog.ManageVisibleFieldsDialog
import com.app.phonebook.presentation.dialog.MyDatePickerDialog
import com.app.phonebook.presentation.dialog.RadioGroupDialog
import com.app.phonebook.presentation.dialog.SelectAlarmSoundDialog
import com.app.phonebook.presentation.dialog.SelectGroupsDialog
import com.app.phonebook.presentation.view.MyAutoCompleteTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

class EditContactActivity : BaseActivity<ActivityEditContactBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityEditContactBinding {
        return ActivityEditContactBinding.inflate(inflater)
    }

    private val PICK_RINGTONE_INTENT_ID = 1500
    private val INTENT_SELECT_RINGTONE = 600

    private var contact: Contact? = null
    private var originalRingtone: String? = null
    private var currentContactPhotoPath = ""

    companion object {
        private const val INTENT_TAKE_PHOTO = 1
        private const val INTENT_CHOOSE_PHOTO = 2
        private const val INTENT_CROP_PHOTO = 3

        private const val TAKE_PHOTO = 1
        private const val CHOOSE_PHOTO = 2
        private const val REMOVE_PHOTO = 3

        private const val AUTO_COMPLETE_DELAY = 5000L
    }

    private var mLastSavePromptTS = 0L
    private var wasActivityInitialized = false
    private var lastPhotoIntentUri: Uri? = null
    private var isSaving = false
    private var isThirdPartyIntent = false
    private var highlightLastPhoneNumber = false
    private var highlightLastEmail = false
    private var numberViewToColor: EditText? = null
    private var emailViewToColor: EditText? = null
    private var originalContactSource = ""

    enum class PrimaryNumberStatus {
        UNCHANGED, STARRED, UNSTARRED
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.contactWrapper.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setupInsets()
        setupMenu()

        isThirdPartyIntent =
            intent.action == Intent.ACTION_EDIT || intent.action == Intent.ACTION_INSERT || intent.action == ADD_NEW_CONTACT_NUMBER
        val isFromSimpleContacts = intent.getBooleanExtra(IS_FROM_SIMPLE_CONTACTS, false)
        if (isThirdPartyIntent && !isFromSimpleContacts) {
            handlePermission(PERMISSION_READ_CONTACTS) {
                if (it) {
                    handlePermission(PERMISSION_WRITE_CONTACTS) { value ->
                        if (value) {
                            initContact()
                        } else {
                            toast(R.string.no_contacts_permission)
                            hideKeyboard()
                            finish()
                        }
                    }
                } else {
                    toast(R.string.no_contacts_permission)
                    hideKeyboard()
                    finish()
                }
            }
        } else {
            initContact()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_RINGTONE_INTENT_ID && resultCode == RESULT_OK && resultData != null && resultData.dataString != null) {
            customRingtoneSelected(Uri.decode(resultData.dataString!!))
        } else if (requestCode == INTENT_SELECT_RINGTONE && resultCode == Activity.RESULT_OK && resultData != null) {
            val extras = resultData.extras
            if (extras?.containsKey(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) == true) {
                val uri = extras.getParcelable<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                try {
                    systemRingtoneSelected(uri)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }

    private fun customRingtoneSelected(ringtonePath: String) {
        contact?.ringtone = ringtonePath
        binding.contactRingtone.text = ringtonePath.getFilenameFromPath()
    }

    private fun systemRingtoneSelected(uri: Uri?) {
        contact?.ringtone = uri?.toString() ?: ""
        val contactRingtone = RingtoneManager.getRingtone(this, uri)
        binding.contactRingtone.text = contactRingtone.getTitle(this)
    }

    private fun setupInsets() {
        binding.contactWrapper.setOnApplyWindowInsetsListener { _, insets ->
            val windowInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            binding.contactScrollview.run {
                setPadding(paddingLeft, paddingTop, paddingRight, imeInsets.bottom)
            }
            insets
        }
    }


    private fun setupMenu() {
        (binding.contactAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin = statusBarHeight
        binding.contactToolbar.menu.apply {
            findItem(R.id.save).setOnMenuItemClickListener {
                saveContact()
                true
            }

            findItem(R.id.share).setOnMenuItemClickListener {
                shareContact(contact!!)
                true
            }

            findItem(R.id.open_with).setOnMenuItemClickListener {
                openWith()
                true
            }

            findItem(R.id.delete).setOnMenuItemClickListener {
                deleteContact()
                true
            }

            findItem(R.id.manage_visible_fields).setOnMenuItemClickListener {
                ManageVisibleFieldsDialog(this@EditContactActivity) {
                    initContact()
                }
                true
            }
        }

        binding.contactToolbar.setNavigationOnClickListener {
            hideKeyboard()
            finish()
        }
    }

    private fun deleteContact() {
        ConfirmationDialog(this) {
            if (contact != null) {
                ContactsHelper(this).deleteContact(contact!!, false) {
                    finish()
                }
            }
        }
    }

    private fun hasContactChanged() = contact != null && contact != fillContactValues() || originalRingtone != contact?.ringtone

    private fun openWith() {
        Intent().apply {
            action = Intent.ACTION_EDIT
            data = getContactPublicUri(contact!!)
            launchActivityIntent(this)
        }
    }

    private fun shareContact(contact: Contact) {
        shareContacts(arrayListOf(contact))
    }

    private fun fillContactValues(): Contact {
        val filledPhoneNumbers = getFilledPhoneNumbers()
        val filledEmails = getFilledEmails()
        val filledAddresses = getFilledAddresses()
        val filledIMs = getFilledIMs()
        val filledEvents = getFilledEvents()
        val filledWebsites = getFilledWebsites()

        val newContact = contact!!.copy(
            prefix = binding.contactPrefix.value,
            firstName = binding.contactFirstName.value,
            middleName = binding.contactMiddleName.value,
            surname = binding.contactSurname.value,
            suffix = binding.contactSuffix.value,
            nickname = binding.contactNickname.value,
            photoUri = currentContactPhotoPath,
            phoneNumbers = filledPhoneNumbers,
            emails = filledEmails,
            addresses = filledAddresses,
            listIM = filledIMs,
            events = filledEvents,
            starred = if (isContactStarred()) 1 else 0,
            notes = binding.contactNotes.value,
            websites = filledWebsites,
        )

        val company = binding.contactOrganizationCompany.value
        val jobPosition = binding.contactOrganizationJobPosition.value
        newContact.organization = Organization(company, jobPosition)
        return newContact
    }

    private fun isContactStarred() = binding.contactToggleFavorite.tag == 1

    private fun initContact() {
        var contactId = intent.getIntExtra(CONTACT_ID, 0)
        val action = intent.action
        if (contactId == 0 && (action == Intent.ACTION_EDIT || action == ADD_NEW_CONTACT_NUMBER)) {
            val data = intent.data
            if (data != null && data.path != null) {
                val rawId = if (data.path!!.contains("lookup")) {
                    if (data.pathSegments.last().startsWith("local_")) {
                        data.path!!.substringAfter("local_").toInt()
                    } else {
                        getLookupUriRawId(data)
                    }
                } else {
                    getContactUriRawId(data)
                }

                if (rawId != -1) {
                    contactId = rawId
                }
            }
        }

        if (contactId != 0) {
            ensureBackgroundThread {
                contact = ContactsHelper(this).getContactWithId(contactId, intent.getBooleanExtra(IS_PRIVATE, false))
                if (contact == null) {
                    toast(R.string.unknown_error_occurred)
                    hideKeyboard()
                    finish()
                } else {
                    runOnUiThread {
                        gotContact()
                    }
                }
            }
        } else {
            gotContact()
        }
    }

    private fun setupEditContact() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setupNames()
        setupPhoneNumbers()
        setupEmails()
        setupAddresses()
        setupIMs()
        setupNotes()
        setupOrganization()
        setupWebsites()
        setupEvents()
        setupGroups()
        setupContactSource()
    }

    private fun setupNames() {
        contact!!.apply {
            binding.contactPrefix.setText(prefix)
            binding.contactFirstName.setText(firstName)
            binding.contactMiddleName.setText(middleName)
            binding.contactSurname.setText(surname)
            binding.contactSuffix.setText(suffix)
            binding.contactNickname.setText(nickname)
        }
    }

    private fun getPhoneNumberFromIntent(intent: Intent): String? {
        if (intent.extras?.containsKey(KEY_PHONE) == true) {
            return intent.getStringExtra(KEY_PHONE)
        } else if (intent.extras?.containsKey("data") == true) {
            // sample contact number from Google Contacts:
            // data: [data1=+123 456 789 mimetype=vnd.android.cursor.item/phone_v2 _id=-1 data2=0]
            val data = intent.extras!!.get("data")
            if (data != null) {
                val contentValues = (data as? ArrayList<Any>)?.firstOrNull() as? ContentValues
                if (contentValues != null && contentValues.containsKey("data1")) {
                    return contentValues.getAsString("data1")
                }
            }
        }
        return null
    }


    private fun gotContact() {
        binding.contactScrollview.beVisible()
        if (contact == null) {
            setupNewContact()
        } else {
            setupEditContact()
            originalRingtone = contact?.ringtone
        }

        val action = intent.action
        if (((contact!!.id == 0 && action == Intent.ACTION_INSERT) || action == ADD_NEW_CONTACT_NUMBER) && intent.extras != null) {
            val phoneNumber = getPhoneNumberFromIntent(intent)
            if (phoneNumber != null) {
                contact!!.phoneNumbers.add(
                    PhoneNumber(
                        phoneNumber, DEFAULT_PHONE_NUMBER_TYPE, "", phoneNumber.normalizePhoneNumber()
                    )
                )
                if (phoneNumber.isNotEmpty() && action == ADD_NEW_CONTACT_NUMBER) {
                    highlightLastPhoneNumber = true
                }
            }

            val email = intent.getStringExtra(KEY_EMAIL)
            if (email != null) {
                val newEmail = Email(email, DEFAULT_EMAIL_TYPE, "")
                contact!!.emails.add(newEmail)
                highlightLastEmail = true
            }

            val firstName = intent.extras!!.get(KEY_NAME)
            if (firstName != null) {
                contact!!.firstName = firstName.toString()
            }

            val data = intent.extras!!.getParcelableArrayList<ContentValues>("data")
            if (data != null) {
                parseIntentData(data)
            }
            setupEditContact()
        }

        setupTypePickers()
        setupRingtone()

        if (contact!!.photoUri.isEmpty() && contact!!.photo == null) {
            showPhotoPlaceholder(binding.contactPhoto)
            binding.contactPhotoBottomShadow.beGone()
        } else {
            updateContactPhoto(contact!!.photoUri, binding.contactPhoto, binding.contactPhotoBottomShadow, contact!!.photo)
        }

        val textColor = getProperTextColor()
        arrayOf(
            binding.contactNameImage,
            binding.contactNumbersImage,
            binding.contactEmailsImage,
            binding.contactAddressesImage,
            binding.contactImsImage,
            binding.contactEventsImage,
            binding.contactNotesImage,
            binding.contactRingtoneImage,
            binding.contactOrganizationImage,
            binding.contactWebsitesImage,
            binding.contactGroupsImage,
            binding.contactSourceImage
        ).forEach {
            it.applyColorFilter(textColor)
        }

        val properPrimaryColor = getProperPrimaryColor()
        arrayOf(
            binding.contactNumbersAddNew,
            binding.contactEmailsAddNew,
            binding.contactAddressesAddNew,
            binding.contactImsAddNew,
            binding.contactEventsAddNew,
            binding.contactWebsitesAddNew,
            binding.contactGroupsAddNew
        ).forEach {
            it.applyColorFilter(properPrimaryColor)
        }

        arrayOf(
            binding.contactNumbersAddNew.background,
            binding.contactEmailsAddNew.background,
            binding.contactAddressesAddNew.background,
            binding.contactImsAddNew.background,
            binding.contactEventsAddNew.background,
            binding.contactWebsitesAddNew.background,
            binding.contactGroupsAddNew.background
        ).forEach {
            it.applyColorFilter(textColor)
        }

        binding.contactToggleFavorite.setOnClickListener { toggleFavorite() }
        binding.contactPhoto.setOnClickListener { trySetPhoto() }
        binding.contactChangePhoto.setOnClickListener { trySetPhoto() }
        binding.contactNumbersAddNew.setOnClickListener { addNewPhoneNumberField() }
        binding.contactEmailsAddNew.setOnClickListener { addNewEmailField() }
        binding.contactAddressesAddNew.setOnClickListener { addNewAddressField() }
        binding.contactImsAddNew.setOnClickListener { addNewIMField() }
        binding.contactEventsAddNew.setOnClickListener { addNewEventField() }
        binding.contactWebsitesAddNew.setOnClickListener { addNewWebsiteField() }
        binding.contactGroupsAddNew.setOnClickListener { showSelectGroupsDialog() }
        binding.contactSource.setOnClickListener { showSelectContactSourceDialog() }

        binding.contactChangePhoto.setOnLongClickListener { toast(R.string.change_photo); true; }

        setupFieldVisibility()

        binding.contactToggleFavorite.apply {
            setImageDrawable(getStarDrawable(contact!!.starred == 1))
            tag = contact!!.starred
            setOnLongClickListener { toast(R.string.toggle_favorite); true; }
        }

        val nameTextViews =
            arrayOf(binding.contactFirstName, binding.contactMiddleName, binding.contactSurname).filter { it.isVisible }
        if (nameTextViews.isNotEmpty()) {
            setupAutoComplete(nameTextViews)
        }

        updateTextColors(binding.contactScrollview)
        numberViewToColor?.setTextColor(properPrimaryColor)
        emailViewToColor?.setTextColor(properPrimaryColor)
        wasActivityInitialized = true

        binding.contactToolbar.menu.apply {
            findItem(R.id.delete).isVisible = contact?.id != 0
            findItem(R.id.share).isVisible = contact?.id != 0
            findItem(R.id.open_with).isVisible = contact?.id != 0 && contact?.isPrivate() == false
        }
    }

    private fun setupFieldVisibility() {
        val showFields = config.showContactFields
        if (showFields and (SHOW_PREFIX_FIELD or SHOW_FIRST_NAME_FIELD or SHOW_MIDDLE_NAME_FIELD or SHOW_SURNAME_FIELD or SHOW_SUFFIX_FIELD) == 0) {
            binding.contactNameImage.beInvisible()
        }

        binding.contactPrefix.beVisibleIf(showFields and SHOW_PREFIX_FIELD != 0)
        binding.contactFirstName.beVisibleIf(showFields and SHOW_FIRST_NAME_FIELD != 0)
        binding.contactMiddleName.beVisibleIf(showFields and SHOW_MIDDLE_NAME_FIELD != 0)
        binding.contactSurname.beVisibleIf(showFields and SHOW_SURNAME_FIELD != 0)
        binding.contactSuffix.beVisibleIf(showFields and SHOW_SUFFIX_FIELD != 0)
        binding.contactNickname.beVisibleIf(showFields and SHOW_NICKNAME_FIELD != 0)

        binding.contactSource.beVisibleIf(showFields and SHOW_CONTACT_SOURCE_FIELD != 0)
        binding.contactSourceImage.beVisibleIf(showFields and SHOW_CONTACT_SOURCE_FIELD != 0)

        val arePhoneNumbersVisible = showFields and SHOW_PHONE_NUMBERS_FIELD != 0
        binding.contactNumbersImage.beVisibleIf(arePhoneNumbersVisible)
        binding.contactNumbersHolder.beVisibleIf(arePhoneNumbersVisible)
        binding.contactNumbersAddNew.beVisibleIf(arePhoneNumbersVisible)

        val areEmailsVisible = showFields and SHOW_EMAILS_FIELD != 0
        binding.contactEmailsImage.beVisibleIf(areEmailsVisible)
        binding.contactEmailsHolder.beVisibleIf(areEmailsVisible)
        binding.contactEmailsAddNew.beVisibleIf(areEmailsVisible)

        val areAddressesVisible = showFields and SHOW_ADDRESSES_FIELD != 0
        binding.contactAddressesImage.beVisibleIf(areAddressesVisible)
        binding.contactAddressesHolder.beVisibleIf(areAddressesVisible)
        binding.contactAddressesAddNew.beVisibleIf(areAddressesVisible)

        val areIMsVisible = showFields and SHOW_IMS_FIELD != 0
        binding.contactImsImage.beVisibleIf(areIMsVisible)
        binding.contactImsHolder.beVisibleIf(areIMsVisible)
        binding.contactImsAddNew.beVisibleIf(areIMsVisible)

        val isOrganizationVisible = showFields and SHOW_ORGANIZATION_FIELD != 0
        binding.contactOrganizationCompany.beVisibleIf(isOrganizationVisible)
        binding.contactOrganizationJobPosition.beVisibleIf(isOrganizationVisible)
        binding.contactOrganizationImage.beVisibleIf(isOrganizationVisible)

        val areEventsVisible = showFields and SHOW_EVENTS_FIELD != 0
        binding.contactEventsImage.beVisibleIf(areEventsVisible)
        binding.contactEventsHolder.beVisibleIf(areEventsVisible)
        binding.contactEventsAddNew.beVisibleIf(areEventsVisible)

        val areWebsitesVisible = showFields and SHOW_WEBSITES_FIELD != 0
        binding.contactWebsitesImage.beVisibleIf(areWebsitesVisible)
        binding.contactWebsitesHolder.beVisibleIf(areWebsitesVisible)
        binding.contactWebsitesAddNew.beVisibleIf(areWebsitesVisible)

        val areGroupsVisible = showFields and SHOW_GROUPS_FIELD != 0
        binding.contactGroupsImage.beVisibleIf(areGroupsVisible)
        binding.contactGroupsHolder.beVisibleIf(areGroupsVisible)
        binding.contactGroupsAddNew.beVisibleIf(areGroupsVisible)

        val areNotesVisible = showFields and SHOW_NOTES_FIELD != 0
        binding.contactNotes.beVisibleIf(areNotesVisible)
        binding.contactNotesImage.beVisibleIf(areNotesVisible)

        val isRingtoneVisible = showFields and SHOW_RINGTONE_FIELD != 0
        binding.contactRingtone.beVisibleIf(isRingtoneVisible)
        binding.contactRingtoneImage.beVisibleIf(isRingtoneVisible)
    }

    private fun setupNewContact() {
        originalContactSource = if (hasContactPermissions()) config.lastUsedContactSource else SMT_PRIVATE
        contact = getEmptyContact()
        getPublicContactSource(contact!!.source) {
            binding.contactSource.text = if (it == "") getString(R.string.phone_storage) else it
        }

        // if the last used contact source is not available anymore, use the first available one. Could happen at ejecting SIM card
        ContactsHelper(this).getSaveableContactSources { sources ->
            val sourceNames = sources.map { it.name }
            if (!sourceNames.contains(originalContactSource)) {
                originalContactSource = sourceNames.first()
                contact?.source = originalContactSource
                getPublicContactSource(contact!!.source) {
                    binding.contactSource.text = if (it == "") getString(R.string.phone_storage) else it
                }
            }
        }
    }

    private fun saveContact() {
        if (isSaving || contact == null) {
            return
        }

        val contactFields = arrayListOf(
            binding.contactPrefix,
            binding.contactFirstName,
            binding.contactMiddleName,
            binding.contactSurname,
            binding.contactSuffix,
            binding.contactNickname,
            binding.contactNotes,
            binding.contactOrganizationCompany,
            binding.contactOrganizationJobPosition
        )

        if (contactFields.all { it.value.isEmpty() }) {
            if (currentContactPhotoPath.isEmpty() && getFilledPhoneNumbers().isEmpty() && getFilledEmails().isEmpty() && getFilledAddresses().isEmpty() && getFilledIMs().isEmpty() && getFilledEvents().isEmpty() && getFilledWebsites().isEmpty()) {
                toast(R.string.fields_empty)
                return
            }
        }

        val contactValues = fillContactValues()

        val oldPhotoUri = contact!!.photoUri
        val oldPrimary = contact!!.phoneNumbers.find { it.isPrimary }
        val newPrimary = contactValues.phoneNumbers.find { it.isPrimary }
        val primaryState = Pair(oldPrimary, newPrimary)

        contact = contactValues

        ensureBackgroundThread {
            config.lastUsedContactSource = contact!!.source
            when {
                contact!!.id == 0 -> insertNewContact(false)
                originalContactSource != contact!!.source -> insertNewContact(true)
                else -> {
                    val photoUpdateStatus = getPhotoUpdateStatus(oldPhotoUri, contact!!.photoUri)
                    updateContact(photoUpdateStatus, primaryState)
                }
            }
        }
    }

    private fun getPhotoUpdateStatus(oldUri: String, newUri: String): Int {
        return if (oldUri.isEmpty() && newUri.isNotEmpty()) {
            PHOTO_ADDED
        } else if (oldUri.isNotEmpty() && newUri.isEmpty()) {
            PHOTO_REMOVED
        } else if (oldUri != newUri) {
            PHOTO_CHANGED
        } else {
            PHOTO_UNCHANGED
        }
    }

    private fun insertNewContact(deleteCurrentContact: Boolean) {
        isSaving = true
        if (!deleteCurrentContact) {
            toast(R.string.inserting)
        }

        if (ContactsHelper(this@EditContactActivity).insertContact(contact!!)) {
            if (deleteCurrentContact) {
                contact!!.source = originalContactSource
                ContactsHelper(this).deleteContact(contact!!, false) {
                    setResult(Activity.RESULT_OK)
                    hideKeyboard()
                    finish()
                }
            } else {
                setResult(Activity.RESULT_OK)
                hideKeyboard()
                finish()
            }
        } else {
            toast(R.string.unknown_error_occurred)
        }
    }

    private fun setupAutoComplete(nameTextViews: List<MyAutoCompleteTextView>) {
        ContactsHelper(this).getContacts { contacts ->
            val adapter = AutoCompleteTextViewAdapter(this, contacts)
            val handler = Handler(mainLooper)
            nameTextViews.forEach { view ->
                view.setAdapter(adapter)
                view.setOnItemClickListener { _, _, position, _ ->
                    val selectedContact = adapter.resultList[position]

                    if (binding.contactFirstName.isVisible) {
                        binding.contactFirstName.setText(selectedContact.firstName)
                    }

                    if (binding.contactMiddleName.isVisible) {
                        binding.contactMiddleName.setText(selectedContact.middleName)
                    }

                    if (binding.contactSurname.isVisible) {
                        binding.contactSurname.setText(selectedContact.surname)
                    }
                }

                view.doAfterTextChanged {
                    handler.postDelayed({
                        adapter.autoComplete = true
                        adapter.filter.filter(it)
                    }, AUTO_COMPLETE_DELAY)
                }
            }
        }
    }

    private fun getFilledPhoneNumbers(): ArrayList<PhoneNumber> {
        val phoneNumbers = ArrayList<PhoneNumber>()
        val numbersCount = binding.contactNumbersHolder.childCount
        for (i in 0 until numbersCount) {
            val numberHolder = ItemEditPhoneNumberBinding.bind(binding.contactNumbersHolder.getChildAt(i))
            val number = numberHolder.contactNumber.value
            val numberType = getPhoneNumberTypeId(numberHolder.contactNumberType.value)
            val numberLabel =
                if (numberType == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) numberHolder.contactNumberType.value else ""

            if (number.isNotEmpty()) {
                var normalizedNumber = number.normalizePhoneNumber()

                // fix a glitch when onBackPressed the app thinks that a number changed because we fetched
                // normalized number +421903123456, then at getting it from the input field we get 0903123456, can happen at WhatsApp contacts
                val fetchedNormalizedNumber = numberHolder.contactNumber.tag?.toString() ?: ""
                if (PhoneNumberUtils.compare(number.normalizePhoneNumber(), fetchedNormalizedNumber)) {
                    normalizedNumber = fetchedNormalizedNumber
                }

                val isPrimary = numberHolder.defaultToggleIcon.tag == 1
                phoneNumbers.add(PhoneNumber(number, numberType, numberLabel, normalizedNumber, isPrimary))
            }
        }
        return phoneNumbers
    }

    private fun getFilledEmails(): ArrayList<Email> {
        val emails = ArrayList<Email>()
        val emailsCount = binding.contactEmailsHolder.childCount
        for (i in 0 until emailsCount) {
            val emailHolder = ItemEditEmailBinding.bind(binding.contactEmailsHolder.getChildAt(i))
            val email = emailHolder.contactEmail.value
            val emailType = getEmailTypeId(emailHolder.contactEmailType.value)
            val emailLabel =
                if (emailType == ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM) emailHolder.contactEmailType.value else ""

            if (email.isNotEmpty()) {
                emails.add(Email(email, emailType, emailLabel))
            }
        }
        return emails
    }

    private fun getEmailTypeId(value: String) = when (value) {
        getString(R.string.home) -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
        getString(R.string.work) -> ContactsContract.CommonDataKinds.Email.TYPE_WORK
        getString(R.string.mobile) -> ContactsContract.CommonDataKinds.Email.TYPE_MOBILE
        getString(R.string.other) -> ContactsContract.CommonDataKinds.Email.TYPE_OTHER
        else -> ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM
    }

    private fun getPhoneNumberTypeId(value: String) = when (value) {
        getString(R.string.mobile) -> ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
        getString(R.string.home) -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
        getString(R.string.work) -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK
        getString(R.string.main_number) -> ContactsContract.CommonDataKinds.Phone.TYPE_MAIN
        getString(R.string.work_fax) -> ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK
        getString(R.string.home_fax) -> ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME
        getString(R.string.pager) -> ContactsContract.CommonDataKinds.Phone.TYPE_PAGER
        getString(R.string.other) -> ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
        else -> ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM
    }

    private fun getEventTypeId(value: String) = when (value) {
        getString(R.string.anniversary) -> ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY
        getString(R.string.birthday) -> ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
        else -> ContactsContract.CommonDataKinds.Event.TYPE_OTHER
    }

    private fun getAddressTypeId(value: String) = when (value) {
        getString(R.string.home) -> ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
        getString(R.string.work) -> ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK
        getString(R.string.other) -> ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER
        else -> ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM
    }

    private fun getIMTypeId(value: String) = when (value) {
        getString(R.string.aim) -> ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM
        getString(R.string.windows_live) -> ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN
        getString(R.string.yahoo) -> ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO
        getString(R.string.skype) -> ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE
        getString(R.string.qq) -> ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ
        getString(R.string.hangouts) -> ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK
        getString(R.string.icq) -> ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ
        getString(R.string.jabber) -> ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER
        else -> ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM
    }

    private fun getFilledAddresses(): ArrayList<Address> {
        val addresses = ArrayList<Address>()
        val addressesCount = binding.contactAddressesHolder.childCount
        for (i in 0 until addressesCount) {
            val addressHolder = ItemEditAddressBinding.bind(binding.contactAddressesHolder.getChildAt(i))
            val address = addressHolder.contactAddress.value
            val addressType = getAddressTypeId(addressHolder.contactAddressType.value)
            val addressLabel =
                if (addressType == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM) addressHolder.contactAddressType.value else ""

            if (address.isNotEmpty()) {
                addresses.add(Address(address, addressType, addressLabel))
            }
        }
        return addresses
    }

    private fun getFilledIMs(): ArrayList<IM> {
        val listIm = ArrayList<IM>()
        val imsCount = binding.contactImsHolder.childCount
        for (i in 0 until imsCount) {
            val imsHolder = ItemEditImBinding.bind(binding.contactImsHolder.getChildAt(i))
            val im = imsHolder.contactIm.value
            val imType = getIMTypeId(imsHolder.contactImType.value)
            val imLabel = if (imType == ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM) imsHolder.contactImType.value else ""

            if (im.isNotEmpty()) {
                listIm.add(IM(im, imType, imLabel))
            }
        }
        return listIm
    }

    private fun getFilledEvents(): ArrayList<Event> {
        val unknown = getString(R.string.unknown)
        val events = ArrayList<Event>()
        val eventsCount = binding.contactEventsHolder.childCount
        for (i in 0 until eventsCount) {
            val eventHolder = ItemEventBinding.bind(binding.contactEventsHolder.getChildAt(i))
            val event = eventHolder.contactEvent.value
            val eventType = getEventTypeId(eventHolder.contactEventType.value)

            if (event.isNotEmpty() && event != unknown) {
                events.add(Event(eventHolder.contactEvent.tag.toString(), eventType))
            }
        }
        return events
    }

    private fun getFilledWebsites(): ArrayList<String> {
        val websites = ArrayList<String>()
        val websitesCount = binding.contactWebsitesHolder.childCount
        for (i in 0 until websitesCount) {
            val websiteHolder = ItemEditWebsiteBinding.bind(binding.contactWebsitesHolder.getChildAt(i))
            val website = websiteHolder.contactWebsite.value
            if (website.isNotEmpty()) {
                websites.add(website)
            }
        }
        return websites
    }

    private fun updateContact(photoUpdateStatus: Int, primaryState: Pair<PhoneNumber?, PhoneNumber?>) {
        isSaving = true
        if (ContactsHelper(this@EditContactActivity).updateContact(contact!!, photoUpdateStatus)) {
            val status = getPrimaryNumberStatus(primaryState.first, primaryState.second)
            if (status != PrimaryNumberStatus.UNCHANGED) {
                updateDefaultNumberForDuplicateContacts(primaryState, status) {
                    setResult(Activity.RESULT_OK)
                    hideKeyboard()
                    finish()
                }
            } else {
                setResult(Activity.RESULT_OK)
                hideKeyboard()
                finish()
            }
        } else {
            toast(R.string.unknown_error_occurred)
        }
    }

    private fun updateDefaultNumberForDuplicateContacts(
        toggleState: Pair<PhoneNumber?, PhoneNumber?>, primaryStatus: PrimaryNumberStatus, callback: () -> Unit
    ) {
        val contactsHelper = ContactsHelper(this)

        contactsHelper.getDuplicatesOfContact(contact!!, false) { contacts ->
            ensureBackgroundThread {
                val displayContactSources = getVisibleContactSources()
                contacts.filter { displayContactSources.contains(it.source) }.forEach { contact ->
                    val duplicate = contactsHelper.getContactWithId(contact.id, contact.isPrivate())
                    if (duplicate != null) {
                        if (primaryStatus == PrimaryNumberStatus.UNSTARRED) {
                            val number =
                                duplicate.phoneNumbers.find { it.normalizedNumber == toggleState.first!!.normalizedNumber }
                            number?.isPrimary = false
                        } else if (primaryStatus == PrimaryNumberStatus.STARRED) {
                            val number =
                                duplicate.phoneNumbers.find { it.normalizedNumber == toggleState.second!!.normalizedNumber }
                            if (number != null) {
                                duplicate.phoneNumbers.forEach {
                                    it.isPrimary = false
                                }
                                number.isPrimary = true
                            }
                        }

                        contactsHelper.updateContact(duplicate, PHOTO_UNCHANGED)
                    }
                }

                runOnUiThread {
                    callback.invoke()
                }
            }
        }
    }

    private fun getPrimaryNumberStatus(oldPrimary: PhoneNumber?, newPrimary: PhoneNumber?): PrimaryNumberStatus {
        return if (oldPrimary != null && newPrimary != null && oldPrimary != newPrimary) {
            PrimaryNumberStatus.STARRED
        } else if (oldPrimary == null && newPrimary != null) {
            PrimaryNumberStatus.STARRED
        } else if (oldPrimary != null && newPrimary == null) {
            PrimaryNumberStatus.UNSTARRED
        } else {
            PrimaryNumberStatus.UNCHANGED
        }
    }

    private fun parseIntentData(data: ArrayList<ContentValues>) {
        data.forEach {
            when (it.get(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE)) {
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> parseEmail(it)
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> parseAddress(it)
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> parseOrganization(it)
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE -> parseEvent(it)
                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE -> parseWebsite(it)
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE -> parseNote(it)
            }
        }
    }


    private fun parseEmail(contentValues: ContentValues) {
        val type = contentValues.getAsInteger(ContactsContract.CommonDataKinds.Email.DATA2) ?: DEFAULT_EMAIL_TYPE
        val emailValue = contentValues.getAsString(ContactsContract.CommonDataKinds.Email.DATA1) ?: return
        val email = Email(emailValue, type, "")
        contact!!.emails.add(email)
    }

    private fun parseAddress(contentValues: ContentValues) {
        val type = contentValues.getAsInteger(ContactsContract.CommonDataKinds.StructuredPostal.DATA2) ?: DEFAULT_ADDRESS_TYPE
        val addressValue =
            contentValues.getAsString(ContactsContract.CommonDataKinds.StructuredPostal.DATA4) ?: contentValues.getAsString(
                ContactsContract.CommonDataKinds.StructuredPostal.DATA1
            ) ?: return
        val address = Address(addressValue, type, "")
        contact!!.addresses.add(address)
    }

    private fun parseOrganization(contentValues: ContentValues) {
        val company = contentValues.getAsString(ContactsContract.CommonDataKinds.Organization.DATA1) ?: ""
        val jobPosition = contentValues.getAsString(ContactsContract.CommonDataKinds.Organization.DATA4) ?: ""
        contact!!.organization = Organization(company, jobPosition)
    }

    private fun parseEvent(contentValues: ContentValues) {
        val type = contentValues.getAsInteger(ContactsContract.CommonDataKinds.Event.DATA2) ?: DEFAULT_EVENT_TYPE
        val eventValue = contentValues.getAsString(ContactsContract.CommonDataKinds.Event.DATA1) ?: return
        val event = Event(eventValue, type)
        contact!!.events.add(event)
    }

    private fun parseWebsite(contentValues: ContentValues) {
        val website = contentValues.getAsString(ContactsContract.CommonDataKinds.Website.DATA1) ?: return
        contact!!.websites.add(website)
    }

    private fun parseNote(contentValues: ContentValues) {
        val note = contentValues.getAsString(ContactsContract.CommonDataKinds.Note.DATA1) ?: return
        contact!!.notes = note
    }

    private fun startTakePhotoIntent() {
        hideKeyboard()
        val uri = getCachePhotoUri()
        lastPhotoIntentUri = uri
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)

            try {
                startActivityForResult(this, INTENT_TAKE_PHOTO)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun startChoosePhotoIntent() {
        hideKeyboard()
        val uri = getCachePhotoUri()
        lastPhotoIntentUri = uri
        Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            clipData = ClipData("Attachment", arrayOf("text/uri-list"), ClipData.Item(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra(MediaStore.EXTRA_OUTPUT, uri)

            try {
                startActivityForResult(this, INTENT_CHOOSE_PHOTO)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (System.currentTimeMillis() - mLastSavePromptTS > SAVE_DISCARD_PROMPT_INTERVAL && hasContactChanged()) {
            mLastSavePromptTS = System.currentTimeMillis()
            ConfirmationAdvancedDialog(
                this, "", R.string.save_before_closing, R.string.save, R.string.discard
            ) {
                if (it) {
                    saveContact()
                } else {
                    super.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun startCropPhotoIntent(primaryUri: Uri?, backupUri: Uri?) {
        if (primaryUri == null) {
            toast(R.string.unknown_error_occurred)
            return
        }

        var imageUri = primaryUri
        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, primaryUri)
        if (bitmap == null) {
            imageUri = backupUri
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, backupUri) ?: return
            } catch (e: Exception) {
                showErrorToast(e)
                return
            }

            // we might have received an URI which we have no permission to send further, so just copy the received image in a new uri (for example from Google Photos)
            val newFile = getCachePhoto()
            val fos = newFile.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            imageUri = getCachePhotoUri(newFile)
        }

        hideKeyboard()
        lastPhotoIntentUri = getCachePhotoUri()
        Intent("com.android.camera.action.CROP").apply {
            setDataAndType(imageUri, "image/*")
            putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoIntentUri)
            putExtra("outputX", 512)
            putExtra("outputY", 512)
            putExtra("aspectX", 1)
            putExtra("aspectY", 1)
            putExtra("crop", "true")
            putExtra("scale", "true")
            putExtra("scaleUpIfNeeded", "true")
            clipData = ClipData("Attachment", arrayOf("text/primaryUri-list"), ClipData.Item(lastPhotoIntentUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            try {
                startActivityForResult(this, INTENT_CROP_PHOTO)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun setupPhoneNumbers() {
        val phoneNumbers = contact!!.phoneNumbers

        phoneNumbers.forEachIndexed { index, number ->
            val numberHolderView = binding.contactNumbersHolder.getChildAt(index)
            val numberHolder = if (numberHolderView == null) {
                ItemEditPhoneNumberBinding.inflate(layoutInflater, binding.contactNumbersHolder, false).apply {
                    binding.contactNumbersHolder.addView(root)
                }
            } else {
                ItemEditPhoneNumberBinding.bind(numberHolderView)
            }

            numberHolder.apply {
                contactNumber.setText(number.value)
                contactNumber.tag = number.normalizedNumber
                setupPhoneNumberTypePicker(contactNumberType, number.type, number.label)
                if (highlightLastPhoneNumber && index == phoneNumbers.size - 1) {
                    numberViewToColor = contactNumber
                }

                defaultToggleIcon.tag = if (number.isPrimary) 1 else 0
            }
        }

        initNumberHolders()
    }

    private fun setDefaultNumber(selected: ImageView) {
        val numbersCount = binding.contactNumbersHolder.childCount
        for (i in 0 until numbersCount) {
            val toggleIcon = ItemEditPhoneNumberBinding.bind(binding.contactNumbersHolder.getChildAt(i)).defaultToggleIcon
            if (toggleIcon != selected) {
                toggleIcon.tag = 0
            }
        }

        selected.tag = if (selected.tag == 1) 0 else 1

        initNumberHolders()
    }

    private fun initNumberHolders() {
        val numbersCount = binding.contactNumbersHolder.childCount

        if (numbersCount == 1) {
            ItemEditPhoneNumberBinding.bind(binding.contactNumbersHolder.getChildAt(0)).defaultToggleIcon.beGone()
            return
        }

        for (i in 0 until numbersCount) {
            val toggleIcon = ItemEditPhoneNumberBinding.bind(binding.contactNumbersHolder.getChildAt(i)).defaultToggleIcon
            val isPrimary = toggleIcon.tag == 1

            val drawableId = if (isPrimary) {
                R.drawable.ic_star_vector
            } else {
                R.drawable.ic_star_outline_vector
            }

            val drawable = ContextCompat.getDrawable(this@EditContactActivity, drawableId)
            drawable?.apply {
                mutate()
                setTint(getProperTextColor())
            }

            toggleIcon.setImageDrawable(drawable)
            toggleIcon.beVisible()
            toggleIcon.setOnClickListener {
                setDefaultNumber(toggleIcon)
            }
        }
    }

    private fun setupEmails() {
        contact!!.emails.forEachIndexed { index, email ->
            val emailHolderView = binding.contactEmailsHolder.getChildAt(index)
            val emailHolder = if (emailHolderView == null) {
                ItemEditEmailBinding.inflate(layoutInflater, binding.contactEmailsHolder, false).apply {
                    binding.contactEmailsHolder.addView(root)
                }
            } else {
                ItemEditEmailBinding.bind(emailHolderView)
            }

            emailHolder.apply {
                contactEmail.setText(email.value)
                setupEmailTypePicker(contactEmailType, email.type, email.label)
                if (highlightLastEmail && index == contact!!.emails.size - 1) {
                    emailViewToColor = contactEmail
                }
            }
        }
    }

    private fun setupAddresses() {
        contact?.addresses?.forEachIndexed { index, address ->
            val addressHolderView = binding.contactAddressesHolder.getChildAt(index)
            val addressHolder = if (addressHolderView == null) {
                ItemEditAddressBinding.inflate(layoutInflater, binding.contactAddressesHolder, false).apply {
                    binding.contactAddressesHolder.addView(root)
                }
            } else {
                ItemEditAddressBinding.bind(addressHolderView)
            }

            addressHolder.apply {
                contactAddress.setText(address.value)
                setupAddressTypePicker(contactAddressType, address.type, address.label)
            }
        }
    }

    private fun setupIMs() {
        contact!!.listIM.forEachIndexed { index, IM ->
            val imHolderView = binding.contactImsHolder.getChildAt(index)
            val imHolder = if (imHolderView == null) {
                ItemEditImBinding.inflate(layoutInflater, binding.contactImsHolder, false).apply {
                    binding.contactImsHolder.addView(root)
                }
            } else {
                ItemEditImBinding.bind(imHolderView)
            }

            imHolder.apply {
                contactIm.setText(IM.value)
                setupIMTypePicker(contactImType, IM.type, IM.label)
            }
        }
    }

    private fun setupNotes() {
        binding.contactNotes.setText(contact!!.notes)
    }

    private fun setupRingtone() {
        binding.contactRingtone.setOnClickListener {
            hideKeyboard()
            val ringtonePickerIntent = getRingtonePickerIntent()
            try {
                startActivityForResult(ringtonePickerIntent, INTENT_SELECT_RINGTONE)
            } catch (e: Exception) {
                val currentRingtone = contact!!.ringtone ?: getDefaultAlarmSound(RingtoneManager.TYPE_RINGTONE).uri
                SelectAlarmSoundDialog(this,
                    currentRingtone,
                    AudioManager.STREAM_RING,
                    PICK_RINGTONE_INTENT_ID,
                    RingtoneManager.TYPE_RINGTONE,
                    true,
                    onAlarmPicked = {
                        contact!!.ringtone = it?.uri
                        binding.contactRingtone.text = it?.title
                    },
                    onAlarmSoundDeleted = {})
            }
        }

        val ringtone = contact!!.ringtone
        if (ringtone?.isEmpty() == true) {
            binding.contactRingtone.text = getString(R.string.no_sound)
        } else if (ringtone?.isNotEmpty() == true) {
            if (ringtone == SILENT) {
                binding.contactRingtone.text = getString(R.string.no_sound)
            } else {
                systemRingtoneSelected(Uri.parse(ringtone))
            }
        } else {
            val default = getDefaultAlarmSound(RingtoneManager.TYPE_RINGTONE)
            binding.contactRingtone.text = default.title
        }
    }

    private fun setupOrganization() {
        binding.contactOrganizationCompany.setText(contact!!.organization.company)
        binding.contactOrganizationJobPosition.setText(contact!!.organization.jobPosition)
    }

    private fun setupWebsites() {
        contact!!.websites.forEachIndexed { index, website ->
            val websitesHolderView = binding.contactWebsitesHolder.getChildAt(index)
            val websitesHolder = if (websitesHolderView == null) {
                ItemEditWebsiteBinding.inflate(layoutInflater, binding.contactWebsitesHolder, false).apply {
                    binding.contactWebsitesHolder.addView(root)
                }
            } else {
                ItemEditWebsiteBinding.bind(websitesHolderView)
            }

            websitesHolder.contactWebsite.setText(website)
        }
    }

    private fun setupEvents() {
        contact!!.events.forEachIndexed { index, event ->
            val eventHolderView = binding.contactEventsHolder.getChildAt(index)
            val eventHolder = if (eventHolderView == null) {
                ItemEventBinding.inflate(layoutInflater, binding.contactEventsHolder, false).apply {
                    binding.contactEventsHolder.addView(root)
                }
            } else {
                ItemEventBinding.bind(eventHolderView)
            }

            eventHolder.apply {
                val contactEvent = contactEvent.apply {
                    event.value.getDateTimeFromDateString(true, this)
                    tag = event.value
                    alpha = 1f
                }

                setupEventTypePicker(this, event.type)

                contactEventRemove.apply {
                    beVisible()
                    applyColorFilter(getProperPrimaryColor())
                    background.applyColorFilter(getProperTextColor())
                    setOnClickListener {
                        resetContactEvent(contactEvent, this)
                    }
                }
            }
        }
    }

    private fun setupGroups() {
        binding.contactGroupsHolder.removeAllViews()
        val groups = contact!!.groups
        groups.forEachIndexed { index, group ->
            val groupHolderView = binding.contactGroupsHolder.getChildAt(index)
            val groupHolder = if (groupHolderView == null) {
                ItemEditGroupBinding.inflate(layoutInflater, binding.contactGroupsHolder, false).apply {
                    binding.contactGroupsHolder.addView(root)
                }
            } else {
                ItemEditGroupBinding.bind(groupHolderView)
            }

            groupHolder.apply {
                contactGroup.apply {
                    text = group.title
                    setTextColor(getProperTextColor())
                    tag = group.id
                    alpha = 1f
                }

                root.setOnClickListener {
                    showSelectGroupsDialog()
                }

                contactGroupRemove.apply {
                    beVisible()
                    applyColorFilter(getProperPrimaryColor())
                    background.applyColorFilter(getProperTextColor())
                    setOnClickListener {
                        removeGroup(group.id!!)
                    }
                }
            }
        }

        if (groups.isEmpty()) {
            ItemEditGroupBinding.inflate(layoutInflater, binding.contactGroupsHolder, false).apply {
                contactGroup.apply {
                    alpha = 0.5f
                    text = getString(R.string.no_groups)
                    setTextColor(getProperTextColor())
                }

                binding.contactGroupsHolder.addView(root)
                contactGroupRemove.beGone()
                root.setOnClickListener {
                    showSelectGroupsDialog()
                }
            }
        }
    }

    private fun setupContactSource() {
        originalContactSource = contact!!.source
        getPublicContactSource(contact!!.source) {
            binding.contactSource.text = if (it == "") getString(R.string.phone_storage) else it
        }
    }

    private fun setupTypePickers() {
        if (contact!!.phoneNumbers.isEmpty()) {
            val numberHolder = ItemEditPhoneNumberBinding.bind(binding.contactNumbersHolder.getChildAt(0))
            numberHolder.contactNumberType.apply {
                setupPhoneNumberTypePicker(this, DEFAULT_PHONE_NUMBER_TYPE, "")
            }
        }

        if (contact!!.emails.isEmpty()) {
            val emailHolder = ItemEditEmailBinding.bind(binding.contactEmailsHolder.getChildAt(0))
            emailHolder.contactEmailType.apply {
                setupEmailTypePicker(this, DEFAULT_EMAIL_TYPE, "")
            }
        }

        if (contact!!.addresses.isEmpty()) {
            val addressHolder = ItemEditAddressBinding.bind(binding.contactAddressesHolder.getChildAt(0))
            addressHolder.contactAddressType.apply {
                setupAddressTypePicker(this, DEFAULT_ADDRESS_TYPE, "")
            }
        }

        if (contact!!.listIM.isEmpty()) {
            val imHolder = ItemEditImBinding.bind(binding.contactImsHolder.getChildAt(0))
            imHolder.contactImType.apply {
                setupIMTypePicker(this, DEFAULT_IM_TYPE, "")
            }
        }

        if (contact!!.events.isEmpty()) {
            val eventHolder = ItemEventBinding.bind(binding.contactEventsHolder.getChildAt(0))
            eventHolder.apply {
                setupEventTypePicker(this)
            }
        }

        if (contact!!.groups.isEmpty()) {
            val groupsHolder = ItemEditGroupBinding.bind(binding.contactGroupsHolder.getChildAt(0))
            groupsHolder.contactGroup.apply {
                setupGroupsPicker(this)
            }
        }
    }

    private fun setupPhoneNumberTypePicker(numberTypeField: TextView, type: Int, label: String) {
        numberTypeField.apply {
            text = getPhoneNumberTypeText(type, label)
            setOnClickListener {
                showNumberTypePicker(it as TextView)
            }
        }
    }

    private fun setupEmailTypePicker(emailTypeField: TextView, type: Int, label: String) {
        emailTypeField.apply {
            text = getEmailTypeText(type, label)
            setOnClickListener {
                showEmailTypePicker(it as TextView)
            }
        }
    }

    private fun setupAddressTypePicker(addressTypeField: TextView, type: Int, label: String) {
        addressTypeField.apply {
            text = getAddressTypeText(type, label)
            setOnClickListener {
                showAddressTypePicker(it as TextView)
            }
        }
    }

    private fun setupIMTypePicker(imTypeField: TextView, type: Int, label: String) {
        imTypeField.apply {
            text = getIMTypeText(type, label)
            setOnClickListener {
                showIMTypePicker(it as TextView)
            }
        }
    }

    private fun setupEventTypePicker(eventHolder: ItemEventBinding, type: Int = DEFAULT_EVENT_TYPE) {
        eventHolder.contactEventType.apply {
            setText(getEventTextId(type))
            setOnClickListener {
                showEventTypePicker(it as TextView)
            }
        }

        val eventField = eventHolder.contactEvent
        eventField.setOnClickListener {
            MyDatePickerDialog(this, eventField.tag?.toString() ?: "") { dateTag ->
                eventField.apply {
                    dateTag.getDateTimeFromDateString(true, this)
                    tag = dateTag
                    alpha = 1f
                }
            }
        }

        eventHolder.contactEventRemove.apply {
            applyColorFilter(getProperPrimaryColor())
            background.applyColorFilter(getProperTextColor())
            setOnClickListener {
                resetContactEvent(eventField, this@apply)
            }
        }
    }

    private fun setupGroupsPicker(groupTitleField: TextView, group: Group? = null) {
        groupTitleField.apply {
            text = group?.title ?: getString(R.string.no_groups)
            alpha = if (group == null) 0.5f else 1f
            setOnClickListener {
                showSelectGroupsDialog()
            }
        }
    }

    private fun resetContactEvent(contactEvent: TextView, removeContactEventButton: ImageView) {
        contactEvent.apply {
            text = getString(R.string.unknown)
            tag = ""
            alpha = 0.5f
        }
        removeContactEventButton.beGone()
    }

    private fun removeGroup(id: Long) {
        contact?.groups = contact?.groups?.filter { it.id != id } as ArrayList<Group>
        setupGroups()
    }

    private fun showNumberTypePicker(numberTypeField: TextView) {
        val items = arrayListOf(
            RadioItem(
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, getString(R.string.mobile)
            ),
            RadioItem(ContactsContract.CommonDataKinds.Phone.TYPE_HOME, getString(R.string.home)),
            RadioItem(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, getString(R.string.work)),
            RadioItem(
                ContactsContract.CommonDataKinds.Phone.TYPE_MAIN, getString(R.string.main_number)
            ),
            RadioItem(
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK, getString(R.string.work_fax)
            ),
            RadioItem(
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME, getString(R.string.home_fax)
            ),
            RadioItem(ContactsContract.CommonDataKinds.Phone.TYPE_PAGER, getString(R.string.pager)),
            RadioItem(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER, getString(R.string.other)),
            RadioItem(
                ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM, getString(R.string.custom)
            )
        )

        val currentNumberTypeId = getPhoneNumberTypeId(numberTypeField.value)
        RadioGroupDialog(this, items, currentNumberTypeId) {
            if (it as Int == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                CustomLabelDialog(this) {
                    numberTypeField.text = it
                }
            } else {
                numberTypeField.text = getPhoneNumberTypeText(it, "")
            }
        }
    }

    private fun showEmailTypePicker(emailTypeField: TextView) {
        val items = arrayListOf(
            RadioItem(ContactsContract.CommonDataKinds.Email.TYPE_HOME, getString(R.string.home)),
            RadioItem(ContactsContract.CommonDataKinds.Email.TYPE_WORK, getString(R.string.work)),
            RadioItem(
                ContactsContract.CommonDataKinds.Email.TYPE_MOBILE, getString(R.string.mobile)
            ),
            RadioItem(ContactsContract.CommonDataKinds.Email.TYPE_OTHER, getString(R.string.other)),
            RadioItem(
                ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM, getString(R.string.custom)
            )
        )

        val currentEmailTypeId = getEmailTypeId(emailTypeField.value)
        RadioGroupDialog(this, items, currentEmailTypeId) {
            if (it as Int == ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM) {
                CustomLabelDialog(this) { str ->
                    emailTypeField.text = str
                }
            } else {
                emailTypeField.text = getEmailTypeText(it, "")
            }
        }
    }

    private fun getEmailTypeText(type: Int, label: String): String {
        return if (type == ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM) {
            label
        } else {
            getString(
                when (type) {
                    ContactsContract.CommonDataKinds.Email.TYPE_HOME -> R.string.home
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK -> R.string.work
                    ContactsContract.CommonDataKinds.Email.TYPE_MOBILE -> R.string.mobile
                    else -> R.string.other
                }
            )
        }
    }

    private fun showAddressTypePicker(addressTypeField: TextView) {
        val items = arrayListOf(
            RadioItem(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME, getString(R.string.home)
            ), RadioItem(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK, getString(R.string.work)
            ), RadioItem(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER, getString(R.string.other)
            ), RadioItem(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM, getString(R.string.custom)
            )
        )

        val currentAddressTypeId = getAddressTypeId(addressTypeField.value)
        RadioGroupDialog(this, items, currentAddressTypeId) {
            if (it as Int == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM) {
                CustomLabelDialog(this) { str ->
                    addressTypeField.text = str
                }
            } else {
                addressTypeField.text = getAddressTypeText(it, "")
            }
        }
    }

    private fun getAddressTypeText(type: Int, label: String): String {
        return if (type == ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM) {
            label
        } else {
            getString(
                when (type) {
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> R.string.home
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> R.string.work
                    else -> R.string.other
                }
            )
        }
    }

    private fun showIMTypePicker(imTypeField: TextView) {
        val items = arrayListOf(
            RadioItem(ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM, getString(R.string.aim)),
            RadioItem(ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN, getString(R.string.windows_live)),
            RadioItem(ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO, getString(R.string.yahoo)),
            RadioItem(ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE, getString(R.string.skype)),
            RadioItem(ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ, getString(R.string.qq)),
            RadioItem(ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK, getString(R.string.hangouts)),
            RadioItem(ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ, getString(R.string.icq)),
            RadioItem(ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER, getString(R.string.jabber)),
            RadioItem(
                ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM, getString(R.string.custom)
            )
        )

        val currentIMTypeId = getIMTypeId(imTypeField.value)
        RadioGroupDialog(this, items, currentIMTypeId) {
            if (it as Int == ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM) {
                CustomLabelDialog(this) {
                    imTypeField.text = it
                }
            } else {
                imTypeField.text = getIMTypeText(it, "")
            }
        }
    }

    fun getIMTypeText(type: Int, label: String): String {
        return if (type == ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM) {
            label
        } else {
            getString(
                when (type) {
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM -> R.string.aim
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN -> R.string.windows_live
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO -> R.string.yahoo
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE -> R.string.skype
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ -> R.string.qq
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK -> R.string.hangouts
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ -> R.string.icq
                    else -> R.string.jabber
                }
            )
        }
    }

    private fun showEventTypePicker(eventTypeField: TextView) {
        val items = arrayListOf(
            RadioItem(
                ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY, getString(R.string.anniversary)
            ), RadioItem(
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY, getString(R.string.birthday)
            ), RadioItem(ContactsContract.CommonDataKinds.Event.TYPE_OTHER, getString(R.string.other))
        )

        val currentEventTypeId = getEventTypeId(eventTypeField.value)
        RadioGroupDialog(this, items, currentEventTypeId) {
            eventTypeField.setText(getEventTextId(it as Int))
        }
    }

    fun getEventTextId(type: Int) = when (type) {
        ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY -> R.string.anniversary
        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY -> R.string.birthday
        else -> R.string.other
    }

    private fun showSelectGroupsDialog() {
        SelectGroupsDialog(this@EditContactActivity, contact!!.groups) {
            contact!!.groups = it
            setupGroups()
        }
    }

    private fun showSelectContactSourceDialog() {
        showContactSourcePicker(contact!!.source) {
            contact!!.source = if (it == getString(R.string.phone_storage_hidden)) SMT_PRIVATE else it
            getPublicContactSource(it) { str ->
                binding.contactSource.text = if (str == "") getString(R.string.phone_storage) else str
            }
        }
    }

    private fun addNewPhoneNumberField() {
        val numberHolder = ItemEditPhoneNumberBinding.inflate(layoutInflater, binding.contactNumbersHolder, false)
        updateTextColors(numberHolder.root)
        setupPhoneNumberTypePicker(numberHolder.contactNumberType, DEFAULT_PHONE_NUMBER_TYPE, "")
        binding.contactNumbersHolder.addView(numberHolder.root)
        binding.contactNumbersHolder.onGlobalLayout {
            numberHolder.contactNumber.requestFocus()
            showKeyboard(numberHolder.contactNumber)
        }
        numberHolder.defaultToggleIcon.tag = 0
        initNumberHolders()
    }

    private fun addNewEmailField() {
        val emailHolder = ItemEditEmailBinding.inflate(layoutInflater, binding.contactEmailsHolder, false)
        updateTextColors(emailHolder.root)
        setupEmailTypePicker(emailHolder.contactEmailType, DEFAULT_EMAIL_TYPE, "")
        binding.contactEmailsHolder.addView(emailHolder.root)
        binding.contactEmailsHolder.onGlobalLayout {
            emailHolder.contactEmail.requestFocus()
            showKeyboard(emailHolder.contactEmail)
        }
    }

    private fun addNewAddressField() {
        val addressHolder = ItemEditAddressBinding.inflate(layoutInflater, binding.contactAddressesHolder, false)
        updateTextColors(addressHolder.root)
        setupAddressTypePicker(addressHolder.contactAddressType, DEFAULT_ADDRESS_TYPE, "")
        binding.contactAddressesHolder.addView(addressHolder.root)
        binding.contactAddressesHolder.onGlobalLayout {
            addressHolder.contactAddress.requestFocus()
            showKeyboard(addressHolder.contactAddress)
        }
    }

    private fun addNewIMField() {
        val IMHolder = ItemEditImBinding.inflate(layoutInflater, binding.contactImsHolder, false)
        updateTextColors(IMHolder.root)
        setupIMTypePicker(IMHolder.contactImType, DEFAULT_IM_TYPE, "")
        binding.contactImsHolder.addView(IMHolder.root)
        binding.contactImsHolder.onGlobalLayout {
            IMHolder.contactIm.requestFocus()
            showKeyboard(IMHolder.contactIm)
        }
    }

    private fun addNewEventField() {
        val eventHolder = ItemEventBinding.inflate(layoutInflater, binding.contactEventsHolder, false)
        updateTextColors(eventHolder.root)
        setupEventTypePicker(eventHolder)
        binding.contactEventsHolder.addView(eventHolder.root)
    }

    private fun toggleFavorite() {
        val isStarred = isContactStarred()
        binding.contactToggleFavorite.apply {
            setImageDrawable(getStarDrawable(!isStarred))
            tag = if (isStarred) 0 else 1

            setOnLongClickListener { toast(R.string.toggle_favorite); true; }
        }
    }

    private fun addNewWebsiteField() {
        val websitesHolder = ItemEditWebsiteBinding.inflate(layoutInflater, binding.contactWebsitesHolder, false)
        updateTextColors(websitesHolder.root)
        binding.contactWebsitesHolder.addView(websitesHolder.root)
        binding.contactWebsitesHolder.onGlobalLayout {
            websitesHolder.contactWebsite.requestFocus()
            showKeyboard(websitesHolder.contactWebsite)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getStarDrawable(on: Boolean) =
        resources.getDrawable(if (on) R.drawable.ic_star_vector else R.drawable.ic_star_outline_vector)

    private fun trySetPhoto() {
        val items = arrayListOf(
            RadioItem(TAKE_PHOTO, getString(R.string.take_photo)), RadioItem(CHOOSE_PHOTO, getString(R.string.choose_photo))
        )

        if (currentContactPhotoPath.isNotEmpty() || contact!!.photo != null) {
            items.add(RadioItem(REMOVE_PHOTO, getString(R.string.remove_photo)))
        }

        RadioGroupDialog(this, items) {
            when (it as Int) {
                TAKE_PHOTO -> startTakePhotoIntent()
                CHOOSE_PHOTO -> startChoosePhotoIntent()
                else -> {
                    showPhotoPlaceholder(binding.contactPhoto)
                    binding.contactPhotoBottomShadow.beGone()
                }
            }
        }
    }

    private fun showPhotoPlaceholder(photoView: ImageView) {
        val placeholder = BitmapDrawable(resources, getBigLetterPlaceholder(contact?.getNameToDisplay() ?: "A"))
        photoView.setImageDrawable(placeholder)
        currentContactPhotoPath = ""
        contact?.photo = null
    }

    private fun getBigLetterPlaceholder(name: String): Bitmap {
        val letter = name.getNameLetter()
        val height = resources.getDimension(R.dimen.top_contact_image_height).toInt()
        val bitmap = Bitmap.createBitmap(realScreenSize.x, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val view = TextView(this)
        view.layout(0, 0, bitmap.width, bitmap.height)

        val circlePaint = Paint().apply {
            color = letterBackgroundColors[Math.abs(name.hashCode()) % letterBackgroundColors.size].toInt()
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val wantedTextSize = bitmap.height / 2f
        val textPaint = Paint().apply {
            color = circlePaint.color.getContrastColor()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = wantedTextSize
            style = Paint.Style.FILL
        }

        canvas.drawPaint(circlePaint)

        val xPos = canvas.width / 2f
        val yPos = canvas.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(letter, xPos, yPos, textPaint)
        view.draw(canvas)
        return bitmap
    }

    private fun updateContactPhoto(path: String, photoView: ImageView, bottomShadow: ImageView, bitmap: Bitmap? = null) {
        currentContactPhotoPath = path

        if (isDestroyed || isFinishing) {
            return
        }

        val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()

        val wantedWidth = realScreenSize.x
        val wantedHeight = resources.getDimension(R.dimen.top_contact_image_height).toInt()

        Glide.with(this).load(bitmap ?: path).transition(DrawableTransitionOptions.withCrossFade()).apply(options)
            .override(wantedWidth, wantedHeight).listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean
                ): Boolean {
                    photoView.background = ColorDrawable(0)
                    bottomShadow.beVisible()
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
                ): Boolean {
                    showPhotoPlaceholder(photoView)
                    bottomShadow.beGone()
                    return true
                }
            }).into(photoView)
    }

    private fun getDefaultRingtoneUri() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

    private fun getRingtonePickerIntent(): Intent {
        val defaultRingtoneUri = getDefaultRingtoneUri()
        val currentRingtoneUri = if (contact!!.ringtone != null && contact!!.ringtone!!.isNotEmpty()) {
            Uri.parse(contact!!.ringtone)
        } else if (contact!!.ringtone?.isNotEmpty() == false) {
            null
        } else {
            defaultRingtoneUri
        }

        return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultRingtoneUri)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingtoneUri)
        }
    }
}