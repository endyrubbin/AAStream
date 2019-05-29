package com.garage.aastream.interfaces

import com.garage.aastream.models.AppItem

/**
 * Created by Endy Rubbin on 23.05.2019 15:05.
 * For project: AAStream
 */
interface OnAppClickedCallback {

    /**
     * Called when [AppItem] is selected at adapter position
     */
    fun onAppClicked(app: AppItem)

    /**
     * Called when [AppItem] is long clicked ar adapter position to add to favorites
     */
    fun onAppLongClicked(app: AppItem)
}