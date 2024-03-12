package com.app.phonebook.presentation.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.app.phonebook.R
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.checkAppIconColor
import com.app.phonebook.base.extension.getSharedTheme
import com.app.phonebook.base.extension.isUsingSystemDarkTheme
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun initView() {
        if (!baseConfig.isUsingAutoTheme && !baseConfig.isUsingSystemTheme) {
            getSharedTheme {
                if (it != null) {
                    baseConfig.apply {
                        wasSharedThemeForced = true
                        isUsingSharedTheme = true
                        wasSharedThemeEverActivated = true

                        textColor = it.textColor
                        backgroundColor = it.backgroundColor
                        primaryColor = it.primaryColor
                        accentColor = it.accentColor
                    }

                    if (baseConfig.appIconColor != it.appIconColor) {
                        baseConfig.appIconColor = it.appIconColor
                        checkAppIconColor()
                    }
                }
                initActivity()
            }
        } else {
            initActivity()
        }
    }

    override fun initData() {
        baseConfig.apply {
            if (isUsingAutoTheme) {
                val isUsingSystemDarkTheme = isUsingSystemDarkTheme()
                isUsingSharedTheme = false

                textColor = resources.getColor(
                    if (isUsingSystemDarkTheme) R.color.theme_dark_text_color else R.color.theme_light_text_color,
                    theme,
                )

                backgroundColor = resources.getColor(
                    if (isUsingSystemDarkTheme) R.color.theme_dark_background_color else R.color.theme_light_background_color,
                    theme,
                )
            }
        }
    }

    override fun initListener() {
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }

    private fun initActivity(): Unit {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)

            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}