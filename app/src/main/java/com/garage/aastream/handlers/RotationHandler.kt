package com.garage.aastream.handlers

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.provider.Settings.System.ACCELEROMETER_ROTATION
import android.provider.Settings.System.USER_ROTATION
import android.view.Surface
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 26.05.2019 20:27.
 * For project: AAStream
 */
class RotationHandler(val context: Context, val preferences: PreferenceHandler) {

    private val systemAutoRotation = Settings.System.getInt(context.contentResolver, ACCELEROMETER_ROTATION)

    /**
     * @return true if rotation can be changed
     */
    private fun canChangeRotation(): Boolean {
        return preferences.getBoolean(PreferenceHandler.KEY_ROTATION_SWITCH, false) &&
                (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context))
    }

    /**
     * @return current / saved screen brightness
     */
    fun getScreenRotation(): Int {
        return if (canChangeRotation()) {
            preferences.getInt(PreferenceHandler.KEY_ROTATION_VALUE, 0)
        } else {
            0
        }
    }

    /**
     * Update device screen brightness
     */
    fun setScreenRotation(value: Int = getScreenRotation(), autoRotation: Int = 0) {
        if (canChangeRotation()) {
            DevLog.d("Changing screen rotation: $value $systemAutoRotation")
            Settings.System.putInt(context.contentResolver, ACCELEROMETER_ROTATION, autoRotation)
            Settings.System.putInt(context.contentResolver, USER_ROTATION, getOrientation(value))
        }
    }

    /**
     * @return System rotation value for selected option
     */
    private fun getOrientation(value: Int): Int {
        return when(value) {
            0 -> Surface.ROTATION_0
            1 -> Surface.ROTATION_90
            2 -> Surface.ROTATION_180
            3 -> Surface.ROTATION_270
            else -> Surface.ROTATION_0
        }
    }

    /**
     * Restore previous screen brightness
     */
    fun restoreScreenRotation() {
        DevLog.d("Restoring screen rotation")
        setScreenRotation(Surface.ROTATION_0, systemAutoRotation)
    }
}