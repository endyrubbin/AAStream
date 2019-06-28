package com.garage.aastream.activities

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.garage.aastream.App
import com.garage.aastream.BuildConfig
import com.garage.aastream.R
import com.garage.aastream.handlers.BrightnessHandler
import com.garage.aastream.handlers.PreferenceHandler
import com.garage.aastream.handlers.RotationHandler
import com.garage.aastream.interfaces.OnPatchStatusCallback
import com.garage.aastream.utils.Const
import com.garage.aastream.utils.DevLog
import com.garage.aastream.utils.PhenotypePatcher
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.view_settings_about.*
import kotlinx.android.synthetic.main.view_settings_brightness.*
import kotlinx.android.synthetic.main.view_settings_debug.*
import kotlinx.android.synthetic.main.view_settings_resize.*
import kotlinx.android.synthetic.main.view_settings_rotation.*
import kotlinx.android.synthetic.main.view_settings_sidebar.*
import kotlinx.android.synthetic.main.view_settings_unlock.*
import javax.inject.Inject

/**
 * Created by Endy Rubbin on 22.05.2019 10:44.
 * For project: AAStream
 */
class SettingsActivity : AppCompatActivity() {

    @Inject lateinit var preferences: PreferenceHandler
    @Inject lateinit var brightnessHandler: BrightnessHandler
    @Inject lateinit var rotationHandler: RotationHandler
    @Inject lateinit var patcher: PhenotypePatcher

    private var previousTime: Long = 0
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        (application as App).component.inject(this)

