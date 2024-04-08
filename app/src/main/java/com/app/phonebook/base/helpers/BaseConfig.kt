package com.app.phonebook.base.helpers

import android.content.Context
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.core.content.ContextCompat
import com.app.phonebook.R
import com.app.phonebook.base.extension.getSharedPrefs
import com.app.phonebook.base.utils.ACCENT_COLOR
import com.app.phonebook.base.utils.BACKGROUND_COLOR
import com.app.phonebook.base.utils.CONTACTS_GRID_COLUMN_COUNT
import com.app.phonebook.base.utils.DATE_FORMAT
import com.app.phonebook.base.utils.DATE_FORMAT_EIGHT
import com.app.phonebook.base.utils.DATE_FORMAT_FIVE
import com.app.phonebook.base.utils.DATE_FORMAT_FOUR
import com.app.phonebook.base.utils.DATE_FORMAT_ONE
import com.app.phonebook.base.utils.DATE_FORMAT_SEVEN
import com.app.phonebook.base.utils.DATE_FORMAT_SIX
import com.app.phonebook.base.utils.DATE_FORMAT_THREE
import com.app.phonebook.base.utils.DATE_FORMAT_TWO
import com.app.phonebook.base.utils.DEFAULT_TAB
import com.app.phonebook.base.utils.FAVORITES_CONTACTS_ORDER
import com.app.phonebook.base.utils.FAVORITES_CUSTOM_ORDER_SELECTED
import com.app.phonebook.base.utils.FONT_SIZE
import com.app.phonebook.base.utils.IGNORED_CONTACT_SOURCES
import com.app.phonebook.base.utils.IS_USING_AUTO_THEME
import com.app.phonebook.base.utils.IS_USING_SHARED_THEME
import com.app.phonebook.base.utils.IS_USING_SYSTEM_THEME
import com.app.phonebook.base.utils.LAST_USED_CONTACT_SOURCE
import com.app.phonebook.base.utils.LAST_USED_VIEW_PAGER_PAGE
import com.app.phonebook.base.utils.MERGE_DUPLICATE_CONTACTS
import com.app.phonebook.base.utils.PRIMARY_COLOR
import com.app.phonebook.base.utils.SHOW_CALL_CONFIRMATION
import com.app.phonebook.base.utils.SHOW_ONLY_CONTACTS_WITH_NUMBERS
import com.app.phonebook.base.utils.SHOW_PRIVATE_CONTACTS
import com.app.phonebook.base.utils.SORT_ORDER
import com.app.phonebook.base.utils.SPEED_DIAL
import com.app.phonebook.base.utils.START_NAME_WITH_SURNAME
import com.app.phonebook.base.utils.TAB_LAST_USED
import com.app.phonebook.base.utils.TEXT_COLOR
import com.app.phonebook.base.utils.USE_24_HOUR_FORMAT
import com.app.phonebook.base.utils.VIEW_TYPE
import com.app.phonebook.base.utils.VIEW_TYPE_LIST
import com.app.phonebook.base.utils.WAS_LOCAL_ACCOUNT_INITIALIZED
import com.app.phonebook.base.utils.WAS_SHARED_THEME_EVER_ACTIVATED
import com.app.phonebook.base.utils.WAS_SHARED_THEME_FORCED
import java.text.SimpleDateFormat

open class BaseConfig(private val context: Context) {
    val prefs = context.getSharedPrefs()

    companion object {
        fun newInstance(context: Context) = BaseConfig(context)
    }

    var textColor: Int
        get() = prefs.getInt(
            TEXT_COLOR, ContextCompat.getColor(context, R.color.default_text_color)
        )
        set(textColor) = prefs.edit().putInt(TEXT_COLOR, textColor).apply()

    var backgroundColor: Int
        get() = prefs.getInt(
            BACKGROUND_COLOR, ContextCompat.getColor(context, R.color.default_background_color)
        )
        set(backgroundColor) = prefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = prefs.getInt(
            PRIMARY_COLOR, ContextCompat.getColor(context, R.color.default_primary_color)
        )
        set(primaryColor) = prefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()

