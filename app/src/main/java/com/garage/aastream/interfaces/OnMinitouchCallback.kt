package com.garage.aastream.interfaces

/**
 * Created by Endy Rubbin on 10.06.2019 14:20.
 * For project: AAStream
 */
interface OnMinitouchCallback {

    /**
     * Called when minitouch is installed
     */
    fun onInstalled(path: String)

    /**
     * Called when minitouch has failed
     */
    fun onFailed()
}