        initViews()
    }

    /**
     * Check if app can modify System settings
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun checkForSystemWritePermission() {
        if (!Settings.System.canWrite(this)) {
            startActivity(Intent(ACTION_MANAGE_WRITE_SETTINGS))
        }
    }

    /**
     * Initialize views and set listeners
     */
    private fun initViews() {
        // Debug controller
        settings_debug_activity_holder.setOnClickListener {
            startActivity(Intent(this, CarDebugActivity::class.java))
        }
        settings_debug_switch.setOnCheckedChangeListener { _, isChecked ->
            DevLog.d("Debug switch changed: $isChecked")
            preferences.putBoolean(PreferenceHandler.KEY_DEBUG_DISABLED, isChecked)
            view_settings_debug.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
        settings_debug_switch.isChecked = false
        view_settings_debug.visibility = if (preferences.getBoolean(PreferenceHandler.KEY_DEBUG_DISABLED, true)) {
            View.GONE
        } else {
            View.VISIBLE
        }

        // Unlock controller
        view_settings_unlock.setOnClickListener { unlock() }
        settings_unlock_state_icon.visibility = if (patcher.isPatched()) View.VISIBLE else View.GONE

        // Brightness controller
        settings_brightness_seek_bar.progress = brightnessHandler.getScreenBrightness()
        settings_brightness_seek_bar.max = Const.MAX_VALUE
        settings_brightness_seek_bar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                DevLog.d("Brightness value changed $progress")
                preferences.putInt(PreferenceHandler.KEY_BRIGHTNESS_VALUE, progress)
            }
        })
        settings_brightness_switch.isChecked = preferences.getBoolean(PreferenceHandler.KEY_BRIGHTNESS_SWITCH, false)
        settings_brightness_switch.setOnCheckedChangeListener { _, isChecked ->
            DevLog.d("Brightness switch changed: $isChecked")
            if (isChecked) {
                checkForSystemWritePermission()
            }
            preferences.putBoolean(PreferenceHandler.KEY_BRIGHTNESS_SWITCH, isChecked)
            settings_brightness_seek_bar.isEnabled = isChecked
        }
        settings_brightness_seek_bar.isEnabled = settings_brightness_switch.isChecked

        // Rotation controller
        val rotationAdapter = ArrayAdapter.createFromResource(this, R.array.rotation_values,
            android.R.layout.simple_spinner_item)
        rotationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        settings_rotation_dropdown.adapter = rotationAdapter
        settings_rotation_dropdown.setSelection(rotationHandler.getScreenRotation())
        settings_rotation_dropdown.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                DevLog.d("Rotation selected $position")
                preferences.putInt(PreferenceHandler.KEY_ROTATION_VALUE, position)
            }
        }
        settings_rotation_switch.isChecked = preferences.getBoolean(PreferenceHandler.KEY_ROTATION_SWITCH, false)
        settings_rotation_switch.setOnCheckedChangeListener { _, isChecked ->
            DevLog.d("Rotation switch changed: $isChecked")
            if (isChecked) {
                checkForSystemWritePermission()
            }
            preferences.putBoolean(PreferenceHandler.KEY_ROTATION_SWITCH, isChecked)
            settings_rotation_dropdown.isEnabled = isChecked
        }
        settings_rotation_dropdown.isEnabled = settings_rotation_switch.isChecked

        // Resize controller
        settings_resize_switch.isChecked = preferences.getBoolean(PreferenceHandler.KEY_RESIZE_ENABLED, false)
        settings_resize_switch.setOnCheckedChangeListener { _, isChecked ->
            DevLog.d("Resize switch changed: $isChecked")
            if (isChecked) {
                checkForSystemWritePermission()
            }
            preferences.putBoolean(PreferenceHandler.KEY_RESIZE_ENABLED, isChecked)
        }

        // Sidebar controller
        val sidebarAdapter = ArrayAdapter.createFromResource(this, R.array.screen_values,
            android.R.layout.simple_spinner_item)
        sidebarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        settings_sidebar_dropdown.adapter = sidebarAdapter
        settings_sidebar_dropdown.setSelection(preferences.getInt(PreferenceHandler.KEY_STARTUP_VALUE,
            Const.DEFAULT_SCREEN))
        settings_sidebar_dropdown.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                DevLog.d("Startup screen selected $position")
                preferences.putInt(PreferenceHandler.KEY_STARTUP_VALUE, position)
            }
        }
        val sidebarMenuAdapter = ArrayAdapter.createFromResource(this, R.array.tap_values,
            android.R.layout.simple_spinner_item)
        sidebarMenuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        settings_sidebar_dropdown_menu.adapter = sidebarMenuAdapter
        settings_sidebar_dropdown_menu.setSelection(preferences.getInt(PreferenceHandler.KEY_OPEN_MENU_METHOD,
            Const.DEFAULT_TAP_METHOD))
        settings_sidebar_dropdown_menu.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                DevLog.d("Sidebar open method selected $position")
                preferences.putInt(PreferenceHandler.KEY_OPEN_MENU_METHOD, position)
            }
        }
        settings_sidebar_switch.isChecked = preferences.getBoolean(PreferenceHandler.KEY_SIDEBAR_SWITCH,
            Const.DEFAULT_SHOW_SIDEBAR)
        settings_sidebar_switch.setOnCheckedChangeListener { _, isChecked ->
            DevLog.d("Sidebar switch changed: $isChecked")
            preferences.putBoolean(PreferenceHandler.KEY_SIDEBAR_SWITCH, isChecked)
        }

        // About controller
        settings_about_version.text = getString(R.string.txt_version,
            "${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}")
        settings_about.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - previousTime <= Const.CLICK_INTERVAL) {
                count++
            } else {
                count = 0
            }

            previousTime = currentTime
            if (count == Const.DEBUG_CLICK_COUNT) {
                DevLog.d("Debug mode enabled")
                Toast.makeText(this@SettingsActivity, getString(R.string.toast_developer_mode_enabled),
                    Toast.LENGTH_LONG).show()
                preferences.putBoolean(PreferenceHandler.KEY_DEBUG_DISABLED, false)
                view_settings_debug.visibility = View.VISIBLE
                settings_debug_switch.isChecked = false
            } else if (count >= Const.DEBUG_CLICK_COUNT - 3 && count < Const.DEBUG_CLICK_COUNT) {
                Toast.makeText(this@SettingsActivity, getString(R.string.toast_developer_mode_click,
                    (Const.DEBUG_CLICK_COUNT - count)), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * White list this app for Android Auto
     * Reference: @see <a href="https://github.com/Eselter/AA-Phenotype-Patcher">AA-Phenotype-Patcher</a>
     */
    private fun unlock() {
        settings_unlock_state_spinner.visibility = View.VISIBLE
        settings_unlock_state_icon.visibility = View.GONE
        patcher.patch(object : OnPatchStatusCallback{
            override fun onPatchSuccessful() {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity,
                        getString(R.string.toast_app_whitelisted), Toast.LENGTH_LONG).show()
                    settings_unlock_state_icon.visibility = View.VISIBLE
                    settings_unlock_state_spinner.visibility = View.GONE
                }
            }

            override fun onPatchFailed() {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity,
                        getString(R.string.toast_root_not_available), Toast.LENGTH_LONG).show()
                    settings_unlock_state_icon.visibility = View.GONE
                    settings_unlock_state_spinner.visibility = View.GONE
                }
            }
        })
    }
}
