package com.garage.aastream

import android.app.Application
import com.garage.aastream.injection.DaggerInjectionComponent
import com.garage.aastream.injection.InjectionComponent
import com.garage.aastream.injection.InjectionModule
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 22.05.2019 10:54.
 * For project: AAStream
 */
@Suppress("unused")
class App : Application() {

    lateinit var component: InjectionComponent

    override fun onCreate() {
        super.onCreate()
        DevLog.init(getString(R.string.app_name), BuildConfig.DEBUG)
        component = DaggerInjectionComponent.builder().injectionModule(InjectionModule(this)).build()
        component.inject(this)
    }
}