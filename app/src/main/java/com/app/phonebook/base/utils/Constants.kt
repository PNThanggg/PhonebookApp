package com.app.phonebook.base.utils

import android.os.Looper
import android.provider.ContactsContract
import androidx.annotation.StringRes
import com.app.phonebook.R
import com.app.phonebook.base.extension.normalizeString
import com.app.phonebook.data.models.LocalContact
import com.app.phonebook.overloads.times

const val MIN_RECENTS_THRESHOLD = 30

const val DIALPAD_TONE_LENGTH_MS = 150L // The

const val APP_NAME = "Phone Book"

const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
const val EXTRA_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED"

const val KEY_PHONE = "phone"
const val KEY_MAILTO = "mailto"
const val CONTACT_ID = "contact_id"
const val IS_PRIVATE = "is_private"
const val SMT_PRIVATE = "smt_private"
const val FIRST_GROUP_ID = 10000L
const val SHORT_ANIMATION_DURATION = 150L
const val SD_OTG_PATTERN = "^/storage/[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val SD_OTG_SHORT = "^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val OTG_TREE_URI = "otg_tree_uri_2"
const val SD_CARD_PATH = "sd_card_path_2"
const val OTG_REAL_PATH = "otg_real_path_2"
const val DARK_GREY = 0xFF333333.toInt()

const val INTERNAL_STORAGE_PATH = "internal_storage_path"
const val SD_TREE_URI = "tree_uri_2"

val photoExtensions: Array<String> get() = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif", ".apng", ".avif")
val videoExtensions: Array<String> get() = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")
val audioExtensions: Array<String> get() = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac", ".m4b")
val rawExtensions: Array<String> get() = arrayOf(".dng", ".orf", ".nef", ".arw", ".rw2", ".cr2", ".cr3")

const val MD5 = "MD5"
const val NOMEDIA = ".nomedia"

val extensionsSupportingEXIF: Array<String> get() = arrayOf(".jpg", ".jpeg", ".png", ".webp", ".dng")

const val DATE_FORMAT_ONE = "dd.MM.yyyy"
const val DATE_FORMAT_TWO = "dd/MM/yyyy"
const val DATE_FORMAT_THREE = "MM/dd/yyyy"
const val DATE_FORMAT_FOUR = "yyyy-MM-dd"
const val DATE_FORMAT_FIVE = "d MMMM yyyy"
const val DATE_FORMAT_SIX = "MMMM d yyyy"
const val DATE_FORMAT_SEVEN = "MM-dd-yyyy"
const val DATE_FORMAT_EIGHT = "dd-MM-yyyy"
const val DATE_FORMAT_NINE = "yyyyMMdd"
const val DATE_FORMAT_TEN = "yyyy.MM.dd"
const val DATE_FORMAT_ELEVEN = "yy-MM-dd"
const val DATE_FORMAT_TWELVE = "yyMMdd"
const val DATE_FORMAT_THIRTEEN = "yy.MM.dd"
const val DATE_FORMAT_FOURTEEN = "yy/MM/dd"

const val TIME_FORMAT_12 = "hh:mm a"
const val TIME_FORMAT_24 = "HH:mm"

const val DEFAULT_FILE_NAME = "contacts.vcf"
const val AVOID_CHANGING_TEXT_TAG = "avoid_changing_text_tag"
const val AVOID_CHANGING_VISIBILITY_TAG = "avoid_changing_visibility_tag"

const val LOWER_ALPHA = 0.25f
const val MEDIUM_ALPHA = 0.5f
const val HIGHER_ALPHA = 0.75f

const val OTG_PARTITION = "otg_partition_2"

// sorting
const val SORT_ORDER = "sort_order"
const val SORT_FOLDER_PREFIX = "sort_folder_"       // storing folder specific values at using "Use for this folder only"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_TAKEN = 8
const val SORT_BY_EXTENSION = 16
const val SORT_BY_PATH = 32
const val SORT_BY_NUMBER = 64
const val SORT_BY_FIRST_NAME = 128
const val SORT_BY_MIDDLE_NAME = 256
const val SORT_BY_SURNAME = 512
const val SORT_DESCENDING = 1024
const val SORT_BY_TITLE = 2048
const val SORT_BY_ARTIST = 4096
const val SORT_BY_DURATION = 8192
const val SORT_BY_RANDOM = 16384
const val SORT_USE_NUMERIC_VALUE = 32768
const val SORT_BY_FULL_NAME = 65536
const val SORT_BY_CUSTOM = 131072
const val SORT_BY_DATE_CREATED = 262144

// alpha values on a scale 0 - 255
const val LOWER_ALPHA_INT = 30

const val HOUR_MINUTES = 60

const val MINUTE_SECONDS = 60

