package com.app.phonebook.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.phonebook.helpers.ACCEPT_CALL
import com.app.phonebook.helpers.CallManager
import com.app.phonebook.helpers.DECLINE_CALL
import com.app.phonebook.presentation.activities.CallActivity

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACCEPT_CALL -> {
                context.startActivity(CallActivity.getStartIntent(context))
                CallManager.accept()
            }

            DECLINE_CALL -> CallManager.reject()
        }
    }
}
