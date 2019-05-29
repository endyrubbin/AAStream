package com.garage.aastream.handlers

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.provider.Settings.System.SCREEN_BRIGHTNESS
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 26.05.2019 19:41.
 * For project: AAStream
 */
class BrightnessHandler(val context: Context, val preferences: PreferenceHandler) {

    private val systemBrightness = (Settings.System.getInt(context.contentResolver,
        SCREEN_BRIGHTNESS).toFloat() / 255 * 100).toInt()

    /**
     * @return true if brightness can be changed
     */
    private fun canChangeBrightness(): Boolean {
        return preferences.getBoolean(PreferenceHandler.KEY_BRIGHTNESS_SWITCH, false) &&
                (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context))
    }

    /**
     * @return current / saved screen brightness
     */
    fun getScreenBrightness(): Int {
        return if (canChangeBrightness()) {
            preferences.getInt(PreferenceHandler.KEY_BRIGHTNESS_VALUE, systemBrightness)
        } else {
            systemBrightness
        }
    }

    /**
     * Update device screen brightness
     */
    fun setScreenBrightness(value: Int = getScreenBrightness()) {
        if (canChangeBrightness()) {
            DevLog.d("Changing screen brightness: $value $systemBrightness")
            Settings.System.putInt(context.contentResolver, SCREEN_BRIGHTNESS, 255 * value / 100)
        }
    }

    /**
     * Restore previous screen brightness
     */
    fun restoreScreenBrightness() {
        DevLog.d("Restoring screen brightness $systemBrightness")
        setScreenBrightness(systemBrightness)
    }
}