    var accentColor: Int
        get() = prefs.getInt(
            ACCENT_COLOR, ContextCompat.getColor(context, R.color.default_accent_color)
        )
        set(accentColor) = prefs.edit().putInt(ACCENT_COLOR, accentColor).apply()

    var wasSharedThemeEverActivated: Boolean
        get() = prefs.getBoolean(WAS_SHARED_THEME_EVER_ACTIVATED, false)
        set(wasSharedThemeEverActivated) = prefs.edit().putBoolean(WAS_SHARED_THEME_EVER_ACTIVATED, wasSharedThemeEverActivated)
            .apply()

    var isUsingSharedTheme: Boolean
        get() = prefs.getBoolean(IS_USING_SHARED_THEME, false)
        set(isUsingSharedTheme) = prefs.edit().putBoolean(IS_USING_SHARED_THEME, isUsingSharedTheme).apply()

    var isUsingAutoTheme: Boolean
        get() = prefs.getBoolean(IS_USING_AUTO_THEME, false)
        set(isUsingAutoTheme) = prefs.edit().putBoolean(IS_USING_AUTO_THEME, isUsingAutoTheme).apply()

    var isUsingSystemTheme: Boolean
        get() = prefs.getBoolean(IS_USING_SYSTEM_THEME, false)
        set(isUsingSystemTheme) = prefs.edit().putBoolean(IS_USING_SYSTEM_THEME, isUsingSystemTheme).apply()

    var wasSharedThemeForced: Boolean
        get() = prefs.getBoolean(WAS_SHARED_THEME_FORCED, false)
        set(wasSharedThemeForced) = prefs.edit().putBoolean(WAS_SHARED_THEME_FORCED, wasSharedThemeForced).apply()

    var sorting: Int
        get() = prefs.getInt(SORT_ORDER, context.resources.getInteger(R.integer.default_sorting))
        set(sorting) = prefs.edit().putInt(SORT_ORDER, sorting).apply()

    var lastUsedViewPagerPage: Int
        get() = prefs.getInt(
            LAST_USED_VIEW_PAGER_PAGE, context.resources.getInteger(R.integer.default_viewpager_page)
        )
        set(lastUsedViewPagerPage) = prefs.edit().putInt(LAST_USED_VIEW_PAGER_PAGE, lastUsedViewPagerPage).apply()

    var use24HourFormat: Boolean
        get() = prefs.getBoolean(USE_24_HOUR_FORMAT, DateFormat.is24HourFormat(context))
        set(use24HourFormat) = prefs.edit().putBoolean(USE_24_HOUR_FORMAT, use24HourFormat).apply()

    var dateFormat: String
        get() = prefs.getString(DATE_FORMAT, getDefaultDateFormat())!!
        set(dateFormat) = prefs.edit().putString(DATE_FORMAT, dateFormat).apply()

    private fun getDefaultDateFormat(): String {
        val format = DateFormat.getDateFormat(context)
        val pattern = (format as SimpleDateFormat).toLocalizedPattern()
        return when (pattern.lowercase().replace(" ", "")) {
            "d.M.y" -> DATE_FORMAT_ONE
            "dd/mm/y" -> DATE_FORMAT_TWO
            "mm/dd/y" -> DATE_FORMAT_THREE
            "y-mm-dd" -> DATE_FORMAT_FOUR
            "dmmmmy" -> DATE_FORMAT_FIVE
            "mmmmdy" -> DATE_FORMAT_SIX
            "mm-dd-y" -> DATE_FORMAT_SEVEN
            "dd-mm-y" -> DATE_FORMAT_EIGHT
            else -> DATE_FORMAT_ONE
        }
    }

    var fontSize: Int
        get() = prefs.getInt(FONT_SIZE, context.resources.getInteger(R.integer.default_font_size))
        set(size) = prefs.edit().putInt(FONT_SIZE, size).apply()


    var defaultTab: Int
        get() = prefs.getInt(DEFAULT_TAB, TAB_LAST_USED)
        set(defaultTab) = prefs.edit().putInt(DEFAULT_TAB, defaultTab).apply()

