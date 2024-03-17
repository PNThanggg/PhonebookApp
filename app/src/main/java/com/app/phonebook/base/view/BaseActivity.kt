package com.app.phonebook.base.view

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.app.phonebook.R
import com.app.phonebook.base.extension.addBit
import com.app.phonebook.base.extension.adjustAlpha
import com.app.phonebook.base.extension.applyColorFilter
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.finishWithSlide
import com.app.phonebook.base.extension.getAvailableSIMCardLabels
import com.app.phonebook.base.extension.getColoredDrawableWithColor
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getPermissionString
import com.app.phonebook.base.extension.getProperBackgroundColor
import com.app.phonebook.base.extension.getProperStatusBarColor
import com.app.phonebook.base.extension.getThemeId
import com.app.phonebook.base.extension.handleBackPressed
import com.app.phonebook.base.extension.hasPermission
import com.app.phonebook.base.extension.launchActivityIntent
import com.app.phonebook.base.extension.removeBit
import com.app.phonebook.base.extension.showErrorToast
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.DARK_GREY
import com.app.phonebook.base.utils.HIGHER_ALPHA
import com.app.phonebook.base.utils.PERMISSION_CALL_PHONE
import com.app.phonebook.base.utils.PERMISSION_POST_NOTIFICATIONS
import com.app.phonebook.base.utils.PERMISSION_READ_PHONE_STATE
import com.app.phonebook.base.utils.REQUEST_CODE_SET_DEFAULT_CALLER_ID
import com.app.phonebook.base.utils.REQUEST_CODE_SET_DEFAULT_DIALER
import com.app.phonebook.base.utils.isQPlus
import com.app.phonebook.base.utils.isRPlus
import com.app.phonebook.base.utils.isTiramisuPlus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    //region variable
    companion object {
        private const val TIME_DELAY_CLICK = 200L
        private const val GENERIC_PERM_HANDLER = 100
    }

    lateinit var binding: VB
    private var isAvailableClick = true
    var useTopSearchMenu = false

    var actionOnPermission: ((granted: Boolean) -> Unit)? = null

    var isAskingPermissions = false

    var isMaterialActivity = false
    var useDynamicTheme = true
    var showTransparentTop = false

    //endregion


    override fun onCreate(savedInstanceState: Bundle?) {
        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))
        }

        beforeCreate()

        super.onCreate(savedInstanceState)

        binding = inflateViewBinding(layoutInflater)
        setContentView(binding.root)

        initView(savedInstanceState)
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

    abstract fun initView(savedInstanceState: Bundle?)
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
            setTaskDescription(
                ActivityManager.TaskDescription.Builder().setStatusBarColor(color).build()
            )
        } else {
            setTaskDescription(ActivityManager.TaskDescription(null, null, color))
        }
    }

    fun updateNavigationBarColor(color: Int) {
        window.navigationBarColor = color
        updateNavigationBarButtons(color)
    }

    @Suppress("DEPRECATION")
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

    fun updateMenuItemColors(
        menu: Menu?,
        baseColor: Int = getProperStatusBarColor(),
        forceWhiteIcons: Boolean = false
    ) {
        if (menu == null) {
            return
        }

        var color = baseColor.getContrastColor()
        if (forceWhiteIcons) {
            color = Color.WHITE
        }

        for (i in 0 until menu.size()) {
            try {
                menu.getItem(i)?.icon?.setTint(color)
            } catch (ignored: Exception) {
                Log.e(APP_NAME, "updateMenuItemColors: ${ignored.message}")
            }
        }
    }

    fun handlePermission(permissionId: Int, callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(
                this,
                arrayOf(getPermissionString(permissionId)),
                GENERIC_PERM_HANDLER
            )
        }
    }

    fun launchCallIntent(recipient: String, handle: PhoneAccountHandle? = null) {
        handlePermission(PERMISSION_CALL_PHONE) {
            val action = if (it) Intent.ACTION_CALL else Intent.ACTION_DIAL
            Intent(action).apply {
                data = Uri.fromParts("tel", recipient, null)

                if (handle != null) {
                    putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
                }

//                if (isDefaultDialer()) {
//                    val packageName = if (baseConfig.appId.contains(
//                            ".debug",
//                            true
//                        )
//                    ) "com.simplemobiletools.dialer.debug" else "com.simplemobiletools.dialer"
//                    val className = "com.simplemobiletools.dialer.activities.DialerActivity"
//                    setClassName(packageName, className)
//                }

                launchActivityIntent(this)
            }
        }
    }

    fun callContactWithSim(recipient: String, useMainSIM: Boolean) {
        handlePermission(PERMISSION_READ_PHONE_STATE) {
            val wantedSimIndex = if (useMainSIM) 0 else 1
            val handle =
                getAvailableSIMCardLabels().sortedBy { it.id }.getOrNull(wantedSimIndex)?.handle
            launchCallIntent(recipient, handle)
        }
    }

    fun startCallIntent(recipient: String) {
        launchCallIntent(recipient, null)

//        if (isDefaultDialer()) {
//            getHandleToUse(null, recipient) { handle ->
//                launchCallIntent(recipient, handle)
//            }
//        } else {
//            launchCallIntent(recipient, null)
//        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setDefaultCallerIdApp() {
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_CALLER_ID)
        }
    }

    fun handleNotificationPermission(callback: (granted: Boolean) -> Unit) {
        if (!isTiramisuPlus()) {
            callback(true)
        } else {
            handlePermission(PERMISSION_POST_NOTIFICATIONS) { granted ->
                callback(granted)
            }
        }
    }

    @SuppressLint("InlinedApi")
    protected fun launchSetDefaultDialerIntent() {
        if (isQPlus()) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            ).apply {
                try {
                    startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.no_app_found)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }
}