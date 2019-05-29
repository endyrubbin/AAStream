package com.garage.aastream.interfaces

/**
 * Created by Endy Rubbin on 22.05.2019 13:06.
 * For project: AAStream
 */
interface OnScreenLockCallback {

    /**
     * Called when device lock screen is unlocked
     */
    fun onScreenUnlocked()

    /**
     * Called when device screen is turned on and available for recording
     */
    fun onScreenOn()

    /**
     * Called when device screen is turned off / sleeping
     */
    fun onScreenOff()
}