package com.app.phonebook.presentation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun initView() {

    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }
}