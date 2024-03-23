package com.app.phonebook.presentation.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.app.phonebook.R
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.getSharedTheme
import com.app.phonebook.base.extension.isUsingSystemDarkTheme
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
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
                    if (isUsingSystemDarkTheme) {
                        R.color.theme_dark_text_color
                    } else {
                        R.color.theme_light_text_color
                    },
                    theme,
                )

                backgroundColor = resources.getColor(
                    if (isUsingSystemDarkTheme) {
                        R.color.theme_dark_background_color
                    } else {
                        R.color.theme_light_background_color
                    },
                    theme,
                )
            }
        }
    }

    override fun initListener() {
    }

    override fun beforeCreate() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }

    private fun initActivity() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)

            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}