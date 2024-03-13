package com.app.phonebook.presentation.activities

import android.view.LayoutInflater
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import com.app.phonebook.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var launchedDialer = false
    private var storedShowTabs = 0
    private var storedFontSize = 0
    private var storedStartNameWithSurname = false
    var cachedContacts = ArrayList<Contact>()


    override fun initView() {
        isMaterialActivity = true
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun beforeCreate() {
        isMaterialActivity = true
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }
}