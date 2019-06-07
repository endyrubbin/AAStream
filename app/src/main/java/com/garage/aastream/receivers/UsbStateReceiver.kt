package com.garage.aastream.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 05.06.2019 10:34.
 * For project: AAStream
 */
class UsbStateReceiver(private val callback: UsbStateCallback) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        DevLog.d("USB state changed: ${intent?.action}")
        if(intent?.action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            callback.onUsbDisconnected()
        }
    }

    /**
     * Callback for Usb disconnect state
     */
    interface UsbStateCallback {

        /**
         * Called when USB is disconnected
         */
        fun onUsbDisconnected()
    }
}