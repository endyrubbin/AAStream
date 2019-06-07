package com.garage.aastream.activities

import android.content.res.Configuration
import android.os.Bundle
import com.garage.aastream.App
import com.garage.aastream.R
import com.garage.aastream.activities.controllers.CarActivityController
import com.garage.aastream.handlers.NotificationHandler
import com.garage.aastream.utils.DevLog
import com.google.android.apps.auto.sdk.CarActivity
import javax.inject.Inject

/**
 * Created by Endy Rubbin on 22.05.2019 10:44.
 * For project: AAStream
 */
class CarMainActivity : CarActivity() {

    @Inject lateinit var activityController: CarActivityController

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)
        (applicationContext as App).component.inject(this)
        (applicationContext as App).setRotationCallback(activityController)

        DevLog.d("Car debug activity created")
        activityController.onCreate(c().decorView, c().windowManager,
            savedInstanceState == null, carUiController)
        @Suppress("DEPRECATION")
        setIgnoreConfigChanges(0xFFFF)
    }

    override fun onResume() {
        super.onResume()
        if (intent.hasExtra(NotificationHandler.ACTION_EXIT)) {
            intent.removeExtra(NotificationHandler.ACTION_EXIT)
            activityController.finish()
        } else {
            activityController.onResume()
        }
    }

    override fun onStart() {
        super.onStart()
        activityController.onStart()
    }

    override fun onStop() {
        super.onStop()
        activityController.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityController.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        activityController.onConfigurationChanged()
    }

    override fun onWindowFocusChanged(focus: Boolean, b1: Boolean) {
        super.onWindowFocusChanged(focus, b1)
        DevLog.d("Window focus changed $focus")
        if (focus) {
            activityController.onWindowFocusChanged()
        }
    }
}
