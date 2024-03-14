package com.app.phonebook.base.extension

import com.google.android.material.tabs.TabLayout

//fun TabLayout.onTabSelectionChanged(
//    tabUnselectedAction: ((inactiveTab: TabLayout.Tab) -> Unit)? = null,
//    tabSelectedAction: ((activeTab: TabLayout.Tab) -> Unit)? = null
//) = setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//    override fun onTabSelected(tab: TabLayout.Tab) {
//        tabSelectedAction?.invoke(tab)
//    }
//
//    override fun onTabUnselected(tab: TabLayout.Tab) {
//        tabUnselectedAction?.invoke(tab)
//    }
//
//    override fun onTabReselected(tab: TabLayout.Tab) {
//        tabSelectedAction?.invoke(tab)
//    }
//})


fun TabLayout.onTabSelectionChanged(
    tabUnselectedAction: ((inactiveTab: TabLayout.Tab) -> Unit)? = null,
    tabSelectedAction: ((activeTab: TabLayout.Tab) -> Unit)? = null
) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            tabSelectedAction?.invoke(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            tabUnselectedAction?.invoke(tab)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            // If you want to handle reselection, add your logic here
            // For simplicity, it's currently invoking the same action as selection
            tabSelectedAction?.invoke(tab)
        }
    })
}
