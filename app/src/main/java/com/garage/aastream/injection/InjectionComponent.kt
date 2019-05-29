package com.garage.aastream.injection

import dagger.Component
import com.garage.aastream.App
import com.garage.aastream.activities.CarDebugActivity
import com.garage.aastream.activities.CarMainActivity
import com.garage.aastream.activities.SettingsActivity
import com.garage.aastream.activities.controllers.CarActivityController
import com.garage.aastream.handlers.PreferenceHandler
import javax.inject.Singleton

/**
 * Created by Endy Rubbin on 22.05.2019 14:48.
 * For project: AAStream
 */

@Singleton
@Component(modules = [InjectionModule::class])
interface InjectionComponent {
    fun inject(target: App)
    fun inject(target: SettingsActivity)
    fun inject(target: CarMainActivity)
    fun inject(target: CarDebugActivity)
    fun inject(target: CarActivityController)

    fun exposePreferenceHandler(): PreferenceHandler
}