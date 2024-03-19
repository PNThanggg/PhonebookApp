package com.app.phonebook.presentation.activities

import android.os.Bundle
import android.view.LayoutInflater
import com.app.phonebook.adapter.ConferenceCallsAdapter
import com.app.phonebook.base.utils.NavigationIcon
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.ActivityConferenceBinding
import com.app.phonebook.helpers.CallManager

class ConferenceActivity : BaseActivity<ActivityConferenceBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        binding.apply {
            updateMaterialActivityViews(
                conferenceCoordinator,
                conferenceList,
                useTransparentNavigation = true,
                useTopSearchMenu = false
            )
            setupMaterialScrollListener(conferenceList, conferenceToolbar)
            conferenceList.adapter = ConferenceCallsAdapter(
                this@ConferenceActivity,
                conferenceList,
                ArrayList(
                    CallManager.getConferenceCalls()
                )
            ) {}
        }
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun beforeCreate() {
        isMaterialActivity = true
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityConferenceBinding {
        return ActivityConferenceBinding.inflate(inflater)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.conferenceToolbar, NavigationIcon.Arrow)
    }
}