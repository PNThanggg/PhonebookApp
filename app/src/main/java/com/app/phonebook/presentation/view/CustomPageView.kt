package com.app.phonebook.presentation.view

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.ClassLoaderCreator
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.app.phonebook.adapter.DelegatingPagerAdapter
import com.app.phonebook.base.utils.isTiramisuPlus

open class CustomPageView : ViewPager {
    private val pageChangeListeners =
        hashMapOf<OnPageChangeListener, ReversingOnPageChangeListener>()

    private var layoutDirection = ViewCompat.LAYOUT_DIRECTION_LTR

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onRtlPropertiesChanged(newLayoutDirection: Int) {
        super.onRtlPropertiesChanged(newLayoutDirection)

        val viewCompatLayoutDirection = if (newLayoutDirection == View.LAYOUT_DIRECTION_RTL) {
            ViewCompat.LAYOUT_DIRECTION_RTL
        } else {
            ViewCompat.LAYOUT_DIRECTION_LTR
        }

        if (viewCompatLayoutDirection != layoutDirection) {
            val adapter = super.getAdapter()
            var position = 0
            if (adapter != null) {
                position = currentItem
            }
            layoutDirection = viewCompatLayoutDirection
            if (adapter != null) {
                adapter.notifyDataSetChanged()
                currentItem = position
            }
        }
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        super.setAdapter(adapter?.let { ReversingAdapter(it, context) })
        currentItem = 0
    }

    override fun getAdapter(): PagerAdapter? {
        val adapter = super.getAdapter()
        return if (adapter is ReversingAdapter) {
            adapter.getDelegate()
        } else {
            adapter
        }
    }

    private val isRtl: Boolean
        get() = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL

    override fun getCurrentItem(): Int {
        var item = super.getCurrentItem()
        val adapter = super.getAdapter()
        if (adapter != null && isRtl) {
            item = adapter.count - item - 1
        }
        return item
    }

    override fun setCurrentItem(position: Int, smoothScroll: Boolean) {
        var newPosition = position
        val adapter = super.getAdapter()
        if (adapter != null && isRtl) {
            newPosition = adapter.count - position - 1
        }

        super.setCurrentItem(newPosition, smoothScroll)
    }

    override fun setCurrentItem(position: Int) {
        var newPosition = position
        val adapter = super.getAdapter()
        if (adapter != null && isRtl) {
            newPosition = adapter.count - position - 1
        }
        super.setCurrentItem(newPosition)
    }

    override fun onSaveInstanceState(): SavedState? {
        val superState = super.onSaveInstanceState()
        return superState?.let { SavedState(it, layoutDirection) }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        layoutDirection = state.layoutDirection
        super.onRestoreInstanceState(state.viewPagerSavedState)
    }

    override fun addOnPageChangeListener(listener: OnPageChangeListener) {
        val reversingListener = ReversingOnPageChangeListener(
            listener = listener,
            context = context
        )
        pageChangeListeners[listener] = reversingListener
        super.addOnPageChangeListener(reversingListener)
    }

