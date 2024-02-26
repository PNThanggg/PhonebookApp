package com.app.phonebook.presentation

import android.view.LayoutInflater
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

}