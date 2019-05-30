package com.garage.aastream.handlers

import android.app.Application
import android.content.Context
import com.garage.aastream.models.AppItem
import com.garage.aastream.models.AppItemWrapper
import com.garage.aastream.utils.Const
import com.google.gson.Gson
import java.util.*

/**
 * Created by Endy Rubbin on 22.05.2019 14:31.
 * For project: AAStream
 */
class PreferenceHandler(context: Application) {
    private val preferences = context.getSharedPreferences(Const.PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson: Gson = Gson()

    /**
     * Save a boolean value preferences
     */
    fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Load a boolean value from preferences
     */
    fun getBoolean(key: String, value: Boolean): Boolean {
        return preferences.getBoolean(key, value)
    }

    /**
     * Save a int value preferences
     */
    fun putInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    /**
     * Load a int value from preferences
     */
    fun getInt(key: String, value: Int): Int {
        return preferences.getInt(key, value)
    }

    /**
     * Save a list of [AppItem]s in preferences
     */
    fun putFavorites(apps: ArrayList<AppItem>) {
        val data = gson.toJson(AppItemWrapper(apps))
        preferences.edit().putString(KEY_FAVORITE_APPS, data).apply()
    }

    /**
     * Load a list of [AppItem]s from preferences
     */
    fun getFavorites(): ArrayList<AppItem> {
        val data = preferences.getString(KEY_FAVORITE_APPS, null)
        return if (data != null) {
            gson.fromJson(data, AppItemWrapper::class.java).apps
        } else ArrayList()
    }

    companion object {
        const val KEY_REQUEST_AUDIO_FOCUS = "request_audio_focus_on_connect"
        const val KEY_FAVORITE_APPS = "favorite_app_list"
        const val KEY_ROTATION_SWITCH = "rotation_switch"
        const val KEY_ROTATION_VALUE = "rotation_value"
        const val KEY_BRIGHTNESS_SWITCH = "brightness_switch"
        const val KEY_BRIGHTNESS_VALUE = "brightness_value"
        const val KEY_SIDEBAR_SWITCH = "sidebar_switch"
        const val KEY_STARTUP_VALUE = "sidebar_value"
        const val KEY_DEBUG_ENABLED = "debug_enabled"
        const val KEY_OPEN_MENU_METHOD = "menu_open_method"
    }
}
