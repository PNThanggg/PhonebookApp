package com.app.phonebook.base.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.app.phonebook.adapter.ContactsAdapter
import com.app.phonebook.adapter.RecentCallsAdapter
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getProperPrimaryColor
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.extension.getTextSize
import com.app.phonebook.base.utils.SORT_BY_FIRST_NAME
import com.app.phonebook.base.utils.SORT_BY_SURNAME
import com.app.phonebook.databinding.FragmentLettersLayoutBinding
import com.app.phonebook.databinding.FragmentRecentBinding
import com.app.phonebook.helpers.Config
import com.app.phonebook.presentation.activities.MainActivity
import com.app.phonebook.presentation.fragments.RecentFragment
import com.app.phonebook.presentation.view.MyRecyclerView

abstract class BaseViewPagerFragment<BINDING : BaseViewPagerFragment.InnerBinding>(
    context: Context, attributeSet: AttributeSet
) : RelativeLayout(context, attributeSet) {
    protected var activity: BaseActivity<*>? = null
    protected lateinit var innerBinding: BINDING
    private lateinit var config: Config

    fun setupFragment(activity: BaseActivity<*>) {
        config = activity.config
        if (this.activity == null) {
            this.activity = activity

            setupFragment()
            setupColors(
                activity.getProperTextColor(),
                activity.getProperPrimaryColor(),
                activity.getProperPrimaryColor()
            )
        }
    }

    fun startNameWithSurnameChanged(startNameWithSurname: Boolean) {
        if (this !is RecentFragment) {
            (innerBinding.fragmentList?.adapter as? ContactsAdapter)?.apply {
                config.sorting = if (startNameWithSurname) SORT_BY_SURNAME else SORT_BY_FIRST_NAME
                (this@BaseViewPagerFragment.activity!! as MainActivity).refreshFragments()
            }
        }
    }

    fun finishActMode() {
        (innerBinding.fragmentList?.adapter as? BaseRecyclerViewAdapter)?.finishActMode()
        (innerBinding.recentList?.adapter as? BaseRecyclerViewAdapter)?.finishActMode()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fontSizeChanged() {
        if (this is RecentFragment) {
            (innerBinding.recentList.adapter as? RecentCallsAdapter)?.apply {
                fontSize = activity.getTextSize()
                notifyDataSetChanged()
            }
        } else {
            (innerBinding.fragmentList?.adapter as? ContactsAdapter)?.apply {
                fontSize = activity.getTextSize()
                notifyDataSetChanged()
            }
        }
    }

    abstract fun setupFragment()

    abstract fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int)

    abstract fun onSearchClosed()

    abstract fun onSearchQueryChanged(context: Context, text: String)

    interface InnerBinding {
        val fragmentList: MyRecyclerView?
        val recentList: MyRecyclerView?
    }

    class LettersInnerBinding(binding: FragmentLettersLayoutBinding) : InnerBinding {
        override val fragmentList: MyRecyclerView = binding.fragmentList
        override val recentList = null
    }

    class RecentInnerBinding(binding: FragmentRecentBinding) : InnerBinding {
        override val fragmentList = null
        override val recentList = binding.recentsList
    }
}
