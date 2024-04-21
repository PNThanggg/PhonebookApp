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
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getColorStateList
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.base.extension.hasPermission
import com.app.phonebook.base.extension.normalizeString
import com.app.phonebook.base.extension.startContactDetailsIntent
import com.app.phonebook.base.extension.telephonyManager
import com.app.phonebook.base.helpers.Converters
import com.app.phonebook.base.interfaces.RefreshItemsListener
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.utils.VIEW_TYPE_GRID
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import com.app.phonebook.databinding.FragmentFavoritesBinding
import com.app.phonebook.databinding.FragmentLettersLayoutBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.MyContactsContentProvider
import com.app.phonebook.presentation.dialog.CallConfirmationDialog
import com.app.phonebook.presentation.view.FastScrollItemIndicator
import com.app.phonebook.presentation.view.MyGridLayoutManager
import com.app.phonebook.presentation.view.MyLinearLayoutManager
import com.google.gson.Gson
import java.util.Locale

class FavoritesFragment(
    context: Context,
    attributeSet: AttributeSet
) : MyViewPagerFragment<MyViewPagerFragment.LettersInnerBinding>(context, attributeSet), RefreshItemsListener {
    private lateinit var binding: FragmentLettersLayoutBinding
    var allContacts = ArrayList<Contact>()

    private var contactAdapter: ContactsAdapter? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = FragmentLettersLayoutBinding.bind(FragmentFavoritesBinding.bind(this).favoritesFragment)
        innerBinding = LettersInnerBinding(binding)
    }

    override fun setupFragment() {
        val placeholderResId = if (context.hasPermission(PERMISSION_READ_CONTACTS)) {
            R.string.no_contacts_found
        } else {
            R.string.could_not_access_contacts
        }

        binding.fragmentPlaceholder.text = context.getString(placeholderResId)
        binding.fragmentPlaceholder2.beGone()
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
        binding.apply {
            fragmentPlaceholder.setTextColor(textColor)
            contactAdapter?.updateTextColor(textColor)

            letterFastScroller.textColor = textColor.getColorStateList()
            letterFastScroller.pressedTextColor = properPrimaryColor

            letterFastScrollerThumb.setupWithFastScroller(letterFastScroller)
            letterFastScrollerThumb.textColor = properPrimaryColor.getContrastColor()
            letterFastScrollerThumb.thumbColor = properPrimaryColor.getColorStateList()
        }
    }

    override fun refreshItems(callback: (() -> Unit)?) {
        ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
            allContacts = contacts

            if (SMT_PRIVATE !in context.baseConfig.ignoredContactSources) {
                val privateCursor = context?.getMyContactsCursor(favoritesOnly = true, withPhoneNumbersOnly = true)
                val privateContacts = MyContactsContentProvider.getContacts(privateCursor).map {
                    it.copy(starred = 1)
                }
                if (privateContacts.isNotEmpty()) {
                    allContacts.addAll(privateContacts)
                    allContacts.sort()
                }
            }
            val favorites = contacts.filter { it.starred == 1 } as ArrayList<Contact>

            allContacts = if (activity!!.config.isCustomOrderSelected) {
                sortByCustomOrder(favorites)
            } else {
                favorites
            }

            activity?.runOnUiThread {
                gotContacts(allContacts)
                callback?.invoke()
            }
        }
    }

    private fun gotContacts(contacts: ArrayList<Contact>) {
        setupLetterFastScroller(contacts)
        binding.apply {
            if (contacts.isEmpty()) {
                fragmentPlaceholder.beVisible()
                fragmentList.beGone()
            } else {
                fragmentPlaceholder.beGone()
                fragmentList.beVisible()

                updateListAdapter()
            }
        }
    }

    private fun updateListAdapter() {
        setViewType(context.config.viewType)

        if (contactAdapter == null) {
            ContactsAdapter(
                activity = activity as BaseActivity<*>,
                contacts = allContacts,
                recyclerView = binding.fragmentList,
                refreshItemsListener = this,
                viewType = context.config.viewType,
                showDeleteButton = false,
                enableDrag = true,
            ) {
                if (context.config.showCallConfirmation) {
                    CallConfirmationDialog(activity as BaseActivity<*>, (it as Contact).getNameToDisplay()) {
                        activity?.apply {
                            initiateCall(it) { str ->
                                launchCallIntent(str)
                            }
                        }
                    }
                } else {
                    val contact = it as Contact
                    activity?.startContactDetailsIntent(contact)
                }
            }.apply {
                contactAdapter = this
                binding.fragmentList.adapter = contactAdapter

                onDragEndListener = {
                    val adapter = binding.fragmentList.adapter
                    if (adapter is ContactsAdapter) {
                        val items = adapter.contacts
                        saveCustomOrderToPrefs(items)
                        setupLetterFastScroller(items)
                    }
                }

                onSpanCountListener = { newSpanCount ->
                    context.config.contactsGridColumnCount = newSpanCount
                }
            }

            if (context.areSystemAnimationsEnabled) {
                binding.fragmentList.scheduleLayoutAnimation()
            }
        } else {
            contactAdapter?.apply {
                updateItems(allContacts)
            }
        }
    }

    fun columnCountChanged() {
        (binding.fragmentList.layoutManager as MyGridLayoutManager).spanCount = context!!.config.contactsGridColumnCount
        binding.fragmentList.adapter?.apply {
            notifyItemRangeChanged(0, allContacts.size)
        }
    }

    private fun sortByCustomOrder(favorites: List<Contact>): ArrayList<Contact> {
        val favoritesOrder = activity!!.config.favoritesContactsOrder

        if (favoritesOrder.isEmpty()) {
            return ArrayList(favorites)
        }

        val orderList = Converters().jsonToStringList(favoritesOrder)
        val map = orderList.withIndex().associate { it.value to it.index }
        val sorted = favorites.sortedBy { map[it.contactId.toString()] }

        return ArrayList(sorted)
    }

    private fun saveCustomOrderToPrefs(items: List<Contact>) {
        activity?.apply {
            val orderIds = items.map { it.contactId }
            val orderGsonString = Gson().toJson(orderIds)
            config.favoritesContactsOrder = orderGsonString
        }
    }

    private fun setupLetterFastScroller(contacts: List<Contact>) {
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
        (binding.fragmentList.adapter as? ContactsAdapter)?.updateItems(allContacts)
        setupLetterFastScroller(allContacts)
    }

    override fun onSearchQueryChanged(text: String) {
        val contacts = allContacts.filter {
            it.name.contains(text, true) || it.doesContainPhoneNumber(text, true, context.telephonyManager)
        }.sortedByDescending {
            it.name.startsWith(text, true)
        }.toMutableList() as ArrayList<Contact>

        binding.fragmentPlaceholder.beVisibleIf(contacts.isEmpty())
        (binding.fragmentList.adapter as? ContactsAdapter)?.updateItems(contacts, text)
        setupLetterFastScroller(contacts)
    }

    private fun setViewType(viewType: Int) {
        val spanCount = context.config.contactsGridColumnCount

        val layoutManager = if (viewType == VIEW_TYPE_GRID) {
            binding.letterFastScroller.beGone()
            MyGridLayoutManager(context, spanCount)
        } else {
            binding.letterFastScroller.beVisible()
            MyLinearLayoutManager(context)
        }

        binding.fragmentList.layoutManager = layoutManager
    }
}