// shared preferences
const val PREFS_KEY = "Prefs"
const val TEXT_COLOR = "text_color"
const val BACKGROUND_COLOR = "background_color"
const val PRIMARY_COLOR = "primary_color_2"
const val ACCENT_COLOR = "accent_color"
const val WAS_SHARED_THEME_EVER_ACTIVATED = "was_shared_theme_ever_activated"
const val IS_USING_SHARED_THEME = "is_using_shared_theme"
const val IS_USING_AUTO_THEME = "is_using_auto_theme"
const val IS_USING_SYSTEM_THEME = "is_using_system_theme"
const val AUTO_BACKUP_CONTACT_SOURCES = "auto_backup_contact_sources"
const val SHOW_PRIVATE_CONTACTS = "show_private_contacts"
const val ON_CONTACT_CLICK = "on_contact_click"
const val SHOW_CONTACT_THUMBNAILS = "show_contact_thumbnails"
const val SHOW_PHONE_NUMBERS = "show_phone_numbers"
const val WAS_SHARED_THEME_FORCED = "was_shared_theme_forced"
const val LAST_USED_VIEW_PAGER_PAGE = "last_used_view_pager_page"
const val USE_24_HOUR_FORMAT = "use_24_hour_format"
const val DATE_FORMAT = "date_format"
const val FONT_SIZE = "font_size"
const val DEFAULT_TAB = "default_tab"
const val START_NAME_WITH_SURNAME = "start_name_with_surname"
const val SHOW_CALL_CONFIRMATION = "show_call_confirmation"
const val SHOW_ONLY_CONTACTS_WITH_NUMBERS = "show_only_contacts_with_numbers"
const val IGNORED_CONTACT_SOURCES = "ignored_contact_sources_2"
const val LAST_USED_CONTACT_SOURCE = "last_used_contact_source"
const val SHOW_TABS = "show_tabs"
const val SPEED_DIAL = "speed_dial"
const val WAS_LOCAL_ACCOUNT_INITIALIZED = "was_local_account_initialized"
const val MERGE_DUPLICATE_CONTACTS = "merge_duplicate_contacts"
const val FAVORITES_CONTACTS_ORDER = "favorites_contacts_order"
const val FAVORITES_CUSTOM_ORDER_SELECTED = "favorites_custom_order_selected"
const val VIEW_TYPE = "view_type"
const val CONTACTS_GRID_COLUMN_COUNT = "contacts_grid_column_count"
const val REMEMBER_SIM_PREFIX = "remember_sim_"
const val GROUP_SUBSEQUENT_CALLS = "group_subsequent_calls"
const val OPEN_DIAL_PAD_AT_LAUNCH = "open_dial_pad_at_launch"
const val DISABLE_PROXIMITY_SENSOR = "disable_proximity_sensor"
const val DISABLE_SWIPE_TO_ANSWER = "disable_swipe_to_answer"
const val WAS_OVERLAY_SNACKBAR_CONFIRMED = "was_overlay_snackbar_confirmed"
const val DIALPAD_VIBRATION = "dialpad_vibration"
const val DIALPAD_BEEPS = "dialpad_beeps"
const val HIDE_DIALPAD_NUMBERS = "hide_dialpad_numbers"
const val ALWAYS_SHOW_FULLSCREEN = "always_show_fullscreen"
const val PRIMARY_ANDROID_DATA_TREE_URI = "primary_android_data_tree_uri_2"
const val OTG_ANDROID_OBB_TREE_URI = "otg_android_obb_tree_uri_2"
const val SD_ANDROID_OBB_TREE_URI = "sd_android_obb_tree_uri_2"
const val OTG_ANDROID_DATA_TREE_URI = "otg_android_data_tree__uri_2"
const val SD_ANDROID_DATA_TREE_URI = "sd_android_data_tree_uri_2"
const val PRIMARY_ANDROID_OBB_TREE_URI = "primary_android_obb_tree_uri_2"
const val GROUP = "group"
const val IS_FROM_SIMPLE_CONTACTS = "is_from_simple_contacts"
const val ADD_NEW_CONTACT_NUMBER = "add_new_contact_number"

const val LOCATION_CONTACTS_TAB = 0
const val LOCATION_FAVORITES_TAB = 1
const val LOCATION_GROUP_CONTACTS = 2
const val LOCATION_INSERT_OR_EDIT = 3