    override fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        val reverseListener: ReversingOnPageChangeListener? = pageChangeListeners.remove(listener)
        if (reverseListener != null) {
            super.removeOnPageChangeListener(reverseListener)
        }
    }

    override fun clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners()
        pageChangeListeners.clear()
    }

    override fun onMeasure(newWidthMeasureSpec: Int, newHeightMeasureSpec: Int) {
        var heightMeasureSpec = newHeightMeasureSpec
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            var height = 0
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.measure(
                    newWidthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                val h = child.measuredHeight
                if (h > height) {
                    height = h
                }
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        super.onMeasure(newWidthMeasureSpec, heightMeasureSpec)
    }

    class SavedState : Parcelable {
        val viewPagerSavedState: Parcelable?
        val layoutDirection: Int

        constructor(newViewPagerSavedState: Parcelable, newLayoutDirection: Int) {
            viewPagerSavedState = newViewPagerSavedState
            layoutDirection = newLayoutDirection
        }

        @Suppress("DEPRECATION")
        constructor(`in`: Parcel, newLoader: ClassLoader?) {
            var loader: ClassLoader? = newLoader
            if (loader == null) {
                loader = javaClass.classLoader
            }
            viewPagerSavedState = if (isTiramisuPlus()) `in`.readParcelable(
                loader, javaClass
            ) else `in`.readParcelable(loader)
            layoutDirection = `in`.readInt()
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            out.writeParcelable(viewPagerSavedState, flags)
            out.writeInt(layoutDirection)
        }

        companion object {
            @JvmField
            val CREATOR: ClassLoaderCreator<SavedState> = object : ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return createFromParcel(source, null)
                }

                override fun createFromParcel(source: Parcel, loader: ClassLoader?): SavedState {
                    return SavedState(source, loader)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private class ReversingOnPageChangeListener(
        private val listener: OnPageChangeListener,
        context: Context,
    ) : OnPageChangeListener {
        val customPageView: CustomPageView = CustomPageView(context = context)

        override fun onPageScrolled(
            newPosition: Int, newPositionOffset: Float, newPositionOffsetPixels: Int
        ) {
            // The documentation says that `getPageWidth(...)` returns the fraction of the _measured_ width that that page takes up.  However, the code seems to
            // use the width so we will here too.
            var position = newPosition
            var positionOffset = newPositionOffset
            var positionOffsetPixels = newPositionOffsetPixels


            // The documentation says that `getPageWidth(...)` returns the fraction of the _measured_ width that that page takes up.  However, the code seems to
            // use the width so we will here too.
            val width: Int = customPageView.width
            val adapter: PagerAdapter? = customPageView.adapter

            if (customPageView.isRtl && adapter != null) {
                val count = adapter.count
                var remainingWidth =
                    (width * (1 - adapter.getPageWidth(position))).toInt() + positionOffsetPixels
                while (position < count && remainingWidth > 0) {
                    position += 1
                    remainingWidth -= (width * adapter.getPageWidth(position)).toInt()
                }
                position = count - position - 1
                positionOffsetPixels = -remainingWidth
                positionOffset = positionOffsetPixels / (width * adapter.getPageWidth(position))
            }
            listener.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(newPosition: Int) {
            var position = newPosition

            if (customPageView.isRtl) {
                if (customPageView.adapter != null) {
                    position = customPageView.adapter!!.count - position - 1
                }
            }

            listener.onPageSelected(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            listener.onPageScrollStateChanged(state)
        }
    }


    private class ReversingAdapter(adapter: PagerAdapter, context: Context) :
        DelegatingPagerAdapter(adapter) {
        val customPageView = CustomPageView(context = context)

        override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
            var newPosition = position
            if (customPageView.isRtl) {
                newPosition = count - position - 1
            }
            super.destroyItem(container, newPosition, any)
        }

        override fun getItemPosition(any: Any): Int {
            var position: Int = super.getItemPosition(any)
            if (customPageView.isRtl) {
                position = if (position == POSITION_UNCHANGED || position == POSITION_NONE) {
                    POSITION_NONE
                } else {
                    count - position - 1
                }
            }
            return position
        }

        override fun getPageTitle(position: Int): CharSequence? {
            var newPosition = position
            if (customPageView.isRtl) {
                newPosition = count - position - 1
            }
            return super.getPageTitle(newPosition)
        }

        override fun getPageWidth(position: Int): Float {
            var newPosition = position
            if (customPageView.isRtl) {
                newPosition = count - position - 1
            }
            return super.getPageWidth(newPosition)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var newPosition = position
            if (customPageView.isRtl) {
                newPosition = count - position - 1
            }
            return super.instantiateItem(container, newPosition)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, any: Any) {
            var newPosition = position
            if (customPageView.isRtl) {
                newPosition = count - position - 1
            }
            super.setPrimaryItem(container, newPosition, any)
        }
    }
}