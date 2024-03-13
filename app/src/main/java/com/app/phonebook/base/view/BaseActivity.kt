package com.app.phonebook.base.view

import android.app.ActivityManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.app.phonebook.R
import com.app.phonebook.base.extension.addBit
import com.app.phonebook.base.extension.adjustAlpha
import com.app.phonebook.base.extension.applyColorFilter
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.finishWithSlide
import com.app.phonebook.base.extension.getColoredDrawableWithColor
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getProperBackgroundColor
import com.app.phonebook.base.extension.getProperStatusBarColor
import com.app.phonebook.base.extension.getThemeId
import com.app.phonebook.base.extension.handleBackPressed
import com.app.phonebook.base.extension.removeBit
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.DARK_GREY
import com.app.phonebook.base.utils.HIGHER_ALPHA
import com.app.phonebook.base.utils.isRPlus
import com.app.phonebook.base.utils.isTiramisuPlus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    //region variable
    companion object {
        const val TIME_DELAY_CLICK = 200L
    }

    private lateinit var binding: VB
    private var isAvailableClick = true
    private var useTopSearchMenu = false
    //endregion

    var isMaterialActivity = false
    var useDynamicTheme = true
    var showTransparentTop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))
        }

        beforeCreate()

        super.onCreate(savedInstanceState)

        binding = inflateViewBinding(layoutInflater)
        setContentView(binding.root)

        initView()
        initData()
        initListener()

        handleBackPressed {
            onBack()
        }
    }

    open fun onBack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressedDispatcher.onBackPressed()
        } else {
            finishWithSlide()
        }
    }

    abstract fun initView()
    abstract fun initData()
    abstract fun initListener()

    abstract fun beforeCreate()

    /**override it and inflate your view binding, demo in MainActivity*/
    abstract fun inflateViewBinding(inflater: LayoutInflater): VB

    private fun delayClick() {
        lifecycleScope.launch(Dispatchers.IO) {
            isAvailableClick = false
            delay(TIME_DELAY_CLICK)
            isAvailableClick = true
        }
    }

    fun View.clickSafety(action: () -> Unit) {
        this.setOnClickListener {
            if (isAvailableClick) {
                action()
                delayClick()
            }
        }
    }

    fun updateTopBarColors(toolbar: Toolbar, color: Int) {
        val contrastColor = if (useTopSearchMenu) {
            getProperBackgroundColor().getContrastColor()
        } else {
            color.getContrastColor()
        }

        if (!useTopSearchMenu) {
            updateStatusBarColor(color)
            toolbar.setBackgroundColor(color)
            toolbar.setTitleTextColor(contrastColor)
            toolbar.navigationIcon?.applyColorFilter(contrastColor)
            toolbar.collapseIcon = resources.getColoredDrawableWithColor(
                drawableId = R.drawable.ic_arrow_left_vector,
                color = contrastColor,
                context = this@BaseActivity
            )
        }

        toolbar.overflowIcon = resources.getColoredDrawableWithColor(
            drawableId = R.drawable.ic_three_dots_vector,
            color = contrastColor,
            context = this@BaseActivity
        )

        val menu = toolbar.menu
        for (i in 0 until menu.size()) {
            try {
                menu.getItem(i)?.icon?.setTint(contrastColor)
            } catch (ignored: Exception) {
                Log.e(APP_NAME, "updateTopBarColors: ${ignored.message}")
            }
        }
    }

    fun updateBackgroundColor(color: Int = baseConfig.backgroundColor) {
        window.decorView.setBackgroundColor(color)
    }

    @Suppress("DEPRECATION")
    fun updateStatusBarColor(color: Int) {
        window.statusBarColor = color

        if (isRPlus()) {
            val controller = window.insetsController
            if (color.getContrastColor() == DARK_GREY) {
                controller?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                controller?.setSystemBarsAppearance(
                    0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else {
            if (color.getContrastColor() == DARK_GREY) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            } else {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun updateNavigationBarButtons(color: Int) {
        if (isRPlus()) {
            val controller = window.insetsController
            if (color.getContrastColor() == DARK_GREY) {
                controller?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                controller?.setSystemBarsAppearance(
                    0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else {
            if (color.getContrastColor() == DARK_GREY) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            } else {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun updateActionbarColor(color: Int = getProperStatusBarColor()) {
        updateStatusBarColor(color)
        if (isTiramisuPlus()) {
            setTaskDescription(ActivityManager.TaskDescription.Builder().setStatusBarColor(color).build())
        } else {
            setTaskDescription(ActivityManager.TaskDescription(null, null, color))
        }
    }

    fun updateNavigationBarColor(color: Int) {
        window.navigationBarColor = color
        updateNavigationBarButtons(color)
    }

    override fun onResume() {
        super.onResume()

        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))

            val backgroundColor = if (baseConfig.isUsingSystemTheme) {
                resources.getColor(R.color.you_background_color, theme)
            } else {
                baseConfig.backgroundColor
            }

            updateBackgroundColor(backgroundColor)
        }

        if (showTransparentTop) {
            window.statusBarColor = Color.TRANSPARENT
        } else if (!isMaterialActivity) {
            val color = if (baseConfig.isUsingSystemTheme) {
                if (baseConfig.isUsingSystemTheme) {
                    resources.getColor(R.color.you_background_color, theme)
                } else {
                    resources.getColor(R.color.you_status_bar_color)
                }
            } else {
                getProperStatusBarColor()
            }

            updateActionbarColor(color)
        }

        var navBarColor = getProperBackgroundColor()
        if (isMaterialActivity) {
            navBarColor = navBarColor.adjustAlpha(HIGHER_ALPHA)
        }

        updateNavigationBarColor(navBarColor)
    }
}