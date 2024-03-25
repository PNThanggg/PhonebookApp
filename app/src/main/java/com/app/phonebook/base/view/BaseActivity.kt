package com.app.phonebook.base.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ScrollingView
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.app.phonebook.R
import com.app.phonebook.base.extension.addBit
import com.app.phonebook.base.extension.adjustAlpha
import com.app.phonebook.base.extension.applyColorFilter
import com.app.phonebook.base.extension.baseConfig
import com.app.phonebook.base.extension.finishWithSlide
import com.app.phonebook.base.extension.getAvailableSIMCardLabels
import com.app.phonebook.base.extension.getColoredDrawableWithColor
import com.app.phonebook.base.extension.getColoredMaterialStatusBarColor
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getHandleToUse
import com.app.phonebook.base.extension.getPermissionString
import com.app.phonebook.base.extension.getPhoneNumberTypeText
import com.app.phonebook.base.extension.getProperBackgroundColor
import com.app.phonebook.base.extension.getProperStatusBarColor
import com.app.phonebook.base.extension.handleBackPressed
import com.app.phonebook.base.extension.hasPermission
import com.app.phonebook.base.extension.hideKeyboard
import com.app.phonebook.base.extension.isDefaultDialer
import com.app.phonebook.base.extension.isUsingGestureNavigation
import com.app.phonebook.base.extension.launchActivityIntent
import com.app.phonebook.base.extension.navigationBarHeight
import com.app.phonebook.base.extension.onApplyWindowInsets
import com.app.phonebook.base.extension.removeBit
import com.app.phonebook.base.extension.showErrorToast
import com.app.phonebook.base.extension.statusBarHeight
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.DARK_GREY
import com.app.phonebook.base.utils.HIGHER_ALPHA
import com.app.phonebook.base.utils.MEDIUM_ALPHA
import com.app.phonebook.base.utils.NavigationIcon
import com.app.phonebook.base.utils.PERMISSION_CALL_PHONE
import com.app.phonebook.base.utils.PERMISSION_POST_NOTIFICATIONS
import com.app.phonebook.base.utils.PERMISSION_READ_PHONE_STATE
import com.app.phonebook.base.utils.REQUEST_CODE_SET_DEFAULT_CALLER_ID
import com.app.phonebook.base.utils.REQUEST_CODE_SET_DEFAULT_DIALER
import com.app.phonebook.base.utils.isQPlus
import com.app.phonebook.base.utils.isRPlus
import com.app.phonebook.base.utils.isTiramisuPlus
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.RadioItem
import com.app.phonebook.presentation.dialog.RadioGroupDialog
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
    private var useTopSearchMenu = false

    private var actionOnPermission: ((granted: Boolean) -> Unit)? = null

    private var isAskingPermissions = false

    var isMaterialActivity = false
    private var showTransparentTop = false

    private var mainCoordinatorLayout: CoordinatorLayout? = null
    private var nestedView: View? = null
    private var scrollingView: ScrollingView? = null
    private var toolbar: Toolbar? = null
    private var useTransparentNavigation = false

    private var materialScrollColorAnimation: ValueAnimator? = null
    var copyMoveCallback: ((destinationPath: String) -> Unit)? = null
    var checkedDocumentPath = ""
    var configItemsToExport = LinkedHashMap<String, Any>()

    private var currentScrollY = 0
    //endregion


    override fun onCreate(savedInstanceState: Bundle?) {
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
    private fun updateNavigationBarButtons(color: Int) {
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
    private fun updateActionbarColor(color: Int = getProperStatusBarColor()) {
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

        if (isDefaultDialer()) {
            getHandleToUse(null, recipient) { handle ->
                launchCallIntent(recipient, handle)
            }
        } else {
            launchCallIntent(recipient, null)
        }
    }

    /**
     * Determines the appropriate status bar color based on the scroll position of the view.
     *
     * This function checks if the current scrolling view (either a `RecyclerView` or a `NestedScrollView`)
     * is at the top (i.e., has not been scrolled). If the scrolling view is at the top, it returns a background
     * color appropriate for the current theme or context, obtained from `getProperBackgroundColor()`. If the
     * view has been scrolled, it returns a status bar color that matches the primary color of the app's theme,
     * obtained from `getColoredMaterialStatusBarColor()`.
     *
     * This approach allows for dynamic adjustment of the status bar's appearance to reflect the primary color
     * of the app when the content is scrolled, providing a visual indication of scrolling while maintaining
     * a cohesive appearance with the rest of the app's theme when at the top.
     *
     * @return An integer representing the ARGB color value for the status bar, determined by the current scroll
     * position of the view.
     *
     * Note: This function should be called whenever the scroll position of the view might change, such as in
     * a scroll listener or after updating the content of the view.
     */
    private fun getRequiredStatusBarColor(): Int {
        return if ((scrollingView is RecyclerView || scrollingView is NestedScrollView) && scrollingView?.computeVerticalScrollOffset() == 0) {
            getProperBackgroundColor()
        } else {
            getColoredMaterialStatusBarColor()
        }
    }


    // use translucent navigation bar, set the background color to action and status bars
    fun updateMaterialActivityViews(
        mainCoordinatorLayout: CoordinatorLayout?,
        nestedView: View?,
        useTransparentNavigation: Boolean,
        useTopSearchMenu: Boolean,
    ) {
        this.mainCoordinatorLayout = mainCoordinatorLayout
        this.nestedView = nestedView
        this.useTransparentNavigation = useTransparentNavigation
        this.useTopSearchMenu = useTopSearchMenu

        handleNavigationAndScrolling()

        val backgroundColor = getProperBackgroundColor()
        updateStatusBarColor(backgroundColor)
        updateActionbarColor(backgroundColor)
    }

    private fun animateTopBarColors(colorFrom: Int, colorTo: Int) {
        if (toolbar == null) {
            return
        }

        materialScrollColorAnimation?.end()
        materialScrollColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        materialScrollColorAnimation!!.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            if (toolbar != null) {
                updateTopBarColors(toolbar!!, color)
            }
        }

        materialScrollColorAnimation!!.start()
    }

    // colorize the top toolbar and status bar at scrolling down a bit
    fun setupMaterialScrollListener(scrollingView: ScrollingView?, toolbar: Toolbar) {
        this.scrollingView = scrollingView
        this.toolbar = toolbar
        if (scrollingView is RecyclerView) {
            scrollingView.setOnScrollChangeListener { _, _, _, _, _ ->
                val newScrollY = scrollingView.computeVerticalScrollOffset()
                scrollingChanged(newScrollY, currentScrollY)
                currentScrollY = newScrollY
            }
        } else if (scrollingView is NestedScrollView) {
            scrollingView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                scrollingChanged(scrollY, oldScrollY)
            }
        }
    }

    private fun scrollingChanged(newScrollY: Int, oldScrollY: Int) {
        if (newScrollY > 0 && oldScrollY == 0) {
            val colorFrom = window.statusBarColor
            val colorTo = getColoredMaterialStatusBarColor()
            animateTopBarColors(colorFrom, colorTo)
        } else if (newScrollY == 0 && oldScrollY > 0) {
            val colorFrom = window.statusBarColor
            val colorTo = getRequiredStatusBarColor()
            animateTopBarColors(colorFrom, colorTo)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Suppress("DEPRECATION")
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
    @Suppress("DEPRECATION")
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

    @Suppress("DEPRECATION")
    fun setupToolbar(
        toolbar: Toolbar,
        toolbarNavigationIcon: NavigationIcon = NavigationIcon.None,
        statusBarColor: Int = getRequiredStatusBarColor(),
        searchMenuItem: MenuItem? = null
    ) {
        val contrastColor = statusBarColor.getContrastColor()
        if (toolbarNavigationIcon != NavigationIcon.None) {
            val drawableId = if (toolbarNavigationIcon == NavigationIcon.Cross) {
                R.drawable.ic_cross_vector
            } else {
                R.drawable.ic_arrow_left_vector
            }
            toolbar.navigationIcon = resources.getColoredDrawableWithColor(
                drawableId = drawableId,
                color = contrastColor,
                context = this@BaseActivity
            )
            toolbar.setNavigationContentDescription(toolbarNavigationIcon.accessibilityResId)
        }

        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            finish()
        }

        updateTopBarColors(toolbar, statusBarColor)

        if (!useTopSearchMenu) {
            searchMenuItem?.actionView?.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.apply {
                applyColorFilter(contrastColor)
            }

            searchMenuItem?.actionView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
                setTextColor(contrastColor)
                setHintTextColor(contrastColor.adjustAlpha(MEDIUM_ALPHA))
                hint = "${getString(R.string.search)}…"

                if (isQPlus()) {
                    textCursorDrawable = null
                }
            }

            // search underline
            searchMenuItem?.actionView?.findViewById<View>(androidx.appcompat.R.id.search_plate)?.apply {
                background.setColorFilter(contrastColor, PorterDuff.Mode.MULTIPLY)
            }
        }
    }

    fun initiateCall(contact: Contact, onStartCallIntent: (phoneNumber: String) -> Unit) {
        val numbers = contact.phoneNumbers
        if (numbers.size == 1) {
            onStartCallIntent(numbers.first().value)
        } else if (numbers.size > 1) {
            val primaryNumber = contact.phoneNumbers.find { it.isPrimary }
            if (primaryNumber != null) {
                onStartCallIntent(primaryNumber.value)
            } else {
                val items = ArrayList<RadioItem>()
                numbers.forEachIndexed { index, phoneNumber ->
                    items.add(
                        RadioItem(
                            index,
                            "${phoneNumber.value} (${getPhoneNumberTypeText(phoneNumber.type, phoneNumber.label)})",
                            phoneNumber.value
                        )
                    )
                }

                RadioGroupDialog(this, items) {
                    onStartCallIntent(it as String)
                }
            }
        }
    }

    private fun updateTopBottomInsets(top: Int, bottom: Int) {
        nestedView?.run {
            setPadding(paddingLeft, paddingTop, paddingRight, bottom)
        }
        (mainCoordinatorLayout?.layoutParams as? FrameLayout.LayoutParams)?.topMargin = top
    }

    @Suppress("DEPRECATION")
    private fun handleNavigationAndScrolling() {
        if (useTransparentNavigation) {
            if (navigationBarHeight > 0 || isUsingGestureNavigation()) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                updateTopBottomInsets(statusBarHeight, navigationBarHeight)
                // Don't touch this. Window Inset API often has a domino effect and things will most likely break.
                onApplyWindowInsets {
                    val insets = it.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
                    updateTopBottomInsets(insets.top, insets.bottom)
                }
            } else {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                updateTopBottomInsets(0, 0)
            }
        }
    }
}