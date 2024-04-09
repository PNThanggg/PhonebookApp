package com.app.phonebook.presentation.fragments

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.ViewGroup
import com.app.phonebook.R
import com.app.phonebook.adapter.GroupsAdapter
import com.app.phonebook.base.extension.areSystemAnimationsEnabled
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.base.extension.hideKeyboard
import com.app.phonebook.base.extension.normalizeString
import com.app.phonebook.base.extension.underlineText
import com.app.phonebook.base.extension.updateTextColors
import com.app.phonebook.base.interfaces.RefreshItemsListener
import com.app.phonebook.base.utils.AVOID_CHANGING_VISIBILITY_TAG
import com.app.phonebook.base.utils.GROUP
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseViewPagerFragment
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.Group
import com.app.phonebook.databinding.FragmentGroupBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.presentation.activities.GroupContactsActivity
import com.app.phonebook.presentation.dialog.CreateNewGroupDialog
import com.app.phonebook.provider.MyContactsContentProvider
import java.util.Locale

class GroupFragment(
    context: Context,
    attributeSet: AttributeSet
) : BaseViewPagerFragment<BaseViewPagerFragment.GroupInnerBinding>(context, attributeSet), RefreshItemsListener {
    private lateinit var binding: FragmentGroupBinding

    private var groupsIgnoringSearch = listOf<Group>()

    private var allGroups: MutableList<Group> = mutableListOf()
    private var groupAdapter: GroupsAdapter? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = FragmentGroupBinding.bind(this)
        innerBinding = GroupInnerBinding(binding)
    }

    override fun setupFragment() {
        binding.groupPlaceholder.text = context.getString(R.string.no_group_created)

        binding.groupPlaceholder2.apply {
            text = context.getString(R.string.create_group)

            underlineText()

            setOnClickListener {
                showNewGroupsDialog()
            }
        }
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
        binding.groupPlaceholder.setTextColor(primaryColor)
        binding.groupPlaceholder2.setTextColor(primaryColor)

        groupAdapter?.apply {
            updateTextColor(textColor)
        }

        context.updateTextColors(binding.groupFragment.parent as ViewGroup)
        binding.fragmentFastScroller.updateColors(primaryColor)
    }

    override fun refreshItems(callback: (() -> Unit)?) {
        var allContacts: MutableList<Contact> = mutableListOf()

        activity?.runOnUiThread {
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
            }

            ContactsHelper(activity!!).getStoredGroups { groups: ArrayList<Group> ->
                allContacts.forEach { contact: Contact ->
                    contact.groups.forEach { group ->
                        val storedGroup = groups.firstOrNull { it.id == group.id }
                        storedGroup?.addContact()
                    }
                }

                allGroups = allGroups.asSequence().sortedWith(compareBy {
                    it.title.lowercase(Locale.ROOT).normalizeString()
                }).toMutableList() as ArrayList<Group>

                binding.groupPlaceholder.beVisibleIf(allGroups.isEmpty())
                binding.groupPlaceholder2.beVisibleIf(allGroups.isEmpty())

                if (groupAdapter == null) {
                    GroupsAdapter(
                        activity as BaseActivity<*>,
                        allGroups,
                        binding.groupList,
                        this,
                    ) {
                        activity?.hideKeyboard()
                        Intent(activity, GroupContactsActivity::class.java).apply {
                            putExtra(GROUP, it as Group)
                            activity?.startActivity(this)
                        }
                    }.apply {
                        groupAdapter = this
                        binding.groupList.adapter = groupAdapter
                    }

                    if (context.areSystemAnimationsEnabled) {
                        binding.groupList.scheduleLayoutAnimation()
                    }
                } else {
                    groupAdapter?.apply {
                        showContactThumbnails = activity.config.showContactThumbnails
                        updateItems(allGroups)
                    }
                }

                callback?.invoke()
            }
        }
    }


    override fun onSearchClosed() {
        binding.groupPlaceholder.beVisibleIf(allGroups.isEmpty())
        (binding.groupList.adapter as? GroupsAdapter)?.updateItems(ArrayList(groupsIgnoringSearch))
        setupViewVisibility(groupsIgnoringSearch.isNotEmpty())
    }

    override fun onSearchQueryChanged(context: Context, text: String) {
        val filtered = groupsIgnoringSearch.filter {
            it.title.contains(text, true)
        } as ArrayList

        if (filtered.isEmpty()) {
            binding.groupPlaceholder.text = activity?.getString(R.string.no_items_found)
        }

        binding.groupPlaceholder.beVisibleIf(filtered.isEmpty())
        (binding.groupList.adapter as? GroupsAdapter)?.updateItems(filtered, text)
    }


    private fun setupViewVisibility(hasItemsToShow: Boolean) {
        if (binding.groupPlaceholder.tag != AVOID_CHANGING_VISIBILITY_TAG) {
            binding.groupPlaceholder.beVisibleIf(!hasItemsToShow)
        }

        binding.groupPlaceholder.beVisibleIf(!hasItemsToShow)
        binding.groupList.beVisibleIf(hasItemsToShow)
    }

    private fun showNewGroupsDialog() {
        CreateNewGroupDialog(activity as BaseActivity<*>) {
            refreshItems()
        }
    }
}