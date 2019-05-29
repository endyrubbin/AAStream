package com.garage.aastream.interfaces

import com.garage.aastream.models.AppItem

/**
 * Created by Endy Rubbin on 23.05.2019 15:26.
 * For project: AAStream
 */
interface OnAppListLoadedCallback {

    /**
     * Called when device app list has finished loading
     */
    fun onAppListLoaded(apps: ArrayList<AppItem>)

    /**
     * Called when device app list failed to load
     */
    fun onAppListLoadFailed()
}