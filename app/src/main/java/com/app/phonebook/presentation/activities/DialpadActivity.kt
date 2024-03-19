package com.app.phonebook.presentation.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.app.phonebook.R
import com.app.phonebook.adapter.ContactsAdapter
import com.app.phonebook.base.extension.addCharacter
import com.app.phonebook.base.extension.applyColorFilter
import com.app.phonebook.base.extension.areMultipleSIMsAvailable
import com.app.phonebook.base.extension.beVisible
import com.app.phonebook.base.extension.beVisibleIf
import com.app.phonebook.base.extension.boundingBox
import com.app.phonebook.base.extension.config
import com.app.phonebook.base.extension.disableKeyboard
import com.app.phonebook.base.extension.getColorStateList
import com.app.phonebook.base.extension.getColoredDrawableWithColor
import com.app.phonebook.base.extension.getContrastColor
import com.app.phonebook.base.extension.getKeyEvent
import com.app.phonebook.base.extension.getMyContactsCursor
import com.app.phonebook.base.extension.getProperBackgroundColor
import com.app.phonebook.base.extension.getProperPrimaryColor
import com.app.phonebook.base.extension.getProperTextColor
import com.app.phonebook.base.extension.isDefaultDialer
import com.app.phonebook.base.extension.launchActivityIntent
import com.app.phonebook.base.extension.normalizeString
import com.app.phonebook.base.extension.onTextChangeListener
import com.app.phonebook.base.extension.performHapticFeedback
import com.app.phonebook.base.extension.telephonyManager
import com.app.phonebook.base.extension.updateTextColors
import com.app.phonebook.base.extension.value
import com.app.phonebook.base.utils.DIALPAD_TONE_LENGTH_MS
import com.app.phonebook.base.utils.KEY_PHONE
import com.app.phonebook.base.utils.LOWER_ALPHA_INT
import com.app.phonebook.base.utils.NavigationIcon
import com.app.phonebook.base.utils.REQUEST_CODE_SET_DEFAULT_DIALER
import com.app.phonebook.base.view.BaseActivity
import com.app.phonebook.data.models.Contact
import com.app.phonebook.data.models.SpeedDial
import com.app.phonebook.databinding.ActivityDialpadBinding
import com.app.phonebook.helpers.ContactsHelper
import com.app.phonebook.helpers.MyContactsContentProvider
import com.app.phonebook.helpers.ToneGeneratorHelper
import com.app.phonebook.presentation.dialog.CallConfirmationDialog
import com.app.phonebook.presentation.view.FastScrollItemIndicator
import java.util.Locale
import kotlin.math.roundToInt

class DialpadActivity : BaseActivity<ActivityDialpadBinding>() {
    private var allContacts = ArrayList<Contact>()
    private var speedDialValues = ArrayList<SpeedDial>()
    private var privateCursor: Cursor? = null
    private var toneGeneratorHelper: ToneGeneratorHelper? = null
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val pressedKeys = mutableSetOf<Char>()

