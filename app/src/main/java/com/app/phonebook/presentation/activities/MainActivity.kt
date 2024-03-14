package com.app.phonebook.presentation.activities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.app.phonebook.R
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.getProperBackgroundColor
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
import com.app.phonebook.presentation.fragments.RecentsFragment

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var launchedDialer = false
    private var storedShowTabs = 0
    private var storedFontSize = 0
    private var storedStartNameWithSurname = false
    var cachedContacts = ArrayList<Contact>()


    override fun initView() {
        setupOptionsMenu()
        refreshMenuItems()
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

    private fun setupOptionsMenu() {
        binding.mainMenu.apply {
            getToolbar().inflateMenu(R.menu.menu)
            toggleHideOnScroll(false)
            setupMenu()

            onSearchClosedListener = {
                getAllFragments().forEach {
                    it?.onSearchQueryChanged("")
                }
            }
//
//            onSearchTextChangedListener = { text ->
//                getCurrentFragment()?.onSearchQueryChanged(text)
//            }
//
//            getToolbar().setOnMenuItemClickListener { menuItem ->
//                when (menuItem.itemId) {
//                    R.id.clear_call_history -> clearCallHistory()
//                    R.id.create_new_contact -> launchCreateNewContactIntent()
//                    R.id.sort -> showSortingDialog(showCustomSorting = getCurrentFragment() is com.app.phonebook.presentation.fragments.FavoritesFragment)
//                    R.id.filter -> showFilterDialog()
//                    R.id.more_apps_from_us -> launchMoreAppsFromUsIntent()
//                    R.id.settings -> launchSettings()
//                    R.id.change_view_type -> changeViewType()
//                    R.id.column_count -> changeColumnCount()
//                    R.id.about -> launchAbout()
//                    else -> return@setOnMenuItemClickListener false
//                }
//                return@setOnMenuItemClickListener true
//            }
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
    fun updateMaterialActivityViews(
        mainCoordinatorLayout: CoordinatorLayout?,
        nestedView: View?,
        useTransparentNavigation: Boolean,
        useTopSearchMenu: Boolean,
    ) {
//        this.mainCoordinatorLayout = mainCoordinatorLayout
//        this.nestedView = nestedView
//        this.useTransparentNavigation = useTransparentNavigation
//        this.useTopSearchMenu = useTopSearchMenu
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

    private fun getRecentsFragment(): RecentsFragment? = findViewById(R.id.recents_fragment)

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