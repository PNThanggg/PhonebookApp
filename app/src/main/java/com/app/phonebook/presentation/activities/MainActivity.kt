package com.app.phonebook.presentation.activities

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ScrollingView
import com.app.phonebook.R
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getProperBackgroundColor
import com.app.phonebook.base.extension.launchCreateNewContactIntent
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
                    text = text,
                    context = context
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


}