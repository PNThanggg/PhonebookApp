package com.app.phonebook.base.extension

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

/**
 * Customizes the back button behavior for a `ComponentActivity`.
 *
 * This extension function for `ComponentActivity` allows for the overriding of the default back button behavior
 * with a custom action. It utilizes the `onBackPressedDispatcher` to intercept back button presses and execute
 * a specified action instead of the default action.
 *
 * By using an `OnBackPressedCallback` with an enabled state, this function ensures that the custom action is
 * executed whenever the back button is pressed. This is particularly useful for handling specific navigation
 * scenarios or executing cleanup tasks before exiting an activity.
 *
 * @param action The lambda expression to execute when the back button is pressed. This replaces the default back
 * press behavior with a custom action.
 *
 * Example usage:
 * ```
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     setContentView(R.layout.your_layout)
 *
 *     handleBackPressed {
 *         // Custom action to perform on back press, e.g., show a dialog, clear a fragment stack, etc.
 *     }
 * }
 * ```
 *
 * Note: This function should be called within the lifecycle of the activity (e.g., onCreate, onStart) to
 * properly register the callback before it's needed.
 */
fun ComponentActivity.handleBackPressed(action: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            action()
        }
    })
}