package com.app.phonebook.interfaces

import android.telecom.Call
import com.app.phonebook.data.models.AudioRoute

interface CallManagerListener {
    fun onStateChanged()
    fun onAudioStateChanged(audioState: AudioRoute)
    fun onPrimaryCallChanged(call: Call)
}
