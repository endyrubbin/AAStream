package com.garage.aastream.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import com.garage.aastream.interfaces.OnScreenLockCallback
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 22.05.2019 13:06.
 * For project: AAStream
 *
 * Used to listen for screen state changes
 */
class ScreenLockReceiver(
    private val callback: OnScreenLockCallback
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        DevLog.d("Screen state changed: ${intent?.action}")
        when(intent?.action) {
            ACTION_USER_PRESENT -> callback.onScreenUnlocked()
            ACTION_SCREEN_ON -> callback.onScreenOn()
            ACTION_SCREEN_OFF -> callback.onScreenOff()
        }
    }
}