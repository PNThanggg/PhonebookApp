package com.app.phonebook.presentation.fragments

import android.content.Context
import android.util.AttributeSet
import com.app.phonebook.R
import com.app.phonebook.adapter.ContactsAdapter
import com.app.phonebook.base.extension.areSystemAnimationsEnabled
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.beGone
import com.app.phonebook.base.extension.beVisible
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.getColorStateList
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.base.extension.hasPermission
import com.app.phonebook.base.extension.launchCreateNewContactIntent
import com.app.phonebook.base.extension.normalizePhoneNumber
import com.app.phonebook.base.extension.normalizeString
import com.app.phonebook.base.extension.startContactDetailsIntent
import com.app.phonebook.base.extension.underlineText
import com.app.phonebook.base.interfaces.RefreshItemsListener
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.utils.getProperText
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import com.app.phonebook.databinding.FragmentContactsBinding
import com.app.phonebook.databinding.FragmentLettersLayoutBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.MyContactsContentProvider
import com.app.phonebook.presentation.activities.MainActivity
import com.app.phonebook.presentation.view.FastScrollItemIndicator
import java.util.Locale

class ContactsFragment(context: Context, attributeSet: AttributeSet) :
    MyViewPagerFragment<MyViewPagerFragment.LettersInnerBinding>(context, attributeSet), RefreshItemsListener {
    private lateinit var binding: FragmentLettersLayoutBinding
    private var allContacts = ArrayList<Contact>()
    private var contactAdapter: ContactsAdapter? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = FragmentLettersLayoutBinding.bind(FragmentContactsBinding.bind(this).contactsFragment)
        innerBinding = LettersInnerBinding(binding)
    }

    override fun setupFragment() {
        val placeholderResId = if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
            R.string.no_contacts_found
        } else {
            R.string.could_not_access_contacts
        }

        binding.fragmentPlaceholder.text = context.getString(placeholderResId)

        val placeholderActionResId = if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
            R.string.create_new_contact
        } else {
            R.string.request_access
        }

        binding.fragmentPlaceholder2.apply {
            text = context.getString(placeholderActionResId)
            underlineText()
            setOnClickListener {
                if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
                    activity?.launchCreateNewContactIntent()
                } else {
                    requestReadContactsPermission()
                }
            }
        }
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
        binding.apply {
            contactAdapter?.updateTextColor(textColor)
            fragmentPlaceholder.setTextColor(textColor)
            fragmentPlaceholder2.setTextColor(properPrimaryColor)

            letterFastScroller.textColor = textColor.getColorStateList()
            letterFastScroller.pressedTextColor = properPrimaryColor

            letterFastScrollerThumb.setupWithFastScroller(letterFastScroller)
            letterFastScrollerThumb.textColor = properPrimaryColor.getContrastColor()
            letterFastScrollerThumb.thumbColor = properPrimaryColor.getColorStateList()
        }
    }

    override fun refreshItems(callback: (() -> Unit)?) {
        val privateCursor = context?.getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
        ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
            allContacts = contacts

            if (SMT_PRIVATE !in context.baseConfig.ignoredContactSources) {
                val privateContacts = MyContactsContentProvider.getContacts(privateCursor)
                if (privateContacts.isNotEmpty()) {
                    allContacts.addAll(privateContacts)
                    allContacts.sort()
                }
            }

            (activity as MainActivity).cacheContacts(allContacts)

            activity?.runOnUiThread {
                gotContacts(contacts)
                callback?.invoke()
            }
        }
    }

    private fun gotContacts(contacts: ArrayList<Contact>) {
        setupLetterFastScroller(contacts)
        if (contacts.isEmpty()) {
            binding.apply {
                fragmentPlaceholder.beVisible()
                fragmentPlaceholder2.beVisible()
                fragmentList.beGone()
            }
        } else {
            binding.apply {
                fragmentPlaceholder.beGone()
                fragmentPlaceholder2.beGone()
                fragmentList.beVisible()

                if (fragmentList.adapter == null) {
                    ContactsAdapter(
                        activity = activity as BaseActivity<*>,
                        contacts = contacts,
                        recyclerView = fragmentList,
                        refreshItemsListener = this@ContactsFragment
                    ) {
                        val contact = it as Contact
                        activity?.startContactDetailsIntent(contact)
                    }.apply {
                        contactAdapter = this
                        fragmentList.adapter = contactAdapter
                    }

                    if (context.areSystemAnimationsEnabled) {
                        fragmentList.scheduleLayoutAnimation()
                    }
                } else {
                    contactAdapter?.updateItems(contacts)
                }
            }
        }
    }

    private fun setupLetterFastScroller(contacts: ArrayList<Contact>) {
        binding.letterFastScroller.setupWithRecyclerView(binding.fragmentList, { position ->
            try {
                val name = contacts[position].getNameToDisplay()
                val character = if (name.isNotEmpty()) name.substring(0, 1) else ""
                FastScrollItemIndicator.Text(character.uppercase(Locale.getDefault()).normalizeString())
            } catch (e: Exception) {
                FastScrollItemIndicator.Text("")
            }
        })
    }

    override fun onSearchClosed() {
        binding.fragmentPlaceholder.beVisibleIf(allContacts.isEmpty())
        contactAdapter?.updateItems(allContacts)
        setupLetterFastScroller(allContacts)
    }

    override fun onSearchQueryChanged(text: String) {
        val shouldNormalize = text.normalizeString() == text
        val filtered = allContacts.filter { contact ->
            getProperText(contact.getNameToDisplay(), shouldNormalize).contains(text, true) || getProperText(
                contact.nickname,
                shouldNormalize
            ).contains(text, true) || contact.phoneNumbers.any {
                text.normalizePhoneNumber().isNotEmpty() && it.normalizedNumber.contains(
                    text.normalizePhoneNumber(), true
                )
            } || contact.emails.any { it.value.contains(text, true) } || contact.addresses.any {
                getProperText(
                    it.value,
                    shouldNormalize
                ).contains(text, true)
            } || contact.listIM.any { im ->
                im.value.contains(text, true)
            } || getProperText(contact.notes, shouldNormalize).contains(text, true) || getProperText(
                contact.organization.company,
                shouldNormalize
            ).contains(text, true) || getProperText(contact.organization.jobPosition, shouldNormalize).contains(
                text,
                true
            ) || contact.websites.any { it.contains(text, true) }
        } as ArrayList

        filtered.sortBy {
            val nameToDisplay = it.getNameToDisplay()
            !getProperText(nameToDisplay, shouldNormalize).startsWith(text, true) && !nameToDisplay.contains(text, true)
        }

        binding.fragmentPlaceholder.beVisibleIf(filtered.isEmpty())
        (binding.fragmentList.adapter as? ContactsAdapter)?.updateItems(filtered, text)
        setupLetterFastScroller(filtered)
    }

    private fun requestReadContactsPermission() {
        activity?.handlePermission(PERMISSION_READ_CONTACTS) {
            if (it) {
                binding.fragmentPlaceholder.text = context.getString(R.string.no_contacts_found)
                binding.fragmentPlaceholder2.text = context.getString(R.string.create_new_contact)
                ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
                    activity?.runOnUiThread {
                        gotContacts(contacts)
                    }
                }
            }
        }
    }
}
