package com.garage.aastream.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.garage.aastream.App
import com.garage.aastream.R
import com.garage.aastream.activities.controllers.CarActivityController
import com.garage.aastream.utils.DevLog
import javax.inject.Inject

/**
 * Created by Endy Rubbin on 22.05.2019 10:44.
 * For project: AAStream
 */
class CarDebugActivity : AppCompatActivity() {

    @Inject lateinit var activityController: CarActivityController

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)
        (application as App).component.inject(this)

        DevLog.d("Car debug activity created")
        activityController.onCreate(window.decorView, windowManager, savedInstanceState == null)
    }

    override fun onResume() {
        super.onResume()
        activityController.onResume()
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
}
