package com.garage.aastream.models

import android.graphics.drawable.Drawable

/**
 * Created by Endy Rubbin on 23.05.2019 15:03.
 * For project: AAStream
 */
data class AppItem(val label: String, val packageName: String, val icon: String?, var favorite: Boolean = false) {

    @Transient var drawable: Drawable? = null

    fun equalTo(app: AppItem): Boolean {
        return this.packageName == app.packageName
    }
}