package com.app.phonebook.adapter

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

open class DelegatingPagerAdapter(
    private val delegate: PagerAdapter
) : PagerAdapter() {
    fun getDelegate(): PagerAdapter {
        return delegate
    }

    override fun getCount(): Int = delegate.count

    override fun startUpdate(container: ViewGroup) {
        delegate.startUpdate(container)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return delegate.instantiateItem(container, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        delegate.destroyItem(container, position, any)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, any: Any) {
        delegate.setPrimaryItem(container, position, any)
    }


    override fun finishUpdate(container: ViewGroup) {
        delegate.finishUpdate(container)
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return delegate.isViewFromObject(view, any)
    }

    override fun saveState(): Parcelable? {
        return delegate.saveState()
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        delegate.restoreState(state, loader)
    }

    override fun getItemPosition(any: Any): Int {
        return delegate.getItemPosition(any)
    }

    override fun notifyDataSetChanged() {
        delegate.notifyDataSetChanged()
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        delegate.registerDataSetObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        delegate.unregisterDataSetObserver(observer)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return delegate.getPageTitle(position)
    }

    override fun getPageWidth(position: Int): Float {
        return delegate.getPageWidth(position)
    }
}