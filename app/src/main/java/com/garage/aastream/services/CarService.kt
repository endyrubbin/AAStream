package com.garage.aastream.services

import com.google.android.apps.auto.sdk.CarActivity
import com.google.android.apps.auto.sdk.CarActivityService
import com.garage.aastream.activities.CarMainActivity

/**
 * Created by Endy Rubbin on 23.05.2019 12:25.
 * For project: AAStream
 */
class CarService : CarActivityService() {

    /**
     * Called from Android Auto to start [CarMainActivity]
     */
    @Suppress("UNCHECKED_CAST")
    override fun getCarActivity(): Class<out CarActivity> {
        return CarMainActivity::class.java
    }
}