    override fun initView(savedInstanceState: Bundle?) {
        binding.apply {
            updateMaterialActivityViews(
                mainCoordinatorLayout = dialpadCoordinator,
                nestedView = dialpadHolder,
                useTransparentNavigation = true,
                useTopSearchMenu = false
            )
            setupMaterialScrollListener(dialpadList, dialpadToolbar)
        }

        updateNavigationBarColor(getProperBackgroundColor())

        binding.dialpadWrapper.apply {
            if (config.hideDialpadNumbers) {
                dialpad1Holder.isVisible = false
                dialpad2Holder.isVisible = false
                dialpad3Holder.isVisible = false
                dialpad4Holder.isVisible = false
                dialpad5Holder.isVisible = false
                dialpad6Holder.isVisible = false
                dialpad7Holder.isVisible = false
                dialpad8Holder.isVisible = false
                dialpad9Holder.isVisible = false
                dialpadPlusHolder.isVisible = true
                dialpad0Holder.visibility = View.INVISIBLE
            }

            arrayOf(
                dialpad0Holder,
                dialpad1Holder,
                dialpad2Holder,
                dialpad3Holder,
                dialpad4Holder,
                dialpad5Holder,
                dialpad6Holder,
                dialpad7Holder,
                dialpad8Holder,
                dialpad9Holder,
                dialpadPlusHolder,
                dialpadAsteriskHolder,
                dialpadHashtagHolder
            ).forEach {
                it.background = ResourcesCompat.getDrawable(resources, R.drawable.pill_background, theme)
                it.background?.alpha = LOWER_ALPHA_INT
            }
        }

        setupOptionsMenu()

        speedDialValues = config.getSpeedDialValues()
        privateCursor = getMyContactsCursor(
            favoritesOnly = false,
            withPhoneNumbersOnly = true
        )

        toneGeneratorHelper = ToneGeneratorHelper(this, DIALPAD_TONE_LENGTH_MS)

        binding.dialpadWrapper.apply {
            setupCharClick(dialpad1Holder, '1')
            setupCharClick(dialpad2Holder, '2')
            setupCharClick(dialpad3Holder, '3')
            setupCharClick(dialpad4Holder, '4')
            setupCharClick(dialpad5Holder, '5')
            setupCharClick(dialpad6Holder, '6')
            setupCharClick(dialpad7Holder, '7')
            setupCharClick(dialpad8Holder, '8')
            setupCharClick(dialpad9Holder, '9')
            setupCharClick(dialpad0Holder, '0')
            setupCharClick(dialpadPlusHolder, '+', longClickable = false)
            setupCharClick(dialpadAsteriskHolder, '*', longClickable = false)
            setupCharClick(dialpadHashtagHolder, '#', longClickable = false)
        }

        binding.apply {
            dialpadClearChar.setOnClickListener { clearChar(it) }
            dialpadClearChar.setOnLongClickListener { clearInput(); true }
            dialpadCallButton.setOnClickListener { initCall(dialpadInput.value, 0) }
            dialpadInput.onTextChangeListener { dialpadValueChanged(it) }
            dialpadInput.requestFocus()
            dialpadInput.disableKeyboard()
        }

        ContactsHelper(this).getContacts(showOnlyContactsWithNumbers = true) { allContacts ->
            gotContact(allContacts)
        }

        val properPrimaryColor = getProperPrimaryColor()
        val callIconId = if (areMultipleSIMsAvailable()) {
            val callIcon = resources.getColoredDrawableWithColor(
                drawableId = R.drawable.ic_phone_two_vector,
                color = properPrimaryColor.getContrastColor(),
                context = this
            )
            binding.apply {
                dialpadCallTwoButton.setImageDrawable(callIcon)
                dialpadCallTwoButton.background.applyColorFilter(properPrimaryColor)
                dialpadCallTwoButton.beVisible()
                dialpadCallTwoButton.setOnClickListener {
                    initCall(dialpadInput.value, 1)
                }
            }

            R.drawable.ic_phone_one_vector
        } else {
            R.drawable.ic_phone_vector
        }

        binding.apply {
            val callIcon = resources.getColoredDrawableWithColor(
                drawableId = callIconId,
                color = properPrimaryColor.getContrastColor(),
                context = this@DialpadActivity
            )
            dialpadCallButton.setImageDrawable(callIcon)
            dialpadCallButton.background.applyColorFilter(properPrimaryColor)

            letterFastScroller.textColor = getProperTextColor().getColorStateList()
            letterFastScroller.pressedTextColor = properPrimaryColor

            letterFastScrollerThumb.setupWithFastScroller(letterFastScroller)
            letterFastScrollerThumb.textColor = properPrimaryColor.getContrastColor()
            letterFastScrollerThumb.thumbColor = properPrimaryColor.getColorStateList()
        }
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun beforeCreate() {
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityDialpadBinding {
        return ActivityDialpadBinding.inflate(inflater)
    }

    override fun onResume() {
        super.onResume()

        updateTextColors(binding.dialpadHolder)
        binding.dialpadClearChar.applyColorFilter(getProperTextColor())
        updateNavigationBarColor(getProperBackgroundColor())
        setupToolbar(binding.dialpadToolbar, NavigationIcon.Arrow)
    }

    /**
     * Updates the application's contact list with new contacts and integrates private contacts.
     *
     * This function is called to update the application's current list of contacts with a new set. It first replaces
     * `allContacts` with `newContacts`. Then, it queries for private contacts using a content provider and, if any are found,
     * adds them to `allContacts`. After integrating private contacts, it sorts the combined list to maintain order.
     *
     * Following the update and integration of contacts, this function checks if there's a pending dial intent that needs to
     * be handled or if the dialpad input is empty. If either condition is true, it triggers `dialpadValueChanged` with an
     * empty string to refresh the dialpad view or handle the dial intent appropriately.
     *
     * @param newContacts The new list of contacts to be integrated into the application's current contact list.
     *
     * Note: This function assumes that `MyContactsContentProvider.getContacts()` is available and can be used to retrieve
     * a list of private contacts. `allContacts` is expected to be a mutable list accessible within the class that contains
     * this function. The method ensures thread-safe UI operations by wrapping UI-related code with `runOnUiThread`.
     */
    private fun gotContact(newContacts: ArrayList<Contact>) {
        allContacts = newContacts

        val privateContacts = MyContactsContentProvider.getContacts(privateCursor)
        if (privateContacts.isNotEmpty()) {
            allContacts.addAll(privateContacts)
            allContacts.sort()
        }

        runOnUiThread {
            if (!checkDialIntent() && binding.dialpadInput.value.isEmpty()) {
                dialpadValueChanged("")
            }
        }
    }

    /**
     * Checks and processes a dial intent, extracting a telephone number if present.
     *
     * This function determines whether the current intent is a dial or view action typically used
     * to initiate a phone call. If the intent contains a data URI with a telephone scheme (`tel:`),
     * it extracts the phone number, updates the dialpad input with this number, and positions the cursor
     * at the end of the number. This prepares the dialpad to make a call or further edit the phone number.
     *
     * The function is designed to handle external intents that request the app to perform a dial action,
     * allowing for seamless integration with other apps and services that may want to initiate calls
     * through this app.
     *
     * @return Returns `true` if the intent is a dial or view action containing a valid `tel:` URI,
     *         indicating that a phone number was successfully extracted and the dialpad input was updated.
     *         Returns `false` otherwise, indicating that the intent did not result in any action by this function.
     *
     * Note: This function relies on the Activity's `intent` property being correctly set before invocation.
     * It modifies the UI state by setting the text of `binding.dialpadInput`, so it should be called at
     * a point where such modifications are safe to perform.
     */
    private fun checkDialIntent(): Boolean {
        return if (intent.action == Intent.ACTION_DIAL || intent.action == Intent.ACTION_VIEW) {
            if (intent.data != null) {
                if (intent.dataString?.contains("tel:") == true) {
                    val number = Uri.decode(intent.dataString).substringAfter("tel:")
                    binding.dialpadInput.setText(number)
                    binding.dialpadInput.setSelection(number.length)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun dialpadValueChanged(text: String) {
        val len = text.length
        if (len > 8 && text.startsWith("*#*#") && text.endsWith("#*#*")) {
            val secretCode = text.substring(4, text.length - 4)
            if (isDefaultDialer()) {
                getSystemService(TelephonyManager::class.java)?.sendDialerSpecialCode(secretCode)
            } else {
                launchSetDefaultDialerIntent()
            }
            return
        }

        (binding.dialpadList.adapter as? ContactsAdapter)?.finishActMode()

        val filtered = allContacts.filter {
            val convertedName = PhoneNumberUtils.convertKeypadLettersToDigits(it.name.normalizeString())

            it.doesContainPhoneNumber(
                text = text, telephonyManager = this.telephonyManager
            ) || (convertedName.contains(text, true))
        }.sortedWith(compareBy {
            !it.doesContainPhoneNumber(
                text = text, telephonyManager = this.telephonyManager
            )
        }).toMutableList() as ArrayList<Contact>

        binding.letterFastScroller.setupWithRecyclerView(binding.dialpadList, { position ->
            try {
                val name = filtered[position].getNameToDisplay()
                val character = if (name.isNotEmpty()) name.substring(0, 1) else ""
                FastScrollItemIndicator.Text(character.uppercase(Locale.getDefault()))
            } catch (e: Exception) {
                FastScrollItemIndicator.Text("")
            }
        })

        ContactsAdapter(
            activity = this,
            contacts = filtered,
            recyclerView = binding.dialpadList,
            highlightText = text
        ) {
            val contact = it as Contact
            if (config.showCallConfirmation) {
                CallConfirmationDialog(this@DialpadActivity, contact.getNameToDisplay()) {
                    startCallIntent(contact.getPrimaryNumber() ?: return@CallConfirmationDialog)
                }
            } else {
                startCallIntent(contact.getPrimaryNumber() ?: return@ContactsAdapter)
            }
        }.apply {
            binding.dialpadList.adapter = this
        }

        binding.dialpadPlaceholder.beVisibleIf(filtered.isEmpty())
        binding.dialpadList.beVisibleIf(filtered.isNotEmpty())
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER && isDefaultDialer()) {
            dialpadValueChanged(binding.dialpadInput.value)
        }
    }

    /**
     * Sets up the options menu for the dialpad toolbar.
     *
     * This function configures the menu item click listener for the dialpad's toolbar. It defines actions for each menu
     * item based on its ID. Currently, it supports the following actions:
     * - Adding a number to contacts: When the 'add_number_to_contact' menu item is selected, it triggers the
     *   `addNumberToContact` function, which is responsible for initiating the process to add the current number
     *   displayed on the dialpad to the user's contacts.
     *
     * If a menu item action is successfully handled, the function returns `true` to indicate that the menu item click
     * event has been consumed. If the menu item ID does not match any predefined actions, the function returns `false`,
     * indicating that the event has not been consumed.
     *
     * This setup is crucial for enabling user interaction with the dialpad toolbar, allowing for extended functionality
     * such as managing contacts directly from the dialpad interface.
     *
     * Note: The actual implementation of the `addNumberToContact` function is not detailed here, as it pertains to the
     * specific logic for adding a number to contacts, which may vary depending on the application's requirements and
     * contact management approach.
     */
    private fun setupOptionsMenu() {
        binding.dialpadToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_number_to_contact -> addNumberToContact()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    /**
     * Initiates an intent to add the current dialpad input number to contacts.
     *
     * This function creates and configures an Intent designed to invoke the contacts application
     * for inserting or editing a contact. The intent's action is set to `ACTION_INSERT_OR_EDIT`,
     * which prompts the user with a contact insertion or editing UI pre-filled with the phone
     * number currently displayed in the dialpad. The specific type of the intent is set to
     * "vnd.android.cursor.item/contact", indicating that the operation concerns contact data.
     *
     * The phone number to be added is retrieved from `dialpadInput`'s current value and passed
     * to the intent as an extra. The intent is then launched using `launchActivityIntent`, which
     * is assumed to handle the process of starting an activity with the given intent.
     *
     * Note: This function depends on the `binding.dialpadInput.value` for the phone number, which
     * means it directly interacts with the UI elements of the dialpad. It is designed to be called
     * when a user indicates the intention to add the currently entered phone number to their contacts,
     * typically through a menu option or button press in the dialpad UI.
     */
    private fun addNumberToContact() {
        Intent().apply {
            action = Intent.ACTION_INSERT_OR_EDIT
            type = "vnd.android.cursor.item/contact"
            putExtra(KEY_PHONE, binding.dialpadInput.value)
            launchActivityIntent(this)
        }
    }

    /**
     * Initiates a phone call with the specified number and SIM card.
     *
     * This function attempts to initiate a phone call to the provided number. It checks if the number
     * is not empty, and then determines the appropriate method to place the call based on the provided
     * SIM handle index and the device's SIM card configuration. If multiple SIM cards are available,
     * and a specific handle index is provided, it will attempt to use the corresponding SIM card.
     *
     * Additionally, this function respects the user's preference regarding call confirmation. If call
     * confirmation is enabled in the app's configuration (`config.showCallConfirmation`), it will display
     * a confirmation dialog before proceeding with the call. Otherwise, it directly initiates the call.
     *
     * @param number The phone number to call. Defaults to the value currently entered in the dialpad input.
     * @param handleIndex An index indicating which SIM card to use for the call. A value of -1 indicates
     *        that the default SIM card settings should be used. For devices with multiple SIMs, a value of
     *        0 typically refers to the first SIM card, and 1 refers to the second SIM card.
     *
     * Note: This function does not directly place the call but prepares and initiates the calling process.
     * Actual call initiation may involve system-level actions outside the control of this app, including
     * user interactions such as accepting the call confirmation prompt, if enabled.
     */
    private fun initCall(number: String = binding.dialpadInput.value, handleIndex: Int) {
        if (number.isNotEmpty()) {
            if (handleIndex != -1 && areMultipleSIMsAvailable()) {
                if (config.showCallConfirmation) {
                    CallConfirmationDialog(this, number) {
                        callContactWithSim(number, handleIndex == 0)
                    }
                } else {
                    callContactWithSim(number, handleIndex == 0)
                }
            } else {
                if (config.showCallConfirmation) {
                    CallConfirmationDialog(this, number) {
                        startCallIntent(number)
                    }
                } else {
                    startCallIntent(number)
                }
            }
        }
    }

    /**
     * Initiates a speed dial call based on a single-digit input.
     *
     * This function checks if the current input in the dialpad is a single digit and attempts to find
     * a corresponding speed dial entry with the specified ID. If a valid speed dial entry is found,
     * a call to the associated number is initiated using the `initCall` function.
     *
     * Speed dial entries are retrieved from a collection of `speedDialValues`, where each entry is
     * expected to have an `id` and a `number`, along with a method `isValid()` to verify its
     * validity. The function uses the default SIM card settings for the call initiation by passing -1
     * as the handle index to `initCall`.
     *
     * @param id The identifier of the speed dial entry. This typically corresponds to the single-digit
     *           input provided by the user on the dialpad.
     * @return Returns `true` if a valid speed dial entry is found and the call initiation process
     *         is started. Returns `false` if the dialpad input is not a single digit or no valid
     *         speed dial entry is found for the given ID.
     *
     * Note: This function assumes that `speedDialValues` is a collection available within the scope
     * of the class that contains valid speed dial entries. The actual call is initiated through
     * `initCall`, which handles the specifics of dialing the number.
     */
    private fun speedDial(id: Int): Boolean {
        if (binding.dialpadInput.value.length == 1) {
            val speedDial = speedDialValues.firstOrNull {
                it.id == id
            }

            if (speedDial?.isValid() == true) {
                initCall(speedDial.number, -1)
                return true
            }
        }
        return false
    }

    private fun dialpadPressed(char: Char, view: View?) {
        binding.dialpadInput.addCharacter(char)
        maybePerformDialpadHapticFeedback(view)
    }

    private fun clearChar(view: View) {
        binding.dialpadInput.dispatchKeyEvent(binding.dialpadInput.getKeyEvent(KeyEvent.KEYCODE_DEL))
        maybePerformDialpadHapticFeedback(view)
    }

    private fun clearInput() {
        binding.dialpadInput.setText("")
    }

    private fun maybePerformDialpadHapticFeedback(view: View?) {
        if (config.dialpadVibration) {
            view?.performHapticFeedback()
        }
    }

    private fun startDialpadTone(char: Char) {
        if (config.dialpadBeeps) {
            pressedKeys.add(char)
            toneGeneratorHelper?.startTone(char)
        }
    }

    private fun stopDialpadTone(char: Char) {
        if (config.dialpadBeeps) {
            if (!pressedKeys.remove(char)) return
            if (pressedKeys.isEmpty()) {
                toneGeneratorHelper?.stopTone()
            } else {
                startDialpadTone(pressedKeys.last())
            }
        }
    }

    /**
     * Executes a long click action for a given dialpad character view.
     *
     * This function defines the action to be taken when a long click is performed on a dialpad character view.
     * Specifically, for the character '0', it replaces the character with a '+', simulating a common shortcut
     * used in international dialing. For other characters, it attempts to initiate a speed dial action, identified
     * by the numeric value of the character.
     *
     * The function checks the character associated with the long-pressed view:
     * - If the character is '0', it first clears the current input (if any) from the view, then simulates
     *   a press of the '+' character.
     * - For any other character, it translates the character to its digit value and attempts to execute
     *   a speed dial action through the `speedDial` function. If the speed dial action is successful,
     *   it stops any ongoing dialpad tone and clears the current input.
     *
     * @param view The View corresponding to the dialpad character that was long-pressed.
     * @param char The character associated with the view, which determines the action to be executed.
     *
     * Note: This function relies on external functions `clearChar`, `dialpadPressed`, and `stopDialpadTone`
     * for its operations. Ensure these are implemented and accessible within the same class or context.
     */
    private fun performLongClick(view: View, char: Char) {
        if (char == '0') {
            clearChar(view)
            dialpadPressed('+', view)
        } else {
            val result = speedDial(char.digitToInt())
            if (result) {
                stopDialpadTone(char)
                clearChar(view)
            }
        }
    }

    /**
     * Configures touch interactions for a dialpad character view.
     *
     * This function sets up touch handling for a given view representing a dialpad character. It enables click
     * and, optionally, long-click behaviors for the view. Touch interactions trigger visual feedback, sound feedback,
     * and potentially a long press action depending on the `longClickable` parameter.
     *
     * @param view The View associated with a dialpad character that should respond to touch events.
     * @param char The character represented by the view, which will be used for feedback and actions upon touch.
     * @param longClickable A Boolean indicating if the view should handle long press actions. Default is `true`.
     *
     * Touch events are handled as follows:
     * - `ACTION_DOWN`: Initiates visual and sound feedback, and sets up a long press action if enabled.
     * - `ACTION_UP` and `ACTION_CANCEL`: Stops the sound feedback and cancels any pending long press actions.
     * - `ACTION_MOVE`: Checks if the touch event is still within the bounds of the view and, if not, stops sound feedback
     *   and cancels the long press action.
     *
     * The function uses `dialpadPressed` and `startDialpadTone` for immediate feedback on touch down, and `performLongClick`
     * for handling long press actions. `stopDialpadTone` is called to halt sound feedback when the touch interaction ends
     * or moves outside the view.
     *
     * Note: This function applies an `@SuppressLint` annotation for "ClickableViewAccessibility" to suppress lint warnings
     * about the accessibility of custom touch event handling. It ensures touch events are handled consistently with the
     * visual and functional design of the dialpad while allowing for custom interaction patterns.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupCharClick(view: View, char: Char, longClickable: Boolean = true) {
        view.isClickable = true
        view.isLongClickable = true
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dialpadPressed(char, view)
                    startDialpadTone(char)
                    if (longClickable) {
                        longPressHandler.removeCallbacksAndMessages(null)
                        longPressHandler.postDelayed({
                            performLongClick(view, char)
                        }, longPressTimeout)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopDialpadTone(char)
                    if (longClickable) {
                        longPressHandler.removeCallbacksAndMessages(null)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    val viewContainsTouchEvent = if (event.rawX.isNaN() || event.rawY.isNaN()) {
                        false
                    } else {
                        view.boundingBox.contains(event.rawX.roundToInt(), event.rawY.roundToInt())
                    }

                    if (!viewContainsTouchEvent) {
                        stopDialpadTone(char)
                        if (longClickable) {
                            longPressHandler.removeCallbacksAndMessages(null)
                        }
                    }
                }
            }
            false
        }
    }
}