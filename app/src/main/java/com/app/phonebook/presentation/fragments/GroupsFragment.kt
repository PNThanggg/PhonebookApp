package com.app.phonebook.presentation.fragments

import android.content.Context
import android.util.AttributeSet
import com.app.phonebook.R
import com.app.phonebook.adapter.MyRecyclerViewAdapter
import com.app.phonebook.base.extension.beGone
import com.app.phonebook.base.extension.beVisible
import com.app.phonebook.base.extension.getColorStateList
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.hasPermission
import com.app.phonebook.base.extension.underlineText
import com.app.phonebook.base.interfaces.RefreshItemsListener
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.RecentCall
import com.app.phonebook.databinding.FragmentGroupsBinding
import com.app.phonebook.databinding.FragmentLettersLayoutBinding

class GroupsFragment(
    context: Context, attributeSet: AttributeSet
) : MyViewPagerFragment<MyViewPagerFragment.LettersInnerBinding>(context, attributeSet), RefreshItemsListener {

    private lateinit var binding: FragmentLettersLayoutBinding


    private var allRecentCalls = listOf<RecentCall>()

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

                underlineText()
            }
        }
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {

        binding.apply {
            fragmentPlaceholder.setTextColor(textColor)
            (fragmentList.adapter as? MyRecyclerViewAdapter)?.updateTextColor(textColor)

            letterFastScroller.textColor = textColor.getColorStateList()
            letterFastScroller.pressedTextColor = properPrimaryColor

            letterFastScrollerThumb.setupWithFastScroller(letterFastScroller)
            letterFastScrollerThumb.textColor = properPrimaryColor.getContrastColor()
            letterFastScrollerThumb.thumbColor = properPrimaryColor.getColorStateList()
        }
    }

    override fun onSearchClosed() {
    }

    override fun onSearchQueryChanged(text: String) {

    }

    override fun refreshItems(callback: (() -> Unit)?) {
        val allGroup: ArrayList<Contact> = ArrayList()
        activity?.runOnUiThread {
            getGroup(allGroup)
            callback?.invoke()
        }
    }

    private fun getGroup(contacts: ArrayList<Contact>) {
        if (contacts.isEmpty()) {
            binding.apply {
                fragmentPlaceholder.beVisible()
                fragmentPlaceholder2.beVisible()
                fragmentList.beGone()
            }
        }
    }
}
