package com.garage.aastream.handlers

import android.content.Context
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.support.car.Car
import android.support.car.CarConnectionCallback
import android.support.car.media.CarAudioManager
import android.support.car.media.CarAudioManager.CAR_AUDIO_USAGE_DEFAULT
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 22.05.2019 14:31.
 * For project: AAStream
 */
class AudioHandler(
    context: Context,
    private val preferences: PreferenceHandler
) {

    private var car: Car? = null

    init {
        car = Car.createCar(context, object : CarConnectionCallback() {
            override fun onConnected(car: Car) {
                requestAudioFocus(car)
            }

            override fun onDisconnected(car: Car) {
                abandonAudioFocus(car)
            }
        })
    }

    /**
     * Start audio handler
     */
    fun start() {
        car?.connect()
    }

    /**
     * Stop audio handler
     */
    fun stop() {
        car?.disconnect()
    }

    /**
     * Request car audio focus
     */
    private fun requestAudioFocus(car: Car) {
        if (!preferences.getBoolean(PreferenceHandler.KEY_AUDIO_FOCUS, false)) {
            return
        }
        DevLog.d("RequestAudioFocus")
        try {
            val carAM = car.getCarManager(CarAudioManager::class.java)
            carAM.requestAudioFocus(
                null,
                carAM.getAudioAttributesForCarUsage(CAR_AUDIO_USAGE_DEFAULT),
                AUDIOFOCUS_GAIN,
                0
            )
        } catch (e: Exception) {
            DevLog.d("RequestAudioFocus exception: $e")
        }
    }

    /**
     * Abandon car audio focus
     */
    private fun abandonAudioFocus(car: Car) {
        DevLog.d("AbandonAudioFocus")
        try {
            val carAM = car.getCarManager(CarAudioManager::class.java)
            carAM.abandonAudioFocus(null, carAM.getAudioAttributesForCarUsage(CAR_AUDIO_USAGE_DEFAULT))
        } catch (e: Exception) {
            DevLog.d("AbandonAudioFocus exception: $e")
        }
    }
}