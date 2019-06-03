package com.garage.aastream

import android.app.Application
import android.content.res.Configuration
import com.garage.aastream.injection.DaggerInjectionComponent
import com.garage.aastream.injection.InjectionComponent
import com.garage.aastream.injection.InjectionModule
import com.garage.aastream.interfaces.OnRotationChangedCallback
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 22.05.2019 10:54.
 * For project: AAStream
 */
@Suppress("unused")
class App : Application() {

    lateinit var component: InjectionComponent
    private var callback: OnRotationChangedCallback? = null

    override fun onCreate() {
        super.onCreate()
        DevLog.init(getString(R.string.app_name), BuildConfig.DEBUG)
        component = DaggerInjectionComponent.builder().injectionModule(InjectionModule(this)).build()
        component.inject(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        DevLog.d("Configuration changed")
        callback?.onRotationChanged()
    }

    fun setRotationCallback(callback: OnRotationChangedCallback) {
        this.callback = callback
    }
}