// visible fields filtering
const val SHOW_PREFIX_FIELD = 1
const val SHOW_FIRST_NAME_FIELD = 2
const val SHOW_MIDDLE_NAME_FIELD = 4
const val SHOW_SURNAME_FIELD = 8
const val SHOW_SUFFIX_FIELD = 16
const val SHOW_PHONE_NUMBERS_FIELD = 32
const val SHOW_EMAILS_FIELD = 64
const val SHOW_ADDRESSES_FIELD = 128
const val SHOW_EVENTS_FIELD = 256
const val SHOW_NOTES_FIELD = 512
const val SHOW_ORGANIZATION_FIELD = 1024
const val SHOW_GROUPS_FIELD = 2048
const val SHOW_CONTACT_SOURCE_FIELD = 4096
const val SHOW_WEBSITES_FIELD = 8192
const val SHOW_NICKNAME_FIELD = 16384
const val SHOW_IMS_FIELD = 32768
const val SHOW_RINGTONE_FIELD = 65536

const val DEFAULT_EMAIL_TYPE = ContactsContract.CommonDataKinds.Email.TYPE_HOME
const val DEFAULT_PHONE_NUMBER_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
const val DEFAULT_ADDRESS_TYPE = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
const val DEFAULT_EVENT_TYPE = ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
const val DEFAULT_IM_TYPE = ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE

const val SHOW_CONTACT_FIELDS = "show_contact_fields"

// contact grid view constants
const val CONTACTS_GRID_MAX_COLUMNS_COUNT = 10

// font sizes
const val FONT_SIZE_SMALL = 0
const val FONT_SIZE_MEDIUM = 1
const val FONT_SIZE_LARGE = 2

// default tabs
const val TAB_LAST_USED = 0
const val TAB_CONTACTS = 1
const val TAB_FAVORITES = 2
const val TAB_CALL_HISTORY = 4
const val TAB_GROUPS = 8

const val ALL_TABS_MASK = TAB_CONTACTS or TAB_FAVORITES or TAB_CALL_HISTORY or TAB_GROUPS

val tabsList = arrayListOf(TAB_CONTACTS, TAB_FAVORITES, TAB_GROUPS, TAB_CALL_HISTORY)

const val ON_CLICK_CALL_CONTACT = 1
const val ON_CLICK_VIEW_CONTACT = 2
const val ON_CLICK_EDIT_CONTACT = 3

// possible icons at the top left corner
enum class NavigationIcon(@StringRes val accessibilityResId: Int) {
    Cross(R.string.close), Arrow(R.string.back), None(0)
}

// view types
const val VIEW_TYPE_GRID = 1
const val VIEW_TYPE_LIST = 2

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

const val FIRST_CONTACT_ID = 1000000

const val DEFAULT_ORGANIZATION_TYPE = ContactsContract.CommonDataKinds.Organization.TYPE_WORK
const val DEFAULT_WEBSITE_TYPE = ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE
const val DEFAULT_MIMETYPE = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE

// contact photo changes
const val PHOTO_ADDED = 1
const val PHOTO_REMOVED = 2
const val PHOTO_CHANGED = 3
const val PHOTO_UNCHANGED = 4

// apps with special handling
const val TELEGRAM_PACKAGE = "org.telegram.messenger"
const val SIGNAL_PACKAGE = "org.thoughtcrime.securesms"
const val WHATSAPP_PACKAGE = "com.whatsapp"
const val VIBER_PACKAGE = "com.viber.voip"
const val THREEMA_PACKAGE = "ch.threema.app"

fun getQuestionMarks(size: Int) = ("?," * size).trimEnd(',')

val letterBackgroundColors = arrayListOf(
    0xCCD32F2F,
    0xCCC2185B,
    0xCC1976D2,
    0xCC0288D1,
    0xCC0097A7,
    0xCC00796B,
    0xCC388E3C,
    0xCC689F38,
    0xCCF57C00,
    0xCCE64A19
)

fun getEmptyLocalContact() = LocalContact(
    0,
    "",
    "",
    "",
    "",
    "",
    "",
    null,
    "",
    ArrayList(),
    ArrayList(),
    ArrayList(),
    0,
    ArrayList(),
    "",
    ArrayList(),
    "",
    "",
    ArrayList(),
    ArrayList(),
    null
)


fun getProperText(
    text: String,
    shouldNormalize: Boolean
) = if (shouldNormalize) text.normalizeString() else text

// global intents
const val OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB = 1000
const val OPEN_DOCUMENT_TREE_OTG = 1001
const val OPEN_DOCUMENT_TREE_SD = 1002
const val OPEN_DOCUMENT_TREE_FOR_SDK_30 = 1003
const val REQUEST_SET_AS = 1004
const val REQUEST_EDIT_IMAGE = 1005
const val SELECT_EXPORT_SETTINGS_FILE_INTENT = 1006
const val REQUEST_CODE_SET_DEFAULT_DIALER = 1007
const val CREATE_DOCUMENT_SDK_30 = 1008
const val REQUEST_CODE_SET_DEFAULT_CALLER_ID = 1010