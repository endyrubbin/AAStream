package com.garage.aastream.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.garage.aastream.App
import com.garage.aastream.handlers.BrightnessHandler
import com.garage.aastream.handlers.DisplayHandler
import com.garage.aastream.handlers.NotificationHandler
import com.garage.aastream.handlers.RotationHandler
import com.garage.aastream.utils.DevLog
import javax.inject.Inject

/**
 * Created by Endy Rubbin on 10.06.2019 15:37.
 * For project: AAStream
 */
class KillerActivity: AppCompatActivity() {

    @Inject lateinit var brightnessHandler: BrightnessHandler
    @Inject lateinit var rotationHandler: RotationHandler
    @Inject lateinit var notificationHandler: NotificationHandler
    @Inject lateinit var displayHandler: DisplayHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as App).component.inject(this)
        DevLog.d("Killer Activity launched")
        intent.removeExtra(NotificationHandler.ACTION_EXIT)
        notificationHandler.clearNotification()
        displayHandler.restoreDisplaySettings()
        brightnessHandler.restoreScreenBrightness()
        rotationHandler.restoreScreenRotation()
        DevLog.d("AAStream values reset - finishing app")
        finishAffinity()
    }
}