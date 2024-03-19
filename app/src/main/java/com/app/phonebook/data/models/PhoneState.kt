package com.app.phonebook.data.models

import android.telecom.Call

sealed class PhoneState

data object NoCall : PhoneState()

class SingleCall(val call: Call) : PhoneState()

class TwoCalls(val active: Call, val onHold: Call) : PhoneState()