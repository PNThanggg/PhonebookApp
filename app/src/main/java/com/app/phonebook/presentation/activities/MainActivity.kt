package com.app.phonebook.presentation.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.app.phonebook.R
import com.app.phonebook.adapter.ViewPagerAdapter
import com.app.phonebook.base.extension.beGoneIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.darkenColor
import com.app.phonebook.base.extension.getBottomNavigationBackgroundColor
import com.app.phonebook.base.extension.getColoredDrawableWithColor
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getProperBackgroundColor
import com.app.phonebook.base.extension.getProperPrimaryColor
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.extension.isDefaultDialer
import com.app.phonebook.base.extension.launchCreateNewContactIntent
import com.app.phonebook.base.extension.onGlobalLayout
import com.app.phonebook.base.extension.onTabSelectionChanged
import com.app.phonebook.base.extension.openNotificationSettings
import com.app.phonebook.base.extension.telecomManager
import com.app.phonebook.base.extension.toast
import com.app.phonebook.base.extension.updateBottomTabItemColors
import com.app.phonebook.base.extension.updateTextColors
import com.app.phonebook.base.helpers.AutoFitHelper
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.CONTACTS_GRID_MAX_COLUMNS_COUNT
import com.app.phonebook.base.utils.OPEN_DIAL_PAD_AT_LAUNCH
import com.app.phonebook.base.utils.PERMISSION_READ_CONTACTS
import com.app.phonebook.base.utils.REQUEST_CODE_SET_DEFAULT_CALLER_ID
import com.app.phonebook.base.utils.REQUEST_CODE_SET_DEFAULT_DIALER
import com.app.phonebook.base.utils.TAB_CALL_HISTORY
import com.app.phonebook.base.utils.TAB_CONTACTS
import com.app.phonebook.base.utils.TAB_FAVORITES
import com.app.phonebook.base.utils.TAB_LAST_USED
import com.app.phonebook.base.utils.VIEW_TYPE_GRID
import com.app.phonebook.base.utils.ensureBackgroundThread
import com.app.phonebook.base.utils.isQPlus
import com.app.phonebook.base.utils.tabsList
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseViewPagerFragment
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.RadioItem
import com.app.phonebook.databinding.ActivityMainBinding
import com.app.phonebook.helpers.RecentHelper
import com.app.phonebook.presentation.dialog.ChangeSortingDialog
import com.app.phonebook.presentation.dialog.ChangeViewTypeDialog
import com.app.phonebook.presentation.dialog.ConfirmationDialog
import com.app.phonebook.presentation.dialog.FilterContactSourcesDialog
import com.app.phonebook.presentation.dialog.PermissionRequiredDialog
import com.app.phonebook.presentation.dialog.RadioGroupDialog
import com.app.phonebook.presentation.fragments.ContactsFragment
import com.app.phonebook.presentation.fragments.FavoritesFragment
import com.app.phonebook.presentation.fragments.RecentFragment
import com.google.android.material.snackbar.Snackbar
import kotlin.system.exitProcess

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var launchedDialer = false
    private var storedShowTabs = 0
    private var storedFontSize = 0
    private var storedStartNameWithSurname = false
    var cachedContacts = ArrayList<Contact>()

    override fun initView(savedInstanceState: Bundle?) {
        setupOptionsMenu(context = this@MainActivity)
        refreshMenuItems()

        updateMaterialActivityViews(
            mainCoordinatorLayout = binding.mainCoordinator,
            nestedView = binding.mainHolder,
            useTransparentNavigation = false,
            useTopSearchMenu = true
        )

        launchedDialer = savedInstanceState?.getBoolean(OPEN_DIAL_PAD_AT_LAUNCH) ?: false

        if (isDefaultDialer()) {
            checkContactPermissions()

            if (!config.wasOverlaySnackbarConfirmed && !Settings.canDrawOverlays(this)) {
                val snackBar = Snackbar.make(
                    binding.mainHolder, R.string.allow_displaying_over_other_apps, Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.ok) {
                    config.wasOverlaySnackbarConfirmed = true
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                }

                snackBar.setBackgroundTint(getProperBackgroundColor().darkenColor())
                snackBar.setTextColor(getProperTextColor())
                snackBar.setActionTextColor(getProperTextColor())
                snackBar.show()
            }

            handleNotificationPermission { granted ->
                if (!granted) {
                    PermissionRequiredDialog(this, R.string.allow_notifications_incoming_calls, {
                        openNotificationSettings()
                    })
                }
            }
        } else {
            launchSetDefaultDialerIntent()
        }

        if (isQPlus()) {
            setDefaultCallerIdApp()
        }

        setupTabs()
        Contact.sorting = config.sorting
    }

    override fun onResume() {
        super.onResume()

        if (storedShowTabs != config.showTabs) {
            config.lastUsedViewPagerPage = 0
            exitProcess(0)
        }

        updateMenuColors()

        val properPrimaryColor = getProperPrimaryColor()
        val dialpadIcon = resources.getColoredDrawableWithColor(
            drawableId = R.drawable.ic_dialpad_vector,
            color = properPrimaryColor.getContrastColor(),
            context = this@MainActivity
        )

        binding.mainDialpadButton.setImageDrawable(dialpadIcon)

        updateTextColors(binding.mainHolder)
        setupTabColors()

        getAllFragments().forEach {
            it?.setupColors(
                getProperTextColor(),
                getProperPrimaryColor(),
                getProperPrimaryColor(),
            )
        }

        val configStartNameWithSurname = config.startNameWithSurname
        if (storedStartNameWithSurname != configStartNameWithSurname) {
            getContactsFragment()?.startNameWithSurnameChanged(configStartNameWithSurname)
            getFavoritesFragment()?.startNameWithSurnameChanged(configStartNameWithSurname)
            storedStartNameWithSurname = config.startNameWithSurname
        }

        if (!binding.mainMenu.isSearchOpen) {
            refreshItems(true)
        }

        val configFontSize = config.fontSize
        if (storedFontSize != configFontSize) {
            getAllFragments().forEach {
                it?.fontSizeChanged()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            getRecentsFragment()?.refreshItems()
        }, 2000)
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

    override fun onPause() {
        super.onPause()

        storedShowTabs = config.showTabs
        storedStartNameWithSurname = config.startNameWithSurname
        config.lastUsedViewPagerPage = binding.viewPager.currentItem
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // we don't really care about the result, the app can work without being the default Dialer too
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            checkContactPermissions()
        } else if (requestCode == REQUEST_CODE_SET_DEFAULT_CALLER_ID && resultCode != Activity.RESULT_OK) {
            toast(R.string.must_make_default_caller_id_app, length = Toast.LENGTH_LONG)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(OPEN_DIAL_PAD_AT_LAUNCH, launchedDialer)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshItems()
    }

    override fun onBack() {
        if (binding.mainMenu.isSearchOpen) {
            binding.mainMenu.closeSearch()
        }
    }

    private fun refreshMenuItems() {
        val currentFragment = getCurrentFragment()
        binding.mainMenu.getToolbar().menu.apply {
            findItem(R.id.clear_call_history).isVisible = currentFragment == getRecentsFragment()
            findItem(R.id.sort).isVisible = currentFragment != getRecentsFragment()
            findItem(R.id.create_new_contact).isVisible = currentFragment == getContactsFragment()
            findItem(R.id.change_view_type).isVisible = currentFragment == getFavoritesFragment()
            findItem(R.id.column_count).isVisible = currentFragment == getFavoritesFragment() && config.viewType == VIEW_TYPE_GRID
        }
    }

    private fun setupOptionsMenu(context: Context) {
        binding.mainMenu.apply {
            getToolbar().inflateMenu(R.menu.menu)
            toggleHideOnScroll(false)
            setupMenu()

            onSearchClosedListener = {
                getAllFragments().forEach {
                    it?.onSearchQueryChanged(
                        context = context, text = ""
                    )
                }
            }

            onSearchTextChangedListener = { text ->
                getCurrentFragment()?.onSearchQueryChanged(
                    text = text, context = context
                )
            }

            getToolbar().setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.clear_call_history -> clearCallHistory()
                    R.id.create_new_contact -> launchCreateNewContactIntent()
                    R.id.sort -> showSortingDialog(showCustomSorting = getCurrentFragment() is FavoritesFragment)
                    R.id.filter -> showFilterDialog()
                    R.id.change_view_type -> changeViewType()
                    R.id.column_count -> changeColumnCount()
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun changeColumnCount() {
        val items = ArrayList<RadioItem>()
        for (i in 1..CONTACTS_GRID_MAX_COLUMNS_COUNT) {
            items.add(RadioItem(i, resources.getQuantityString(R.plurals.column_counts, i, i)))
        }

        val currentColumnCount = config.contactsGridColumnCount
        RadioGroupDialog(this, ArrayList(items), currentColumnCount) {
            val newColumnCount = it as Int
            if (currentColumnCount != newColumnCount) {
                config.contactsGridColumnCount = newColumnCount
                getFavoritesFragment()?.columnCountChanged()
            }
        }
    }

    private fun changeViewType() {
        ChangeViewTypeDialog(this) {
            refreshMenuItems()
            getFavoritesFragment()?.refreshItems()
        }
    }

    private fun updateMenuColors() {
        updateStatusBarColor(getProperBackgroundColor())
        binding.mainMenu.updateColors()
    }

    private fun checkContactPermissions() {
        handlePermission(PERMISSION_READ_CONTACTS) {
            initFragments()
        }
    }

    private fun clearCallHistory() {
        val confirmationText = "${getString(R.string.clear_history_confirmation)}\n\n${getString(R.string.cannot_be_undone)}"
        ConfirmationDialog(this, confirmationText) {
            RecentHelper(this).removeAllRecentCalls(this) {
                runOnUiThread {
                    getRecentsFragment()?.refreshItems()
                }
            }
        }
    }

    private fun getTabIcon(position: Int): Drawable {
        val drawableId = when (position) {
            0 -> R.drawable.ic_person_vector
            1 -> R.drawable.ic_star_vector
            else -> R.drawable.ic_clock_vector
        }

        return resources.getColoredDrawableWithColor(
            drawableId = drawableId, color = getProperTextColor(), context = this@MainActivity
        )
    }

    private fun getTabLabel(position: Int): String {
        val stringId = when (position) {
            0 -> R.string.contacts_tab
            1 -> R.string.favorites_tab
            else -> R.string.call_history_tab
        }

        return resources.getString(stringId)
    }

    private fun getDeselectedTabDrawableIds(): ArrayList<Int> {
        val showTabs = config.showTabs
        val icons = ArrayList<Int>()

        if (showTabs and TAB_CONTACTS != 0) {
            icons.add(R.drawable.ic_person_outline_vector)
        }

        if (showTabs and TAB_FAVORITES != 0) {
            icons.add(R.drawable.ic_star_outline_vector)
        }

        if (showTabs and TAB_CALL_HISTORY != 0) {
            icons.add(R.drawable.ic_clock_vector)
        }

        return icons
    }

    private fun getSelectedTabDrawableIds(): List<Int> {
        val showTabs = config.showTabs
        val icons = mutableListOf<Int>()

        if (showTabs and TAB_CONTACTS != 0) {
            icons.add(R.drawable.ic_person_vector)
        }

        if (showTabs and TAB_FAVORITES != 0) {
            icons.add(R.drawable.ic_star_vector)
        }

        if (showTabs and TAB_CALL_HISTORY != 0) {
            icons.add(R.drawable.ic_clock_filled_vector)
        }

        return icons
    }

    private fun initFragments() {
        binding.viewPager.offscreenPageLimit = 2

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                binding.mainTabsHolder.getTabAt(position)?.select()
                getAllFragments().forEach {
                    it?.finishActMode()
                }
                refreshMenuItems()
            }
        })

        // selecting the proper tab sometimes glitches, add an extra selector to make sure we have it right
        binding.mainTabsHolder.onGlobalLayout {
            Looper.myLooper()?.let {
                Handler(it).postDelayed(
                    {
                        var wantedTab = getDefaultTab()

                        // open the Recents tab if we got here by clicking a missed call notification
                        if (intent.action == Intent.ACTION_VIEW && config.showTabs and TAB_CALL_HISTORY > 0) {
                            wantedTab = binding.mainTabsHolder.tabCount - 1

                            ensureBackgroundThread {
                                clearMissedCalls()
                            }
                        }

                        binding.mainTabsHolder.getTabAt(wantedTab)?.select()
                        refreshMenuItems()
                    },
                    100L,
                )
            }
        }

        binding.mainDialpadButton.setOnClickListener {
            launchDialpad()
        }

        binding.viewPager.onGlobalLayout {
            refreshMenuItems()
        }

        if (config.openDialPadAtLaunch && !launchedDialer) {
            launchDialpad()
            launchedDialer = true
        }
    }

    private fun setupTabs() {
        binding.viewPager.adapter = null
        binding.mainTabsHolder.removeAllTabs()
        tabsList.forEachIndexed { index, value ->
            if (config.showTabs and value != 0) {
                binding.mainTabsHolder.newTab().setCustomView(R.layout.bottom_tablayout_item).apply {
                    customView?.findViewById<ImageView>(R.id.tab_item_icon)?.setImageDrawable(getTabIcon(index))
                    customView?.findViewById<TextView>(R.id.tab_item_label)?.text = getTabLabel(index)
                    customView?.findViewById<TextView>(R.id.tab_item_label)?.let {
                        AutoFitHelper.create(it)
                    }
                    binding.mainTabsHolder.addTab(this)
                }
            }
        }

        binding.mainTabsHolder.onTabSelectionChanged(tabUnselectedAction = {
            updateBottomTabItemColors(
                it.customView, false, getDeselectedTabDrawableIds()[it.position]
            )
        }, tabSelectedAction = {
            binding.mainMenu.closeSearch()
            binding.viewPager.currentItem = it.position
            updateBottomTabItemColors(
                it.customView, true, getSelectedTabDrawableIds()[it.position]
            )
        })

        binding.mainTabsHolder.beGoneIf(binding.mainTabsHolder.tabCount == 1)
        storedShowTabs = config.showTabs
        storedStartNameWithSurname = config.startNameWithSurname
    }

    /**
     * Updates the colors of the tabs and the bottom navigation bar.
     *
     * This method performs the following tasks:
     * - Identifies the currently active tab based on the current item position in the ViewPager and updates its color.
     * - Iterates over all the inactive tabs and updates their colors accordingly.
     * - Sets the background color of the bottom navigation bar based on the determined interface color.
     *
     * Each tab is updated with a drawable indicating its active or inactive state, enhancing user awareness of the selected tab.
     *
     * The method ensures a cohesive and dynamic visual feedback on the tab selection by altering the appearance of the active tab and maintaining a visual consistency across the navigation elements.
     *
     * @see updateBottomTabItemColors Called to update the color of each tab based on its state (active/inactive).
     */
    private fun setupTabColors() {
        val activeView = binding.mainTabsHolder.getTabAt(binding.viewPager.currentItem)?.customView
        updateBottomTabItemColors(
            activeView, true, getSelectedTabDrawableIds()[binding.viewPager.currentItem]
        )

        getInactiveTabIndexes(binding.viewPager.currentItem).forEach { index ->
            val inactiveView = binding.mainTabsHolder.getTabAt(index)?.customView
            updateBottomTabItemColors(inactiveView, false, getDeselectedTabDrawableIds()[index])
        }

        val bottomBarColor = getBottomNavigationBackgroundColor(this@MainActivity)
        binding.mainTabsHolder.setBackgroundColor(bottomBarColor)
        updateNavigationBarColor(bottomBarColor)
    }

    private fun launchDialpad() {
        Intent(applicationContext, DialpadActivity::class.java).apply {
            startActivity(this)
        }
    }

    fun refreshFragments() {
        getContactsFragment()?.refreshItems()
        getFavoritesFragment()?.refreshItems()
        getRecentsFragment()?.refreshItems()
    }

    private fun getAllFragments(): ArrayList<BaseViewPagerFragment<*>?> {
        val showTabs = config.showTabs
        val fragments = arrayListOf<BaseViewPagerFragment<*>?>()

        if (showTabs and TAB_CONTACTS > 0) {
            fragments.add(getContactsFragment())
        }

        if (showTabs and TAB_FAVORITES > 0) {
            fragments.add(getFavoritesFragment())
        }

        if (showTabs and TAB_CALL_HISTORY > 0) {
            fragments.add(getRecentsFragment())
        }

        return fragments
    }

    private fun getInactiveTabIndexes(activeIndex: Int) = (0 until binding.mainTabsHolder.tabCount).filter {
        it != activeIndex
    }

    private fun getCurrentFragment(): BaseViewPagerFragment<*>? = getAllFragments().getOrNull(binding.viewPager.currentItem)

    private fun getContactsFragment(): ContactsFragment? = findViewById(R.id.contacts_fragment)

    private fun getFavoritesFragment(): FavoritesFragment? = findViewById(R.id.favorites_fragment)

    private fun getRecentsFragment(): RecentFragment? = findViewById(R.id.recents_fragment)

    /**
     * Determines and returns the default tab index based on user preferences and application configuration.
     *
     * This function calculates the default tab to be displayed in the main view of the application, taking into
     * consideration the user's last used tab, their preferred default tab, and which tabs are enabled
     * through the application's configuration. The tabs are identified by predefined constants such as
     * TAB_LAST_USED, TAB_CONTACTS, TAB_FAVORITES, and TAB_CALL_HISTORY.
     *
     * @return The index of the default tab to be displayed. The index is determined based on the following logic:
     *         - If the default tab is set to TAB_LAST_USED, it returns the index of the last used tab if it's
     *           within the range of the available tabs; otherwise, it defaults to the first tab (index 0).
     *         - If the default tab is set to TAB_CONTACTS, it returns the index for the Contacts tab, which is 0.
     *         - If the default tab is set to TAB_FAVORITES and the TAB_CONTACTS is enabled, it returns 1,
     *           indicating the Favorites tab is the second tab; if TAB_CONTACTS is not enabled, it defaults to 0.
     *         - For other cases, especially when the default tab is set to TAB_CALL_HISTORY, it calculates the
     *           index based on which tabs (TAB_CONTACTS and TAB_FAVORITES) are enabled before it in the tabs
     *           configuration. It ensures the returned index accurately reflects the position of the CALL_HISTORY tab
     *           among the enabled tabs.
     *         - If none of the specific cases match, it defaults to the first tab (index 0).
     */
    private fun getDefaultTab(): Int {
        val showTabsMask = config.showTabs
        return when (config.defaultTab) {
            TAB_LAST_USED -> if (config.lastUsedViewPagerPage < binding.mainTabsHolder.tabCount) config.lastUsedViewPagerPage else 0
            TAB_CONTACTS -> 0
            TAB_FAVORITES -> if (showTabsMask and TAB_CONTACTS > 0) 1 else 0
            else -> {
                if (showTabsMask and TAB_CALL_HISTORY > 0) {
                    if (showTabsMask and TAB_CONTACTS > 0) {
                        if (showTabsMask and TAB_FAVORITES > 0) {
                            2
                        } else {
                            1
                        }
                    } else {
                        if (showTabsMask and TAB_FAVORITES > 0) {
                            1
                        } else {
                            0
                        }
                    }
                } else {
                    0
                }
            }
        }
    }

    /**
     * Refreshes the items in the main activity, potentially opening the last used tab.
     *
     * This function checks the state of the activity to ensure it is neither destroyed nor finishing.
     * If the activity is in a valid state, it proceeds to either set up the view pager with a new adapter
     * or directly refresh the fragments contained within it, based on the current state of the view pager adapter.
     * It optionally allows the view pager to open the last tab that was viewed, as specified by the user's
     * preference, or defaults to opening the tab determined by the `getDefaultTab` function.
     *
     * @param openLastTab A Boolean parameter that indicates whether the view pager should open the last tab
     *                    that was active before the activity was paused or destroyed. Defaults to false, in which
     *                    case the default tab specified by application logic is opened.
     *
     * Behavior:
     * 1. Checks if the activity is in a state where it can safely modify the UI. If the activity is either
     *    destroyed or finishing, the function returns early without making any changes.
     * 2. If the view pager does not have an adapter set, it initializes the adapter with a new instance of
     *    `ViewPagerAdapter` and sets the current item (tab) of the view pager based on the `openLastTab` parameter.
     *    - If `openLastTab` is true, sets the current item to `com.app.phonebook.base.compose.extensions.getConfig.lastUsedViewPagerPage`.
     *    - If `openLastTab` is false, uses the `getDefaultTab` function to determine which tab to display.
     * 3. After setting the adapter, or if the adapter is already set, calls `refreshFragments` to update the
     *    fragments within the view pager.
     * 4. Utilizes the `viewPager.onGlobalLayout` listener to ensure that `refreshFragments` is called once the
     *    layout has been fully initialized, applicable only when setting a new adapter.
     *
     * Note: This function is intended for use within the MainActivity to manage the display of tabs and the
     * content within those tabs, based on user interactions and application state.
     */
    private fun refreshItems(openLastTab: Boolean = false) {
        if (isDestroyed || isFinishing) {
            return
        }

        binding.apply {
            if (viewPager.adapter == null) {
                viewPager.adapter = ViewPagerAdapter(this@MainActivity)
                viewPager.currentItem = if (openLastTab) config.lastUsedViewPagerPage else getDefaultTab()
                viewPager.onGlobalLayout {
                    refreshFragments()
                }
            } else {
                refreshFragments()
            }
        }
    }

    /**
     * Clears missed calls notification.
     *
     * This function attempts to clear any missed call notifications using the `telecomManager`.
     * It requires the application to have permissions related to call management (e.g., CALL_PHONE).
     * If the application lacks these permissions, an exception will be thrown. However, this exception
     * is caught and logged, ensuring that the application's execution is not interrupted.
     *
     * Note: The use of @SuppressLint("MissingPermission") indicates that the function may not explicitly
     * check for permissions at the point of invocation, assuming permissions have been checked previously or
     * the function is operating in an environment where permissions are not required (e.g., an emulator).
     *
     * @suppress Indicates suppression of the MissingPermission lint warning, acknowledging that permission
     * checking is handled elsewhere or is not applicable.
     */
    @SuppressLint("MissingPermission")
    private fun clearMissedCalls() {
        try {
            telecomManager.cancelMissedCallsNotification()
        } catch (ignored: Exception) {
            Log.e(APP_NAME, "clearMissedCalls: ${ignored.message}")
        }
    }

    /**
     * Displays a dialog for changing the sorting order of contacts.
     *
     * This function presents a `ChangeSortingDialog` to the user, allowing them to select their preferred
     * sorting order for contacts. The dialog can optionally include custom sorting options based on the
     * `showCustomSorting` parameter. Upon confirming their selection, the application will refresh the contacts
     * displayed in the Favorites and Contacts fragments to reflect the new sorting order.
     *
     * If a search is currently active (indicated by the search component of the main menu being open), this
     * function also updates the search results in the currently visible fragment to align with the newly
     * selected sorting criteria. This is accomplished by invoking the `onSearchQueryChanged` method for the
     * current fragment, ensuring that the search results are consistent with both the search query and the
     * updated sorting order.
     *
     * @param showCustomSorting A boolean flag indicating whether custom sorting options should be included
     *                          in the sorting dialog. This allows for additional customization of the sorting
     *                          behavior beyond the default options.
     *
     * Usage:
     * This function is typically invoked in response to a user action, such as selecting a "Change Sorting"
     * option from a menu. It does not return a value, but directly affects the UI by updating the display of
     * contacts based on the selected sorting criteria and refreshing search results if necessary.
     */
    private fun showSortingDialog(showCustomSorting: Boolean) {
        ChangeSortingDialog(this, showCustomSorting) {
            getFavoritesFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(
                        context = this, text = binding.mainMenu.getCurrentQuery()
                    )
                }
            }

            getContactsFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(
                        context = this, text = binding.mainMenu.getCurrentQuery()
                    )
                }
            }
        }
    }

    /**
     * Displays a dialog allowing users to filter contact sources.
     *
     * This function launches a `FilterContactSourcesDialog` that enables users to select which contact sources
     * they wish to see in the application. Upon the dialog's dismissal, it triggers a refresh of the contacts list
     * in the Favorites, Contacts, and Recent fragments, if they exist.
     *
     * Additionally, if a search query is currently active in the main menu's search component, this function
     * will ensure that the search results are updated in the currently visible fragment to reflect any changes
     * made to the filter settings.
     *
     * The refresh and search query update are handled through callbacks defined in each fragment's `refreshItems`
     * method, and the `onSearchQueryChanged` method, respectively. This ensures that the application's UI
     * remains consistent with the user's filtering choices and search intentions.
     *
     * Usage:
     * This function is intended to be called when the user interacts with the UI element (e.g., a button or menu item)
     * designated for filtering contact sources. It does not return a value but updates the application's UI state
     * directly.
     */
    private fun showFilterDialog() {
        FilterContactSourcesDialog(this) {
            getFavoritesFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(
                        context = this, text = binding.mainMenu.getCurrentQuery()
                    )
                }
            }

            getContactsFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(
                        context = this, text = binding.mainMenu.getCurrentQuery()
                    )
                }
            }

            getRecentsFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(
                        context = this, text = binding.mainMenu.getCurrentQuery()
                    )
                }
            }
        }
    }

    /**
     * Caches a list of contacts.
     *
     * This function updates the cache with a new list of contacts. It first clears any existing contacts
     * in the cache and then adds all the contacts from the provided list. If an exception occurs during
     * the process, it logs the error message to the console.
     *
     * This approach ensures that the cache always reflects the most recent set of contacts provided to
     * this function, making it a critical component of the application's data management strategy, especially
     * in scenarios where access to the latest contact information is essential.
     *
     * @param contacts The list of [Contact] objects to be cached.
     *
     * Note: This function is designed to be safe and will not crash the application on failure. Instead,
     * it logs an error message with the application's log tag for debugging purposes.
     */
    fun cacheContacts(contacts: List<Contact>) {
        try {
            cachedContacts.clear()
            cachedContacts.addAll(contacts)
        } catch (e: Exception) {
            Log.e(APP_NAME, "cacheContacts: ${e.message}")
        }
    }
}