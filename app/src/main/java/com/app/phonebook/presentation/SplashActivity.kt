package com.app.phonebook.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun initView() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)

            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }
}