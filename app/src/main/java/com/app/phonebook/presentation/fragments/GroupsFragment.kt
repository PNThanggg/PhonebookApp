package com.app.phonebook.presentation.fragments

import android.content.Context
import android.util.AttributeSet
import com.app.phonebook.base.view.BaseViewPagerFragment
import com.app.phonebook.databinding.FragmentGroupsBinding
import com.app.phonebook.databinding.FragmentLayoutBinding

class GroupsFragment(
    context: Context,
    attributeSet: AttributeSet
) : BaseViewPagerFragment<BaseViewPagerFragment.FragmentLayout>(context, attributeSet) {

    private lateinit var binding: FragmentGroupsBinding

    override fun setupFragment() {

    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
    }

    override fun onSearchClosed() {
    }

    override fun onSearchQueryChanged(context: Context, text: String) {
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = FragmentGroupsBinding.bind(this)
        innerBinding = FragmentLayout(FragmentLayoutBinding.bind(binding.root))
    }
}
