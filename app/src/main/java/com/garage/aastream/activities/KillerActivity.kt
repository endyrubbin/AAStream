package com.garage.aastream.activities

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.garage.aastream.App
import com.garage.aastream.handlers.BrightnessHandler
import com.garage.aastream.handlers.NotificationHandler
import com.garage.aastream.handlers.RotationHandler
import com.garage.aastream.shell.ShellExecutor
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as App).component.inject(this)
        DevLog.d("Killer Activity launched")
        intent.removeExtra(NotificationHandler.ACTION_EXIT)
        DevLog.d("Resetting screen size")
        ShellExecutor("wm size reset").start()
        ShellExecutor("wm density reset").start()
        notificationHandler.clearNotification()
        brightnessHandler.restoreScreenBrightness()
        rotationHandler.restoreScreenRotation()
        DevLog.d("AAStream values reset - finishing app")
        finishAffinity()
        Handler().postDelayed({
            System.exit(0)
        }, 1000)
    }
}