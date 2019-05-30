package com.garage.aastream.injection

import android.app.Application
import com.garage.aastream.activities.controllers.CarActivityController
import com.garage.aastream.activities.controllers.TerminalController
import com.garage.aastream.handlers.*
import com.garage.aastream.minitouch.MiniTouchHandler
import com.garage.aastream.utils.PhenotypePatcher
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Endy Rubbin on 22.05.2019 14:44.
 * For project: AAStream
 */
@Module
class InjectionModule(private val context: Application) {

    @Singleton
    @Provides
    fun provideAudioHandler(preferences: PreferenceHandler): AudioHandler {
        return AudioHandler(context, preferences)
    }

    @Singleton
    @Provides
    fun provideMiniTouchHandler(preferences: PreferenceHandler): MiniTouchHandler {
        return MiniTouchHandler(context, preferences)
    }

    @Singleton
    @Provides
    fun providePreferenceHandler(): PreferenceHandler {
        return PreferenceHandler(context)
    }

    @Singleton
    @Provides
    fun provideAppHandler(preferences: PreferenceHandler): AppHandler {
        return AppHandler(context, preferences)
    }

    @Singleton
    @Provides
    fun provideBrightnessHandler(preferences: PreferenceHandler): BrightnessHandler {
        return BrightnessHandler(context, preferences)
    }

    @Singleton
    @Provides
    fun provideRotationHandler(preferences: PreferenceHandler): RotationHandler {
        return RotationHandler(context, preferences)
    }

    @Singleton
    @Provides
    fun providePhenotypePatcher(): PhenotypePatcher {
        return PhenotypePatcher(context)
    }

    @Singleton
    @Provides
    fun provideCarActivityController(): CarActivityController {
        return CarActivityController(context)
    }

    @Singleton
    @Provides
    fun provideTerminalController(): TerminalController {
        return TerminalController()
    }
}