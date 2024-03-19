package com.app.phonebook.presentation.fragments

import android.content.Context
import android.util.AttributeSet
import com.app.phonebook.R
import com.app.phonebook.adapter.RecentCallsAdapter
import com.app.phonebook.base.extension.areSystemAnimationsEnabled
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.beGone
import com.app.phonebook.base.extension.beGoneIf
import com.app.phonebook.base.extension.beVisible
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.base.extension.hasPermission
import com.app.phonebook.base.extension.telephonyManager
import com.app.phonebook.base.extension.underlineText
import com.app.phonebook.base.interfaces.RefreshItemsListener
import com.app.phonebook.base.utils.MIN_RECENTS_THRESHOLD
import com.app.phonebook.base.utils.PERMISSION_READ_CALL_LOG
import com.app.phonebook.base.utils.SMT_PRIVATE
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseViewPagerFragment
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.RecentCall
import com.app.phonebook.databinding.FragmentRecentBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.MyContactsContentProvider
import com.app.phonebook.helpers.RecentHelper
import com.app.phonebook.helpers.hidePrivateContacts
import com.app.phonebook.helpers.setNamesIfEmpty
import com.app.phonebook.presentation.dialog.CallConfirmationDialog
import com.app.phonebook.presentation.view.MyRecyclerView

class RecentFragment(context: Context, attributeSet: AttributeSet) :
    BaseViewPagerFragment<BaseViewPagerFragment.RecentInnerBinding>(context, attributeSet),
    RefreshItemsListener {
    private lateinit var binding: FragmentRecentBinding
    private var allRecentCalls = listOf<RecentCall>()
    private var recentAdapter: RecentCallsAdapter? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = FragmentRecentBinding.bind(this)
        innerBinding = RecentInnerBinding(binding)
    }

    override fun setupFragment() {
        val placeholderResId = if (context.hasPermission(PERMISSION_READ_CALL_LOG)) {
            R.string.no_previous_calls
        } else {
            R.string.could_not_access_the_call_history
        }

        binding.recentsPlaceholder.text = context.getString(placeholderResId)
        binding.recentsPlaceholder2.apply {
            underlineText()
            setOnClickListener {
                requestCallLogPermission()
            }
        }
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
        binding.recentsPlaceholder.setTextColor(textColor)
        binding.recentsPlaceholder2.setTextColor(properPrimaryColor)

        recentAdapter?.apply {
            initDrawables()
            updateTextColor(textColor)
        }
    }

    override fun refreshItems(callback: (() -> Unit)?) {
        val privateCursor = context?.getMyContactsCursor(
            favoritesOnly = false,
            withPhoneNumbersOnly = true
        )

        val groupSubsequentCalls = context?.config?.groupSubsequentCalls ?: false
        val querySize = allRecentCalls.size.coerceAtLeast(MIN_RECENTS_THRESHOLD)
        RecentHelper(context).getRecentCalls(groupSubsequentCalls, querySize) { recent ->
            ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
                val privateContacts = MyContactsContentProvider.getContacts(privateCursor)

                allRecentCalls = recent.setNamesIfEmpty(
                    context = context,
                    contacts = contacts,
                    privateContacts = privateContacts,
                ).hidePrivateContacts(
                    privateContacts, SMT_PRIVATE in context.baseConfig.ignoredContactSources
                )

                activity?.runOnUiThread {
                    gotRecent(allRecentCalls)
                }
            }
        }
    }

    private fun gotRecent(listRecent: List<RecentCall>) {
        if (listRecent.isEmpty()) {
            binding.apply {
                recentsPlaceholder.beVisible()
                recentsPlaceholder2.beGoneIf(context.hasPermission(PERMISSION_READ_CALL_LOG))
                recentsList.beGone()
            }
        } else {
            binding.apply {
                recentsPlaceholder.beGone()
                recentsPlaceholder2.beGone()
                recentsList.beVisible()
            }

            if (binding.recentsList.adapter == null) {
                recentAdapter = RecentCallsAdapter(
                    activity as BaseActivity,
                    listRecent.toMutableList(),
                    binding.recentsList,
                    this,
                    true
                ) {
                    val recentCall = it as RecentCall
                    if (context.config.showCallConfirmation) {
                        CallConfirmationDialog(
                            activity = activity as BaseActivity,
                            callee = recentCall.name
                        ) {
                            activity?.launchCallIntent(recentCall.phoneNumber)
                        }
                    } else {
                        activity?.launchCallIntent(recentCall.phoneNumber)
                    }
                }

                binding.recentsList.adapter = recentAdapter

                if (context.areSystemAnimationsEnabled) {
                    binding.recentsList.scheduleLayoutAnimation()
                }

                binding.recentsList.endlessScrollListener =
                    object : MyRecyclerView.EndlessScrollListener {
                        override fun updateTop() {}

                        override fun updateBottom() {
                            getMoreRecentCalls()
                        }
                    }

            } else {
                recentAdapter?.updateItems(listRecent)
            }
        }
    }

    private fun getMoreRecentCalls() {
        val privateCursor = context?.getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
        val groupSubsequentCalls = context?.config?.groupSubsequentCalls ?: false
        val querySize = allRecentCalls.size.plus(MIN_RECENTS_THRESHOLD)
        RecentHelper(context).getRecentCalls(groupSubsequentCalls, querySize) { listRecent ->
            ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
                val privateContacts = MyContactsContentProvider.getContacts(privateCursor)

                allRecentCalls = listRecent.setNamesIfEmpty(
                    context = context,
                    contacts = contacts,
                    privateContacts = privateContacts
                ).hidePrivateContacts(
                    privateContacts, SMT_PRIVATE in context.baseConfig.ignoredContactSources
                )

                activity?.runOnUiThread {
                    gotRecent(allRecentCalls)
                }
            }
        }
    }

    private fun requestCallLogPermission() {
        activity?.handlePermission(PERMISSION_READ_CALL_LOG) {
            if (it) {
                binding.recentsPlaceholder.text = context.getString(R.string.no_previous_calls)
                binding.recentsPlaceholder2.beGone()

                val groupSubsequentCalls = context?.config?.groupSubsequentCalls ?: false
                RecentHelper(context).getRecentCalls(groupSubsequentCalls) { listRecent ->
                    activity?.runOnUiThread {
                        gotRecent(listRecent)
                    }
                }
            }
        }
    }

    override fun onSearchClosed() {
        binding.recentsPlaceholder.beVisibleIf(allRecentCalls.isEmpty())
        recentAdapter?.updateItems(allRecentCalls)
    }

    override fun onSearchQueryChanged(context: Context, text: String) {
        val recentCalls = allRecentCalls.filter {
            it.name.contains(text, true) || it.doesContainPhoneNumber(
                text = text,
                telephonyManager = context.telephonyManager
            )
        }.sortedByDescending {
            it.name.startsWith(text, true)
        }.toMutableList() as ArrayList<RecentCall>

        binding.recentsPlaceholder.beVisibleIf(recentCalls.isEmpty())
        recentAdapter?.updateItems(recentCalls, text)
    }
}
