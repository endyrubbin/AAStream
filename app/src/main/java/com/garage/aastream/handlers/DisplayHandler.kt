package com.garage.aastream.handlers

import android.content.Context
import com.garage.aastream.shell.ShellExecutor
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 02.07.2019 10:21.
 * For project: AAStream
 */
class DisplayHandler(val context: Context, val preferences: PreferenceHandler) {

    /**
     * Change initial display settings
     */
    fun changeDisplaySettings() {
        DevLog.d("Changing display settings")
        if (preferences.getBoolean(PreferenceHandler.KEY_IMMERSIVE_MODE, false)) {
            ShellExecutor("settings put global policy_control immersive.full=*").start()
        }
    }

    /**
     * Update screen size
     */
    fun updateScreenSize(deviceWidth: Int, deviceHeight: Int) {
        DevLog.d("Setting screen size: $deviceWidth $deviceHeight")
        ShellExecutor("wm size " + deviceWidth + "x" + deviceHeight).start()
    }

    /**
     * Restore display settings
     */
    fun restoreDisplaySettings() {
        DevLog.d("Restoring display settings")
        if (preferences.getBoolean(PreferenceHandler.KEY_IMMERSIVE_MODE, false)) {
            ShellExecutor("settings put global policy_control none*").start()
        }
        ShellExecutor("wm size reset").start()
    }
}