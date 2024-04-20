package com.app.phonebook.presentation.fragments

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.app.phonebook.R
import com.app.phonebook.adapter.GroupsAdapter
import com.app.phonebook.base.extension.areSystemAnimationsEnabled
import com.app.phonebook.base.extension.beGone
import com.app.phonebook.base.extension.beVisible
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getColorStateList
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.hasPermission
import com.app.phonebook.base.extension.hideKeyboard
import com.app.phonebook.base.extension.normalizeString
import com.app.phonebook.base.extension.underlineText
import com.app.phonebook.base.interfaces.RefreshItemsListener
import com.app.phonebook.base.utils.GROUP
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.Group
import com.app.phonebook.databinding.FragmentGroupsBinding
import com.app.phonebook.databinding.FragmentLettersLayoutBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.presentation.activities.GroupContactsActivity
import com.app.phonebook.presentation.dialog.CreateNewGroupDialog
import java.util.Locale

class GroupsFragment(
    context: Context, attributeSet: AttributeSet
) : MyViewPagerFragment<MyViewPagerFragment.LettersInnerBinding>(context, attributeSet), RefreshItemsListener {

    private lateinit var binding: FragmentLettersLayoutBinding
    private var lastHashCode = 0
    private var groupsIgnoringSearch = listOf<Group>()
    private var allContacts: ArrayList<Contact> = ArrayList()

    private var groupsAdapter: GroupsAdapter? = null

    var skipHashComparing = false
    var forceListRedraw = false

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = FragmentLettersLayoutBinding.bind(FragmentGroupsBinding.bind(this).groupsFragment)
        innerBinding = LettersInnerBinding(binding)
    }

    override fun setupFragment() {
        binding.apply {
            val placeholderResId = if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
                R.string.no_contacts_found
            } else {
                R.string.could_not_access_contacts
            }

            fragmentPlaceholder.text = context.getString(placeholderResId)

            fragmentPlaceholder2.apply {
                text = context.getString(
                    if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
                        R.string.create_new_contact
                    } else {
                        R.string.request_access
                    }
                )

                setOnClickListener {
                    if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
                        if (activity != null) CreateNewGroupDialog(activity!!) {
                            refreshItems()
                        }
                    }
                }

                underlineText()
            }
        }
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {

        binding.apply {
            fragmentPlaceholder.setTextColor(textColor)
            groupsAdapter?.updateTextColor(textColor)

            letterFastScroller.textColor = textColor.getColorStateList()
            letterFastScroller.pressedTextColor = properPrimaryColor

            letterFastScrollerThumb.setupWithFastScroller(letterFastScroller)
            letterFastScrollerThumb.textColor = properPrimaryColor.getContrastColor()
            letterFastScrollerThumb.thumbColor = properPrimaryColor.getColorStateList()
        }
    }

    override fun onSearchClosed() {
        groupsAdapter?.updateItems(ArrayList(groupsIgnoringSearch))
        setupViewVisibility(groupsIgnoringSearch.isNotEmpty())
    }

    private fun setupViewVisibility(hasItemsToShow: Boolean) {
//        if (binding.fragmentPlaceholder2.tag != AVOID_CHANGING_VISIBILITY_TAG) {
//            binding.fragmentPlaceholder2.beVisibleIf(!hasItemsToShow)
//        }

        binding.fragmentPlaceholder2.beVisibleIf(!hasItemsToShow)

        binding.fragmentPlaceholder.beVisibleIf(!hasItemsToShow)
        binding.fragmentList.beVisibleIf(hasItemsToShow)
    }

    override fun onSearchQueryChanged(text: String) {
        val filtered = groupsIgnoringSearch.filter {
            it.title.contains(text, true)
        } as ArrayList

        if (filtered.isEmpty()) {
            binding.fragmentPlaceholder.text = activity?.getString(R.string.no_items_found)
        }

        binding.fragmentPlaceholder.beVisibleIf(filtered.isEmpty())
        (binding.fragmentList.adapter as? GroupsAdapter)?.updateItems(filtered, text)
    }

    override fun refreshItems(callback: (() -> Unit)?) {
        ContactsHelper(context).getContacts { contacts: ArrayList<Contact> ->
            if (context.config.lastUsedContactSource.isEmpty()) {
                val grouped = contacts.groupBy { it.source }.maxWithOrNull(compareBy { it.value.size })
                context.config.lastUsedContactSource = grouped?.key ?: ""
            }

            allContacts = contacts

            var currentHash = 0
            contacts.forEach {
                currentHash += it.getHashWithoutPrivatePhoto()
            }

            if (currentHash != lastHashCode || skipHashComparing || contacts.isEmpty()) {
                skipHashComparing = false
                lastHashCode = currentHash

                activity?.runOnUiThread {
                    setupContacts(contacts)

//                    if (placeholderText != null) {
//                        binding.fragmentPlaceholder.text = placeholderText
//                        binding.fragmentPlaceholder.tag = AVOID_CHANGING_TEXT_TAG
//                        binding.fragmentPlaceholder2.beGone()
//                        binding.fragmentPlaceholder2.tag = AVOID_CHANGING_VISIBILITY_TAG
//                    }
                }
            }
        }
    }

    private fun setupContacts(contacts: ArrayList<Contact>) {
        setupGroupsAdapter(contacts) {
            groupsIgnoringSearch = groupsAdapter?.groups ?: ArrayList()
        }
    }

    private fun setupGroupsAdapter(contacts: ArrayList<Contact>, callback: () -> Unit) {
        if (contacts.isEmpty()) {
            binding.apply {
                fragmentPlaceholder.beVisible()
                fragmentPlaceholder2.beVisible()
                fragmentList.beGone()
            }
        } else {
            ContactsHelper(activity!!).getStoredGroups { listGroup ->
                var storedGroups = listGroup

                contacts.forEach { contact ->
                    contact.groups.forEach { group ->
                        val storedGroup = storedGroups.firstOrNull { it.id == group.id }
                        storedGroup?.addContact()
                    }
                }

                storedGroups =
                    storedGroups.asSequence().sortedWith(compareBy { it.title.lowercase(Locale.ROOT).normalizeString() })
                        .toMutableList() as ArrayList<Group>

                binding.fragmentPlaceholder2.beVisibleIf(storedGroups.isEmpty())
                binding.fragmentPlaceholder.beVisibleIf(storedGroups.isEmpty())
                binding.letterFastScroller.beVisibleIf(storedGroups.isNotEmpty())

                if (groupsAdapter == null) {
                    GroupsAdapter(
                        activity = activity as BaseActivity<*>,
                        groups = storedGroups,
                        recyclerView = binding.fragmentList,
                        refreshItemsListener = this@GroupsFragment
                    ) {
                        activity?.hideKeyboard()
                        Intent(activity, GroupContactsActivity::class.java).apply {
                            putExtra(GROUP, it as Group)
                            activity?.startActivity(this)
                        }
                    }.apply {
                        groupsAdapter = this
                        innerBinding.fragmentList.adapter = groupsAdapter
                    }

                    if (context.areSystemAnimationsEnabled) {
                        innerBinding.fragmentList.scheduleLayoutAnimation()
                    }
                } else {
                    groupsAdapter?.apply {
                        showContactThumbnails = activity.config.showContactThumbnails
                        updateItems(storedGroups)
                    }
                }

                callback()
            }
        }
    }
}
