package com.app.phonebook.presentation.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.app.phonebook.R
import com.app.phonebook.base.extension.adjustAlpha
import com.app.phonebook.base.extension.applyColorFilter
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getProperBackgroundColor
import com.app.phonebook.base.extension.getProperPrimaryColor
import com.app.phonebook.base.extension.hideKeyboard
import com.app.phonebook.base.extension.onTextChangeListener
import com.app.phonebook.base.extension.removeBit
import com.app.phonebook.base.extension.showKeyboard
import com.app.phonebook.base.utils.LOWER_ALPHA
import com.app.phonebook.base.utils.MEDIUM_ALPHA
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.databinding.MenuSearchBinding
import com.google.android.material.appbar.AppBarLayout

class MySearchMenu(context: Context, attrs: AttributeSet) : AppBarLayout(context, attrs) {
    var isSearchOpen = false
    var useArrowIcon = false
    var onSearchOpenListener: (() -> Unit)? = null
    var onSearchClosedListener: (() -> Unit)? = null
    var onSearchTextChangedListener: ((text: String) -> Unit)? = null
    var onNavigateBackClickListener: (() -> Unit)? = null

    private val binding = MenuSearchBinding.inflate(LayoutInflater.from(context), this, true)

    fun getToolbar() = binding.topToolbar

    fun setupMenu() {
        binding.topToolbarSearchIcon.setOnClickListener {
            if (isSearchOpen) {
                closeSearch(it)
            } else if (useArrowIcon && onNavigateBackClickListener != null) {
                onNavigateBackClickListener!!()
            } else {
                binding.topToolbarSearch.requestFocus()
                (context as? Activity)?.showKeyboard(binding.topToolbarSearch)
            }
        }

        post {
            binding.topToolbarSearch.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    openSearch()
                }
            }
        }

        binding.topToolbarSearch.onTextChangeListener { text ->
            onSearchTextChangedListener?.invoke(text)
        }
    }

    fun focusView() {
        binding.topToolbarSearch.requestFocus()
    }

    fun openSearch() {
        isSearchOpen = true
        onSearchOpenListener?.invoke()
        binding.topToolbarSearchIcon.setImageResource(R.drawable.ic_arrow_left_vector)
        binding.topToolbarSearchIcon.contentDescription = resources.getString(R.string.back)
    }

    fun closeSearch(view: View) {
        isSearchOpen = false
        onSearchClosedListener?.invoke()
        binding.topToolbarSearch.setText("")
        if (!useArrowIcon) {
            binding.topToolbarSearchIcon.setImageResource(R.drawable.ic_search_vector)
            binding.topToolbarSearchIcon.contentDescription = resources.getString(R.string.search)
        }
        (context as? Activity)?.hideKeyboard(view)
    }

    fun getCurrentQuery() = binding.topToolbarSearch.text.toString()

    fun updateHintText(text: String) {
        binding.topToolbarSearch.hint = text
    }

    fun toggleHideOnScroll(hideOnScroll: Boolean) {
        val params = binding.topAppBarLayout.layoutParams as LayoutParams
        if (hideOnScroll) {
            params.scrollFlags =
                LayoutParams.SCROLL_FLAG_SCROLL or LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            params.scrollFlags =
                params.scrollFlags.removeBit(LayoutParams.SCROLL_FLAG_SCROLL or LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
        }
    }

    fun toggleForceArrowBackIcon(useArrowBack: Boolean) {
        this.useArrowIcon = useArrowBack
        val (icon, accessibilityString) = if (useArrowBack) {
            Pair(R.drawable.ic_arrow_left_vector, R.string.back)
        } else {
            Pair(R.drawable.ic_search_vector, R.string.search)
        }

        binding.topToolbarSearchIcon.setImageResource(icon)
        binding.topToolbarSearchIcon.contentDescription = resources.getString(accessibilityString)
    }

    fun updateColors() {
        val backgroundColor = context.getProperBackgroundColor()
        val contrastColor = backgroundColor.getContrastColor()

        setBackgroundColor(backgroundColor)
        binding.topAppBarLayout.setBackgroundColor(backgroundColor)
        binding.topToolbarSearchIcon.applyColorFilter(contrastColor)
        binding.topToolbarHolder.background?.applyColorFilter(
            context.getProperPrimaryColor().adjustAlpha(LOWER_ALPHA)
        )
        binding.topToolbarSearch.setTextColor(contrastColor)
        binding.topToolbarSearch.setHintTextColor(contrastColor.adjustAlpha(MEDIUM_ALPHA))
        (context as? BaseActivity<*>)?.updateTopBarColors(binding.topToolbar, backgroundColor)
    }
}
