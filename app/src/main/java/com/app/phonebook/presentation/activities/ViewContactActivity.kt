package com.app.phonebook.presentation.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentUris
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
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.app.phonebook.R
import com.app.phonebook.base.extension.applyColorFilter
import com.app.phonebook.base.extension.beGone
import com.app.phonebook.base.extension.beGoneIf
import com.app.phonebook.base.extension.beInvisibleIf
import com.app.phonebook.base.extension.beVisible
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.copyToClipboard
import com.app.phonebook.base.extension.editContact
import com.app.phonebook.base.extension.getContactPublicUri
import com.app.phonebook.base.extension.getContactUriRawId
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getDateTimeFromDateString
import com.app.phonebook.base.extension.getDefaultAlarmSound
import com.app.phonebook.base.extension.getFilenameFromPath
import com.app.phonebook.base.extension.getLookupKeyFromUri
import com.app.phonebook.base.extension.getLookupUriRawId
import com.app.phonebook.base.extension.getNameLetter
import com.app.phonebook.base.extension.getPackageDrawable
import com.app.phonebook.base.extension.getPhoneNumberTypeText
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.extension.getPublicContactSourceSync
import com.app.phonebook.base.extension.getSocialActions
import com.app.phonebook.base.extension.getVisibleContactSources
import com.app.phonebook.base.extension.isGone
import com.app.phonebook.base.extension.isVisible
import com.app.phonebook.base.extension.launchSendSMSIntent
import com.app.phonebook.base.extension.launchViewContactIntent
import com.app.phonebook.base.extension.openWebsiteIntent
import com.app.phonebook.base.extension.realScreenSize
import com.app.phonebook.base.extension.sendAddressIntent
import com.app.phonebook.base.extension.sendEmailIntent
import com.app.phonebook.base.extension.shareContacts
import com.app.phonebook.base.extension.showErrorToast
import com.app.phonebook.base.extension.statusBarHeight
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.extension.tryInitiateCall
import com.app.phonebook.base.extension.updateTextColors
import com.app.phonebook.base.extension.value
import com.app.phonebook.base.utils.CONTACT_ID
import com.app.phonebook.base.utils.IS_PRIVATE
import com.app.phonebook.base.utils.PERMISSION_CALL_PHONE
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.base.utils.SHOW_ADDRESSES_FIELD
import com.app.phonebook.base.utils.SHOW_CONTACT_SOURCE_FIELD
import com.app.phonebook.base.utils.SHOW_EMAILS_FIELD
import com.app.phonebook.base.utils.SHOW_EVENTS_FIELD
import com.app.phonebook.base.utils.SHOW_FIRST_NAME_FIELD
import com.app.phonebook.base.utils.SHOW_GROUPS_FIELD
import com.app.phonebook.base.utils.SHOW_IMS_FIELD
import com.app.phonebook.base.utils.SHOW_MIDDLE_NAME_FIELD
import com.app.phonebook.base.utils.SHOW_NOTES_FIELD
import com.app.phonebook.base.utils.SHOW_ORGANIZATION_FIELD
import com.app.phonebook.base.utils.SHOW_PHONE_NUMBERS_FIELD
import com.app.phonebook.base.utils.SHOW_PREFIX_FIELD
import com.app.phonebook.base.utils.SHOW_RINGTONE_FIELD
import com.app.phonebook.base.utils.SHOW_SUFFIX_FIELD
import com.app.phonebook.base.utils.SHOW_SURNAME_FIELD
import com.app.phonebook.base.utils.SHOW_WEBSITES_FIELD
import com.app.phonebook.base.utils.SIGNAL
import com.app.phonebook.base.utils.SIGNAL_PACKAGE
import com.app.phonebook.base.utils.SILENT
import com.app.phonebook.base.utils.TELEGRAM
import com.app.phonebook.base.utils.TELEGRAM_PACKAGE
import com.app.phonebook.base.utils.THREEMA
import com.app.phonebook.base.utils.THREEMA_PACKAGE
import com.app.phonebook.base.utils.VIBER
import com.app.phonebook.base.utils.VIBER_PACKAGE
import com.app.phonebook.base.utils.WHATSAPP
import com.app.phonebook.base.utils.WHATSAPP_PACKAGE
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.utils.letterBackgroundColors
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Address
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.ContactSource
import com.app.phonebook.data.models.Event
import com.app.phonebook.data.models.Group
import com.app.phonebook.data.models.IM
import com.app.phonebook.data.models.PhoneNumber
import com.app.phonebook.data.models.RadioItem
import com.app.phonebook.databinding.ActivityViewContactBinding
import com.app.phonebook.databinding.ItemViewAddressBinding
import com.app.phonebook.databinding.ItemViewContactSourceBinding
import com.app.phonebook.databinding.ItemViewEmailBinding
import com.app.phonebook.databinding.ItemViewEventBinding
import com.app.phonebook.databinding.ItemViewGroupBinding
import com.app.phonebook.databinding.ItemViewImBinding
import com.app.phonebook.databinding.ItemViewPhoneNumberBinding
import com.app.phonebook.databinding.ItemWebsiteBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.LocalContactsHelper
import com.app.phonebook.presentation.dialog.CallConfirmationDialog
import com.app.phonebook.presentation.dialog.ChooseSocialDialog
import com.app.phonebook.presentation.dialog.ConfirmationDialog
import com.app.phonebook.presentation.dialog.ManageVisibleFieldsDialog
import com.app.phonebook.presentation.dialog.RadioGroupDialog
import com.app.phonebook.presentation.dialog.SelectAlarmSoundDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.util.Locale

