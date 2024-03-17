package com.app.phonebook.presentation.activities

import android.os.Bundle
import android.view.LayoutInflater
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.ActivityDialpadBinding

class DialpadActivity : BaseActivity<ActivityDialpadBinding>() {
    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun beforeCreate() {
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityDialpadBinding {
        return ActivityDialpadBinding.inflate(inflater)
    }
}