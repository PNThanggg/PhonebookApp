package com.app.phonebook.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.app.phonebook.R
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.utils.TAB_CALL_HISTORY
import com.app.phonebook.base.utils.TAB_CONTACTS
import com.app.phonebook.base.utils.TAB_FAVORITES
import com.app.phonebook.base.utils.TAB_GROUPS
import com.app.phonebook.base.utils.tabsList
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.base.view.BaseViewPagerFragment

class ViewPagerAdapter(val activity: BaseActivity<*>) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = getFragment(position)
        val view = activity.layoutInflater.inflate(layout, container, false)
        container.addView(view)

        (view as BaseViewPagerFragment<*>).apply {
            setupFragment(activity)
        }

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        container.removeView(item as View)
    }

    override fun getCount() = tabsList.filter { it and activity.config.showTabs != 0 }.size

    override fun isViewFromObject(view: View, item: Any) = view == item

    private fun getFragment(position: Int): Int {
        val showTabs = activity.config.showTabs
        val fragments = arrayListOf<Int>()
        if (showTabs and TAB_CONTACTS > 0) {
            fragments.add(R.layout.fragment_contacts)
        }

        if (showTabs and TAB_FAVORITES > 0) {
            fragments.add(R.layout.fragment_favorites)
        }

        if (showTabs and TAB_GROUPS != 0) {
            fragments.add(R.layout.fragment_groups)
        }

        if (showTabs and TAB_CALL_HISTORY > 0) {
            fragments.add(R.layout.fragment_recent)
        }

        return if (position < fragments.size) fragments[position] else fragments.last()
    }
}