class ViewContactActivity : BaseActivity<ActivityViewContactBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityViewContactBinding {
        return ActivityViewContactBinding.inflate(inflater)
    }


    private var contact: Contact? = null
    private var currentContactPhotoPath = ""

    private var isViewIntent = false
    private var wasEditLaunched = false
    private var duplicateContacts = ArrayList<Contact>()
    private var contactSources = ArrayList<ContactSource>()
    private var showFields = 0
    private var fullContact: Contact? = null    // contact with all fields filled from duplicates
    private var duplicateInitialized = false
    private val mergeDuplicate: Boolean get() = config.mergeDuplicateContacts

    companion object {
        private const val COMPARABLE_PHONE_NUMBER_LENGTH = 9
        private const val PICK_RINGTONE_INTENT_ID = 1500
        private const val INTENT_SELECT_RINGTONE = 600
    }

    @Suppress("DEPRECATION")
    override fun initView(savedInstanceState: Bundle?) {
        showFields = config.showContactFields
        binding.contactWrapper.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setupMenu()
    }

    override fun beforeCreate() {
        showTransparentTop = true
    }

    override fun onResume() {
        super.onResume()
        isViewIntent = intent.action == ContactsContract.QuickContact.ACTION_QUICK_CONTACT || intent.action == Intent.ACTION_VIEW
        if (isViewIntent) {
            handlePermission(PERMISSION_READ_CONTACTS) {
                if (it) {
                    ensureBackgroundThread {
                        initContact()
                    }
                } else {
                    toast(R.string.no_contacts_permission)
                    finish()
                }
            }
        } else {
            ensureBackgroundThread {
                initContact()
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.contactPhotoBig.alpha == 1f) {
            hideBigContactPhoto()
        } else {
            super.onBackPressed()
        }
    }

    private fun shareContact(contact: Contact) {
        shareContacts(arrayListOf(contact))
    }


    private fun setupMenu() {
        (binding.contactAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin = statusBarHeight
        binding.contactToolbar.menu.apply {
            findItem(R.id.share).setOnMenuItemClickListener {
                if (fullContact != null) {
                    shareContact(fullContact!!)
                }
                true
            }

            findItem(R.id.edit).setOnMenuItemClickListener {
                if (contact != null) {
                    launchEditContact(contact!!)
                }
                true
            }

            findItem(R.id.delete).setOnMenuItemClickListener {
                deleteContactFromAllSources()
                true
            }

            findItem(R.id.manage_visible_fields).setOnMenuItemClickListener {
                ManageVisibleFieldsDialog(this@ViewContactActivity) {
                    showFields = config.showContactFields
                    ensureBackgroundThread {
                        initContact()
                    }
                }
                true
            }
        }

        binding.contactToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initContact() {
        var wasLookupKeyUsed = false
        var contactId: Int
        try {
            contactId = intent.getIntExtra(CONTACT_ID, 0)
        } catch (e: Exception) {
            return
        }

        if (contactId == 0 && isViewIntent) {
            val data = intent.data
            if (data != null) {
                val rawId = if (data.path!!.contains("lookup")) {
                    val lookupKey = getLookupKeyFromUri(data)
                    if (lookupKey != null) {
                        contact = ContactsHelper(this).getContactWithLookupKey(lookupKey)
                        fullContact = contact
                        wasLookupKeyUsed = true
                    }

                    getLookupUriRawId(data)
                } else {
                    getContactUriRawId(data)
                }

                if (rawId != -1) {
                    contactId = rawId
                }
            }
        }

        if (contactId != 0 && !wasLookupKeyUsed) {
            contact = ContactsHelper(this).getContactWithId(contactId, intent.getBooleanExtra(IS_PRIVATE, false))
            fullContact = contact

            if (contact == null) {
                if (!wasEditLaunched) {
                    toast(R.string.unknown_error_occurred)
                }
                finish()
            } else {
                runOnUiThread {
                    gotContact()
                }
            }
        } else {
            if (contact == null) {
                finish()
            } else {
                runOnUiThread {
                    gotContact()
                }
            }
        }
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

    private fun showPhotoPlaceholder(photoView: ImageView) {
        val placeholder = BitmapDrawable(resources, getBigLetterPlaceholder(contact?.getNameToDisplay() ?: "A"))
        photoView.setImageDrawable(placeholder)
        currentContactPhotoPath = ""
        contact?.photo = null
    }


    private fun updateContactPhoto(path: String, photoView: ImageView, bottomShadow: ImageView, bitmap: Bitmap? = null) {
        currentContactPhotoPath = path

        if (isDestroyed || isFinishing) {
            return
        }

        val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .centerCrop()

        val wantedWidth = realScreenSize.x
        val wantedHeight = resources.getDimension(R.dimen.top_contact_image_height).toInt()

        Glide.with(this)
            .load(bitmap ?: path)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(options)
            .override(wantedWidth, wantedHeight)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    photoView.background = ColorDrawable(0)
                    bottomShadow.beVisible()
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    showPhotoPlaceholder(photoView)
                    bottomShadow.beGone()
                    return true
                }
            }).into(photoView)
    }

    private fun gotContact() {
        if (isDestroyed || isFinishing) {
            return
        }

        binding.contactScrollview.beVisible()
        setupViewContact()
        binding.contactSendSms.beVisibleIf(contact!!.phoneNumbers.isNotEmpty())
        binding.contactStartCall.beVisibleIf(contact!!.phoneNumbers.isNotEmpty())
        binding.contactSendEmail.beVisibleIf(contact!!.emails.isNotEmpty())

        if (contact!!.photoUri.isEmpty() && contact!!.photo == null) {
            showPhotoPlaceholder(binding.contactPhoto)
            binding.contactPhotoBottomShadow.beGone()
        } else {
            updateContactPhoto(contact!!.photoUri, binding.contactPhoto, binding.contactPhotoBottomShadow, contact!!.photo)
            val options = RequestOptions()
                .transform(FitCenter(), RoundedCorners(resources.getDimension(R.dimen.normal_margin).toInt()))

            Glide.with(this)
                .load(contact!!.photo ?: currentContactPhotoPath)
                .apply(options)
                .into(binding.contactPhotoBig)

            binding.contactPhoto.setOnClickListener {
                binding.contactPhotoBig.alpha = 0f
                binding.contactPhotoBig.beVisible()
                binding.contactPhotoBig.animate().alpha(1f).start()
            }

            binding.contactPhotoBig.setOnClickListener {
                hideBigContactPhoto()
            }
        }

        val textColor = getProperTextColor()
        arrayOf(
            binding.contactNameImage,
            binding.contactNumbersImage,
            binding.contactEmailsImage,
            binding.contactAddressesImage,
            binding.contactImsImage,
            binding.contactEventsImage,
            binding.contactSourceImage,
            binding.contactNotesImage,
            binding.contactRingtoneImage,
            binding.contactOrganizationImage,
            binding.contactWebsitesImage,
            binding.contactGroupsImage
        ).forEach {
            it.applyColorFilter(textColor)
        }

        binding.contactSendSms.setOnClickListener { trySendSMS() }
        binding.contactStartCall.setOnClickListener { tryInitiateCall(contact!!) { startCallIntent(it) } }
        binding.contactSendEmail.setOnClickListener { trySendEmail() }

        binding.contactSendSms.setOnLongClickListener { toast(R.string.send_sms); true; }
        binding.contactStartCall.setOnLongClickListener { toast(R.string.call_contact); true; }
        binding.contactSendEmail.setOnLongClickListener { toast(R.string.send_email); true; }

        updateTextColors(binding.contactScrollview)
    }

    private fun setupViewContact() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setupFavorite()
        setupNames()

        ContactsHelper(this).getContactSources {
            contactSources = it
            runOnUiThread {
                setupContactDetails()
            }
        }

        getDuplicateContacts {
            duplicateInitialized = true
            setupContactDetails()
        }
    }

    private fun setupContactDetails() {
        if (isFinishing || isDestroyed || contact == null) {
            return
        }

        setupPhoneNumbers()
        setupEmails()
        setupAddresses()
        setupIMs()
        setupEvents()
        setupWebsites()
        setupGroups()
        setupContactSources()
        setupNotes()
        setupRingtone()
        setupOrganization()
        updateTextColors(binding.contactScrollview)
    }

    private fun launchEditContact(contact: Contact) {
        wasEditLaunched = true
        duplicateInitialized = false
        editContact(contact)
    }

    private fun openWith() {
        if (contact != null) {
            val uri = getContactPublicUri(contact!!)
            launchViewContactIntent(uri)
        }
    }

    private fun setupFavorite() {
        binding.contactToggleFavorite.apply {
            beVisible()
            tag = contact!!.starred
            setImageDrawable(getStarDrawable(tag == 1))

            setOnClickListener {
                val newIsStarred = if (tag == 1) 0 else 1
                ensureBackgroundThread {
                    val contacts = arrayListOf(contact!!)
                    if (newIsStarred == 1) {
                        ContactsHelper(context).addFavorites(contacts)
                    } else {
                        ContactsHelper(context).removeFavorites(contacts)
                    }
                }
                contact!!.starred = newIsStarred
                tag = contact!!.starred
                setImageDrawable(getStarDrawable(tag == 1))
            }

            setOnLongClickListener { toast(R.string.toggle_favorite); true; }
        }
    }

    private fun setupNames() {
        var displayName = contact!!.getNameToDisplay()
        if (contact!!.nickname.isNotEmpty()) {
            displayName += " (${contact!!.nickname})"
        }

        val showNameFields =
            showFields and SHOW_PREFIX_FIELD != 0 || showFields and SHOW_FIRST_NAME_FIELD != 0 || showFields and SHOW_MIDDLE_NAME_FIELD != 0 ||
                    showFields and SHOW_SURNAME_FIELD != 0 || showFields and SHOW_SUFFIX_FIELD != 0

        binding.contactName.text = displayName
        binding.contactName.copyOnLongClick(displayName)
        binding.contactName.beVisibleIf(displayName.isNotEmpty() && !contact!!.isABusinessContact() && showNameFields)
        binding.contactNameImage.beInvisibleIf(binding.contactName.isGone())
    }

    private fun setupPhoneNumbers() {
        var phoneNumbers = contact!!.phoneNumbers.toMutableSet() as LinkedHashSet<PhoneNumber>

        if (mergeDuplicate) {
            duplicateContacts.forEach {
                phoneNumbers.addAll(it.phoneNumbers)
            }
        }

        if (duplicateInitialized) {
            val contactDefaultsNumbers = contact!!.phoneNumbers.filter { it.isPrimary }
            val duplicateContactsDefaultNumbers = duplicateContacts.flatMap { it.phoneNumbers }.filter { it.isPrimary }
            val defaultNumbers = (contactDefaultsNumbers + duplicateContactsDefaultNumbers).toSet()

            if (defaultNumbers.size > 1 && defaultNumbers.distinctBy { it.normalizedNumber }.size > 1) {
                phoneNumbers.forEach { it.isPrimary = false }
            } else if (defaultNumbers.size == 1) {
                if (mergeDuplicate) {
                    val defaultNumber = defaultNumbers.first()
                    val candidate = phoneNumbers.find { it.normalizedNumber == defaultNumber.normalizedNumber && !it.isPrimary }
                    candidate?.isPrimary = true
                } else {
                    duplicateContactsDefaultNumbers.forEach { defaultNumber ->
                        val candidate =
                            phoneNumbers.find { it.normalizedNumber == defaultNumber.normalizedNumber && !it.isPrimary }
                        candidate?.isPrimary = true
                    }
                }
            }
        }

        phoneNumbers = phoneNumbers.distinctBy {
            if (it.normalizedNumber.length >= COMPARABLE_PHONE_NUMBER_LENGTH) {
                it.normalizedNumber.substring(it.normalizedNumber.length - COMPARABLE_PHONE_NUMBER_LENGTH)
            } else {
                it.normalizedNumber
            }
        }.toMutableSet() as LinkedHashSet<PhoneNumber>

        phoneNumbers = phoneNumbers.sortedBy { it.type }.toMutableSet() as LinkedHashSet<PhoneNumber>
        fullContact!!.phoneNumbers = phoneNumbers.toMutableList() as ArrayList<PhoneNumber>
        binding.contactNumbersHolder.removeAllViews()

        if (phoneNumbers.isNotEmpty() && showFields and SHOW_PHONE_NUMBERS_FIELD != 0) {
            phoneNumbers.forEach { phoneNumber ->
                ItemViewPhoneNumberBinding.inflate(layoutInflater, binding.contactNumbersHolder, false).apply {
                    binding.contactNumbersHolder.addView(root)
                    contactNumber.text = phoneNumber.value
                    contactNumberType.text = getPhoneNumberTypeText(phoneNumber.type, phoneNumber.label)
                    root.copyOnLongClick(phoneNumber.value)

                    root.setOnClickListener {
                        if (config.showCallConfirmation) {
                            CallConfirmationDialog(this@ViewContactActivity, phoneNumber.value) {
                                startCallIntent(phoneNumber.value)
                            }
                        } else {
                            startCallIntent(phoneNumber.value)
                        }
                    }

                    defaultToggleIcon.isVisible = phoneNumber.isPrimary
                }
            }
            binding.contactNumbersImage.beVisible()
            binding.contactNumbersHolder.beVisible()
        } else {
            binding.contactNumbersImage.beGone()
            binding.contactNumbersHolder.beGone()
        }

        // make sure the Call and SMS buttons are visible if any phone number is shown
        if (phoneNumbers.isNotEmpty()) {
            binding.contactSendSms.beVisible()
            binding.contactStartCall.beVisible()
        }
    }

    // a contact cannot have different emails per contact source. Such contacts are handled as separate ones, not duplicates of each other
    private fun setupEmails() {
        binding.contactEmailsHolder.removeAllViews()
        val emails = contact!!.emails
        if (emails.isNotEmpty() && showFields and SHOW_EMAILS_FIELD != 0) {
            emails.forEach {
                ItemViewEmailBinding.inflate(layoutInflater, binding.contactEmailsHolder, false).apply {
                    val email = it
                    binding.contactEmailsHolder.addView(root)
                    contactEmail.text = email.value
                    contactEmailType.text = getEmailTypeText(email.type, email.label)
                    root.copyOnLongClick(email.value)

                    root.setOnClickListener {
                        sendEmailIntent(email.value)
                    }
                }
            }
            binding.contactEmailsImage.beVisible()
            binding.contactEmailsHolder.beVisible()
        } else {
            binding.contactEmailsImage.beGone()
            binding.contactEmailsHolder.beGone()
        }
    }

    private fun setupAddresses() {
        var addresses = contact!!.addresses.toMutableSet() as LinkedHashSet<Address>

        if (mergeDuplicate) {
            duplicateContacts.forEach {
                addresses.addAll(it.addresses)
            }
        }

        addresses = addresses.sortedBy { it.type }.toMutableSet() as LinkedHashSet<Address>
        fullContact!!.addresses = addresses.toMutableList() as ArrayList<Address>
        binding.contactAddressesHolder.removeAllViews()

        if (addresses.isNotEmpty() && showFields and SHOW_ADDRESSES_FIELD != 0) {
            addresses.forEach {
                ItemViewAddressBinding.inflate(layoutInflater, binding.contactAddressesHolder, false).apply {
                    val address = it
                    binding.contactAddressesHolder.addView(root)
                    contactAddress.text = address.value
                    contactAddressType.text = getAddressTypeText(address.type, address.label)
                    root.copyOnLongClick(address.value)

                    root.setOnClickListener {
                        sendAddressIntent(address.value)
                    }
                }
            }
            binding.contactAddressesImage.beVisible()
            binding.contactAddressesHolder.beVisible()
        } else {
            binding.contactAddressesImage.beGone()
            binding.contactAddressesHolder.beGone()
        }
    }

    private fun setupIMs() {
        var IMs = contact!!.listIM.toMutableSet() as LinkedHashSet<IM>

        if (mergeDuplicate) {
            duplicateContacts.forEach {
                IMs.addAll(it.listIM)
            }
        }

        IMs = IMs.sortedBy { it.type }.toMutableSet() as LinkedHashSet<IM>
        fullContact!!.listIM = IMs.toMutableList() as ArrayList<IM>
        binding.contactImsHolder.removeAllViews()

        if (IMs.isNotEmpty() && showFields and SHOW_IMS_FIELD != 0) {
            IMs.forEach {
                ItemViewImBinding.inflate(layoutInflater, binding.contactImsHolder, false).apply {
                    binding.contactImsHolder.addView(root)
                    contactIm.text = it.value
                    contactImType.text = getIMTypeText(it.type, it.label)
                    root.copyOnLongClick(it.value)
                }
            }
            binding.contactImsImage.beVisible()
            binding.contactImsHolder.beVisible()
        } else {
            binding.contactImsImage.beGone()
            binding.contactImsHolder.beGone()
        }
    }

    private fun setupEvents() {
        var events = contact!!.events.toMutableSet() as LinkedHashSet<Event>

        if (mergeDuplicate) {
            duplicateContacts.forEach {
                events.addAll(it.events)
            }
        }

        events = events.sortedBy { it.type }.toMutableSet() as LinkedHashSet<Event>
        fullContact!!.events = events.toMutableList() as ArrayList<Event>
        binding.contactEventsHolder.removeAllViews()

        if (events.isNotEmpty() && showFields and SHOW_EVENTS_FIELD != 0) {
            events.forEach {
                ItemViewEventBinding.inflate(layoutInflater, binding.contactEventsHolder, false).apply {
                    binding.contactEventsHolder.addView(root)
                    it.value.getDateTimeFromDateString(true, contactEvent)
                    contactEventType.setText(getEventTextId(it.type))
                    root.copyOnLongClick(it.value)
                }
            }
            binding.contactEventsImage.beVisible()
            binding.contactEventsHolder.beVisible()
        } else {
            binding.contactEventsImage.beGone()
            binding.contactEventsHolder.beGone()
        }
    }

    private fun setupWebsites() {
        var websites = contact!!.websites.toMutableSet() as LinkedHashSet<String>

        if (mergeDuplicate) {
            duplicateContacts.forEach {
                websites.addAll(it.websites)
            }
        }

        websites = websites.sorted().toMutableSet() as LinkedHashSet<String>
        fullContact!!.websites = websites.toMutableList() as ArrayList<String>
        binding.contactWebsitesHolder.removeAllViews()

        if (websites.isNotEmpty() && showFields and SHOW_WEBSITES_FIELD != 0) {
            websites.forEach {
                val url = it
                ItemWebsiteBinding.inflate(layoutInflater, binding.contactWebsitesHolder, false).apply {
                    binding.contactWebsitesHolder.addView(root)
                    contactWebsite.text = url
                    root.copyOnLongClick(url)

                    root.setOnClickListener {
                        openWebsiteIntent(url)
                    }
                }
            }
            binding.contactWebsitesImage.beVisible()
            binding.contactWebsitesHolder.beVisible()
        } else {
            binding.contactWebsitesImage.beGone()
            binding.contactWebsitesHolder.beGone()
        }
    }

    private fun setupGroups() {
        var groups = contact!!.groups.toMutableSet() as LinkedHashSet<Group>

        if (mergeDuplicate) {
            duplicateContacts.forEach {
                groups.addAll(it.groups)
            }
        }

        groups = groups.sortedBy { it.title }.toMutableSet() as LinkedHashSet<Group>
        fullContact!!.groups = groups.toMutableList() as ArrayList<Group>
        binding.contactGroupsHolder.removeAllViews()

        if (groups.isNotEmpty() && showFields and SHOW_GROUPS_FIELD != 0) {
            groups.forEach {
                ItemViewGroupBinding.inflate(layoutInflater, binding.contactGroupsHolder, false).apply {
                    val group = it
                    binding.contactGroupsHolder.addView(root)
                    contactGroup.text = group.title
                    root.copyOnLongClick(group.title)
                }
            }
            binding.contactGroupsImage.beVisible()
            binding.contactGroupsHolder.beVisible()
        } else {
            binding.contactGroupsImage.beGone()
            binding.contactGroupsHolder.beGone()
        }
    }

    private fun setupContactSources() {
        binding.contactSourcesHolder.removeAllViews()
        if (showFields and SHOW_CONTACT_SOURCE_FIELD != 0) {
            var sources = HashMap<Contact, String>()
            sources[contact!!] = getPublicContactSourceSync(contact!!.source, contactSources)

            if (mergeDuplicate) {
                duplicateContacts.forEach {
                    sources[it] = getPublicContactSourceSync(it.source, contactSources)
                }
            }

            if (sources.size > 1) {
                sources =
                    sources.toList().sortedBy { (_, value) -> value.lowercase(Locale.getDefault()) }
                        .toMap() as LinkedHashMap<Contact, String>
            }

            for ((key, value) in sources) {
                ItemViewContactSourceBinding.inflate(layoutInflater, binding.contactSourcesHolder, false).apply {
                    contactSource.text = if (value == "") getString(R.string.phone_storage) else value
                    contactSource.copyOnLongClick(value)
                    binding.contactSourcesHolder.addView(root)

                    contactSource.setOnClickListener {
                        launchEditContact(key)
                    }

                    if (value.lowercase(Locale.ROOT) == WHATSAPP) {
                        contactSourceImage.setImageDrawable(getPackageDrawable(WHATSAPP_PACKAGE))
                        contactSourceImage.beVisible()
                        contactSourceImage.setOnClickListener {
                            showSocialActions(key.id)
                        }
                    }

                    if (value.lowercase(Locale.ROOT) == SIGNAL) {
                        contactSourceImage.setImageDrawable(getPackageDrawable(SIGNAL_PACKAGE))
                        contactSourceImage.beVisible()
                        contactSourceImage.setOnClickListener {
                            showSocialActions(key.id)
                        }
                    }

                    if (value.lowercase(Locale.ROOT) == VIBER) {
                        contactSourceImage.setImageDrawable(getPackageDrawable(VIBER_PACKAGE))
                        contactSourceImage.beVisible()
                        contactSourceImage.setOnClickListener {
                            showSocialActions(key.id)
                        }
                    }

                    if (value.lowercase(Locale.ROOT) == TELEGRAM) {
                        contactSourceImage.setImageDrawable(getPackageDrawable(TELEGRAM_PACKAGE))
                        contactSourceImage.beVisible()
                        contactSourceImage.setOnClickListener {
                            showSocialActions(key.id)
                        }
                    }

                    if (value.lowercase(Locale.ROOT) == THREEMA) {
                        contactSourceImage.setImageDrawable(getPackageDrawable(THREEMA_PACKAGE))
                        contactSourceImage.beVisible()
                        contactSourceImage.setOnClickListener {
                            showSocialActions(key.id)
                        }
                    }
                }
            }

            binding.contactSourceImage.beVisible()
            binding.contactSourcesHolder.beVisible()
        } else {
            binding.contactSourceImage.beGone()
            binding.contactSourcesHolder.beGone()
        }
    }

    private fun setupNotes() {
        val notes = contact!!.notes
        if (notes.isNotEmpty() && showFields and SHOW_NOTES_FIELD != 0) {
            binding.contactNotes.text = notes
            binding.contactNotesImage.beVisible()
            binding.contactNotes.beVisible()
            binding.contactNotes.copyOnLongClick(notes)
        } else {
            binding.contactNotesImage.beGone()
            binding.contactNotes.beGone()
        }
    }

    @Suppress("DEPRECATION")
    private fun setupRingtone() {
        if (showFields and SHOW_RINGTONE_FIELD != 0) {
            binding.contactRingtoneImage.beVisible()
            binding.contactRingtone.beVisible()

            val ringtone = contact!!.ringtone
            if (ringtone?.isEmpty() == true) {
                binding.contactRingtone.text = getString(R.string.no_sound)
            } else if (ringtone?.isNotEmpty() == true && ringtone != getDefaultRingtoneUri().toString()) {
                if (ringtone == SILENT) {
                    binding.contactRingtone.text = getString(R.string.no_sound)
                } else {
                    systemRingtoneSelected(Uri.parse(ringtone))
                }
            } else {
                binding.contactRingtoneImage.beGone()
                binding.contactRingtone.beGone()
                return
            }

            binding.contactRingtone.copyOnLongClick(binding.contactRingtone.text.toString())

            binding.contactRingtone.setOnClickListener {
                val ringtonePickerIntent = getRingtonePickerIntent()

                try {
                    startActivityForResult(ringtonePickerIntent, INTENT_SELECT_RINGTONE)
                } catch (e: Exception) {
                    val currentRingtone = contact!!.ringtone ?: getDefaultAlarmSound(RingtoneManager.TYPE_RINGTONE).uri
                    SelectAlarmSoundDialog(this@ViewContactActivity,
                        currentRingtone,
                        AudioManager.STREAM_RING,
                        PICK_RINGTONE_INTENT_ID,
                        RingtoneManager.TYPE_RINGTONE,
                        true,
                        onAlarmPicked = {
                            binding.contactRingtone.text = it?.title
                            ringtoneUpdated(it?.uri)
                        },
                        onAlarmSoundDeleted = {}
                    )
                }
            }
        } else {
            binding.contactRingtoneImage.beGone()
            binding.contactRingtone.beGone()
        }
    }

    private fun setupOrganization() {
        val organization = contact!!.organization
        if (organization.isNotEmpty() && showFields and SHOW_ORGANIZATION_FIELD != 0) {
            binding.contactOrganizationCompany.text = organization.company
            binding.contactOrganizationJobPosition.text = organization.jobPosition
            binding.contactOrganizationImage.beGoneIf(organization.isEmpty())
            binding.contactOrganizationCompany.beGoneIf(organization.company.isEmpty())
            binding.contactOrganizationJobPosition.beGoneIf(organization.jobPosition.isEmpty())
            binding.contactOrganizationCompany.copyOnLongClick(binding.contactOrganizationCompany.value)
            binding.contactOrganizationJobPosition.copyOnLongClick(binding.contactOrganizationJobPosition.value)

            if (organization.company.isEmpty() && organization.jobPosition.isNotEmpty()) {
                (binding.contactOrganizationImage.layoutParams as RelativeLayout.LayoutParams).addRule(
                    RelativeLayout.ALIGN_TOP,
                    binding.contactOrganizationJobPosition.id
                )
            }
        } else {
            binding.contactOrganizationImage.beGone()
            binding.contactOrganizationCompany.beGone()
            binding.contactOrganizationJobPosition.beGone()
        }
    }

    private fun showSocialActions(contactId: Int) {
        ensureBackgroundThread {
            val actions = getSocialActions(contactId)
            runOnUiThread {
                if (!isDestroyed && !isFinishing) {
                    ChooseSocialDialog(this@ViewContactActivity, actions) { action ->
                        Intent(Intent.ACTION_VIEW).apply {
                            val uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, action.dataId)
                            setDataAndType(uri, action.mimetype)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                            try {
                                startActivity(this)
                            } catch (e: SecurityException) {
                                handlePermission(PERMISSION_CALL_PHONE) { success ->
                                    if (success) {
                                        startActivity(this)
                                    } else {
                                        toast(R.string.no_phone_call_permission)
                                    }
                                }
                            } catch (e: ActivityNotFoundException) {
                                toast(R.string.no_app_found)
                            } catch (e: Exception) {
                                showErrorToast(e)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun systemRingtoneSelected(uri: Uri?) {
        val contactRingtone = RingtoneManager.getRingtone(this, uri)
        binding.contactRingtone.text = contactRingtone.getTitle(this)
        ringtoneUpdated(uri?.toString() ?: "")
    }

    private fun ringtoneUpdated(path: String?) {
        contact!!.ringtone = path

        ensureBackgroundThread {
            if (contact!!.isPrivate()) {
                LocalContactsHelper(this).updateRingtone(contact!!.contactId, path ?: "")
            } else {
                ContactsHelper(this).updateRingtone(contact!!.contactId.toString(), path ?: "")
            }
        }
    }

    private fun getDuplicateContacts(callback: () -> Unit) {
        ContactsHelper(this).getDuplicatesOfContact(contact!!, false) { contacts ->
            ensureBackgroundThread {
                duplicateContacts.clear()
                val displayContactSources = getVisibleContactSources()
                contacts.filter { displayContactSources.contains(it.source) }.forEach {
                    val duplicate = ContactsHelper(this).getContactWithId(it.id, it.isPrivate())
                    if (duplicate != null) {
                        duplicateContacts.add(duplicate)
                    }
                }

                runOnUiThread {
                    callback()
                }
            }
        }
    }

    private fun deleteContactFromAllSources() {
        val addition = if (binding.contactSourcesHolder.childCount > 1) {
            "\n\n${getString(R.string.delete_from_all_sources)}"
        } else {
            ""
        }

        val message = "${getString(R.string.proceed_with_deletion)}$addition"
        ConfirmationDialog(this, message) {
            if (contact != null) {
                ContactsHelper(this).deleteContact(contact!!, true) {
                    finish()
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getStarDrawable(on: Boolean) =
        resources.getDrawable(if (on) R.drawable.ic_star_vector else R.drawable.ic_star_outline_vector, theme)

    private fun hideBigContactPhoto() {
        binding.contactPhotoBig.animate().alpha(0f).withEndAction { binding.contactPhotoBig.beGone() }.start()
    }

    private fun View.copyOnLongClick(value: String) {
        setOnLongClickListener {
            copyToClipboard(value)
            true
        }
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


    private fun trySendSMS() {
        val numbers = contact!!.phoneNumbers
        if (numbers.size == 1) {
            launchSendSMSIntent(numbers.first().value)
        } else if (numbers.size > 1) {
            val primaryNumber = numbers.find { it.isPrimary }
            if (primaryNumber != null) {
                launchSendSMSIntent(primaryNumber.value)
            } else {
                val items = ArrayList<RadioItem>()
                numbers.forEachIndexed { index, phoneNumber ->
                    items.add(RadioItem(index, phoneNumber.value, phoneNumber.value))
                }

                RadioGroupDialog(this, items) {
                    launchSendSMSIntent(it as String)
                }
            }
        }
    }

    fun trySendEmail() {
        val emails = contact!!.emails
        if (emails.size == 1) {
            sendEmailIntent(emails.first().value)
        } else if (emails.size > 1) {
            val items = ArrayList<RadioItem>()
            emails.forEachIndexed { index, email ->
                items.add(RadioItem(index, email.value, email.value))
            }

            RadioGroupDialog(this, items) {
                sendEmailIntent(it as String)
            }
        }
    }

    fun getEmailTypeText(type: Int, label: String): String {
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

    fun getAddressTypeText(type: Int, label: String): String {
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

    @Suppress("DEPRECATION")
    private fun getIMTypeText(type: Int, label: String): String {
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

    private fun getEventTextId(type: Int) = when (type) {
        ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY -> R.string.anniversary
        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY -> R.string.birthday
        else -> R.string.other
    }

}