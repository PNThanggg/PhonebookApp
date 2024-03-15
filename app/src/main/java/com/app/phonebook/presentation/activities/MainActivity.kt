package com.app.phonebook.presentation.activities

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ScrollingView
import com.app.phonebook.R
import com.app.phonebook.base.extension.beGoneIf
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getColoredDrawableWithColor
import com.app.phonebook.base.extension.getProperBackgroundColor
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.extension.launchCreateNewContactIntent
import com.app.phonebook.base.extension.onTabSelectionChanged
import com.app.phonebook.base.extension.updateBottomTabItemColors
import com.app.phonebook.base.helpers.AutoFitHelper
import com.app.phonebook.base.utils.APP_NAME
import com.app.phonebook.base.utils.TAB_CALL_HISTORY
import com.app.phonebook.base.utils.TAB_CONTACTS
import com.app.phonebook.base.utils.TAB_FAVORITES
import com.app.phonebook.base.utils.VIEW_TYPE_GRID
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseViewPagerFragment
import com.app.phonebook.data.models.Contact
import com.app.phonebook.databinding.ActivityMainBinding
import com.app.phonebook.presentation.fragments.ContactsFragment
import com.app.phonebook.presentation.fragments.FavoritesFragment
import com.app.phonebook.presentation.fragments.RecentFragment

class MainActivity : BaseActivity<ActivityMainBinding>() {
    companion object {
        private val tabsList = arrayListOf(TAB_CONTACTS, TAB_FAVORITES, TAB_CALL_HISTORY)
    }

    private var launchedDialer = false
    private var storedShowTabs = 0
    private var storedFontSize = 0
    private var storedStartNameWithSurname = false
    var cachedContacts = ArrayList<Contact>()

    private var mainCoordinatorLayout: CoordinatorLayout? = null
    private var nestedView: View? = null
    private var scrollingView: ScrollingView? = null
    private var useTransparentNavigation = false

    override fun initView() {
        setupOptionsMenu(context = applicationContext)
        refreshMenuItems()

        updateMaterialActivityViews(
            binding.mainCoordinator,
            binding.mainHolder,
            useTransparentNavigation = false,
            useTopSearchMenu = true
        )

        setupTabs()
        Contact.sorting = config.sorting
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

    private fun setupOptionsMenu(context: Context) {
        binding.mainMenu.apply {
            getToolbar().inflateMenu(R.menu.menu)
            toggleHideOnScroll(false)
            setupMenu()

            onSearchClosedListener = {
                getAllFragments().forEach {
                    it?.onSearchQueryChanged(context = context, text = "")
                }
            }

            onSearchTextChangedListener = { text ->
                getCurrentFragment()?.onSearchQueryChanged(
                    text = text, context = context
                )
            }

            getToolbar().setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
//                    R.id.clear_call_history -> clearCallHistory()
                    R.id.create_new_contact -> launchCreateNewContactIntent()
//                    R.id.sort -> showSortingDialog(showCustomSorting = getCurrentFragment() is FavoritesFragment)
//                    R.id.filter -> showFilterDialog()
//
//                    R.id.change_view_type -> changeViewType()
//                    R.id.column_count -> changeColumnCount()
////                    R.id.about -> launchAbout()
////                    R.id.more_apps_from_us -> launchMoreAppsFromUsIntent()
////                    R.id.settings -> launchSettings()
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun getCurrentFragment(): BaseViewPagerFragment<*>? =
        getAllFragments().getOrNull(binding.viewPager.currentItem)

    private fun refreshMenuItems() {
        val currentFragment = getCurrentFragment()
        binding.mainMenu.getToolbar().menu.apply {
            findItem(R.id.clear_call_history).isVisible = currentFragment == getRecentsFragment()
            findItem(R.id.sort).isVisible = currentFragment != getRecentsFragment()
            findItem(R.id.create_new_contact).isVisible = currentFragment == getContactsFragment()
            findItem(R.id.change_view_type).isVisible = currentFragment == getFavoritesFragment()
            findItem(R.id.column_count).isVisible =
                currentFragment == getFavoritesFragment() && config.viewType == VIEW_TYPE_GRID
        }
    }

    // use translucent navigation bar, set the background color to action and status bars
    private fun updateMaterialActivityViews(
        mainCoordinatorLayout: CoordinatorLayout?,
        nestedView: View?,
        useTransparentNavigation: Boolean,
        useTopSearchMenu: Boolean,
    ) {
        this.mainCoordinatorLayout = mainCoordinatorLayout
        this.nestedView = nestedView
        this.useTransparentNavigation = useTransparentNavigation
        this.useTopSearchMenu = useTopSearchMenu

//        handleNavigationAndScrolling()

        val backgroundColor = getProperBackgroundColor()
        updateStatusBarColor(backgroundColor)
        updateActionbarColor(backgroundColor)
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


    private fun getContactsFragment(): ContactsFragment? = findViewById(R.id.contacts_fragment)

    private fun getFavoritesFragment(): FavoritesFragment? = findViewById(R.id.favorites_fragment)

    private fun getRecentsFragment(): RecentFragment? = findViewById(R.id.recents_fragment)

    fun cacheContacts(contacts: List<Contact>) {
        try {
            cachedContacts.clear()
            cachedContacts.addAll(contacts)
        } catch (e: Exception) {
            Log.e(APP_NAME, "cacheContacts: ${e.message}")
        }
    }

    fun refreshFragments() {
        getContactsFragment()?.refreshItems()
        getFavoritesFragment()?.refreshItems()
        getRecentsFragment()?.refreshItems()
    }

    private fun getTabIcon(position: Int): Drawable {
        val drawableId = when (position) {
            0 -> R.drawable.ic_person_vector
            1 -> R.drawable.ic_star_vector
            else -> R.drawable.ic_clock_vector
        }

        return resources.getColoredDrawableWithColor(
            drawableId = drawableId, color = getProperTextColor(), context = applicationContext
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


    private fun setupTabs() {
        binding.viewPager.adapter = null
        binding.mainTabsHolder.removeAllTabs()
        tabsList.forEachIndexed { index, value ->
            if (config.showTabs and value != 0) {
                binding.mainTabsHolder.newTab().setCustomView(R.layout.bottom_tablayout_item)
                    .apply {
                        customView?.findViewById<ImageView>(R.id.tab_item_icon)
                            ?.setImageDrawable(getTabIcon(index))
                        customView?.findViewById<TextView>(R.id.tab_item_label)?.text =
                            getTabLabel(index)
                        customView?.findViewById<TextView>(R.id.tab_item_label)
                            ?.let { AutoFitHelper.create(it) }
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
}