    var startNameWithSurname: Boolean
        get() = prefs.getBoolean(START_NAME_WITH_SURNAME, false)
        set(startNameWithSurname) = prefs.edit().putBoolean(START_NAME_WITH_SURNAME, startNameWithSurname).apply()


    var showCallConfirmation: Boolean
        get() = prefs.getBoolean(SHOW_CALL_CONFIRMATION, false)
        set(showCallConfirmation) = prefs.edit().putBoolean(SHOW_CALL_CONFIRMATION, showCallConfirmation).apply()

    var ignoredContactSources: HashSet<String>
        get() = prefs.getStringSet(IGNORED_CONTACT_SOURCES, hashSetOf(".")) as HashSet
        set(ignoreContactSources) = prefs.edit().remove(IGNORED_CONTACT_SOURCES)
            .putStringSet(IGNORED_CONTACT_SOURCES, ignoreContactSources).apply()


    var showOnlyContactsWithNumbers: Boolean
        get() = prefs.getBoolean(SHOW_ONLY_CONTACTS_WITH_NUMBERS, false)
        set(showOnlyContactsWithNumbers) = prefs.edit().putBoolean(SHOW_ONLY_CONTACTS_WITH_NUMBERS, showOnlyContactsWithNumbers)
            .apply()

    var lastUsedContactSource: String
        get() = prefs.getString(LAST_USED_CONTACT_SOURCE, "")!!
        set(lastUsedContactSource) = prefs.edit().putString(LAST_USED_CONTACT_SOURCE, lastUsedContactSource).apply()

    var wasLocalAccountInitialized: Boolean
        get() = prefs.getBoolean(WAS_LOCAL_ACCOUNT_INITIALIZED, false)
        set(wasLocalAccountInitialized) = prefs.edit().putBoolean(WAS_LOCAL_ACCOUNT_INITIALIZED, wasLocalAccountInitialized)
            .apply()

    var speedDial: String
        get() = prefs.getString(SPEED_DIAL, "")!!
        set(speedDial) = prefs.edit().putString(SPEED_DIAL, speedDial).apply()

    var mergeDuplicateContacts: Boolean
        get() = prefs.getBoolean(MERGE_DUPLICATE_CONTACTS, true)
        set(mergeDuplicateContacts) = prefs.edit().putBoolean(MERGE_DUPLICATE_CONTACTS, mergeDuplicateContacts).apply()

    var favoritesContactsOrder: String
        get() = prefs.getString(FAVORITES_CONTACTS_ORDER, "")!!
        set(order) = prefs.edit().putString(FAVORITES_CONTACTS_ORDER, order).apply()

    var isCustomOrderSelected: Boolean
        get() = prefs.getBoolean(FAVORITES_CUSTOM_ORDER_SELECTED, false)
        set(selected) = prefs.edit().putBoolean(FAVORITES_CUSTOM_ORDER_SELECTED, selected).apply()

    var viewType: Int
        get() = prefs.getInt(VIEW_TYPE, VIEW_TYPE_LIST)
        set(viewType) = prefs.edit().putInt(VIEW_TYPE, viewType).apply()

    var contactsGridColumnCount: Int
        get() = prefs.getInt(CONTACTS_GRID_COLUMN_COUNT, getDefaultContactColumnsCount())
        set(contactsGridColumnCount) = prefs.edit().putInt(CONTACTS_GRID_COLUMN_COUNT, contactsGridColumnCount).apply()

    private fun getDefaultContactColumnsCount(): Int {
        val isPortrait = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return if (isPortrait) {
            context.resources.getInteger(R.integer.contacts_grid_columns_count_portrait)
        } else {
            context.resources.getInteger(R.integer.contacts_grid_columns_count_landscape)
        }
    }

    var showPrivateContacts: Boolean
        get() = prefs.getBoolean(SHOW_PRIVATE_CONTACTS, true)
        set(showPrivateContacts) = prefs.edit().putBoolean(SHOW_PRIVATE_CONTACTS, showPrivateContacts).apply()
}
