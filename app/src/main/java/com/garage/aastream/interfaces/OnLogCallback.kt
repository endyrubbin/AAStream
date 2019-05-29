package com.garage.aastream.interfaces

/**
 * Created by Endy Rubbin on 28.05.2019 13:35.
 * For project: AAStream
 */
interface OnLogCallback {

    /**
     * Called when a log line is written to console
     */
    fun onLogWritten(log: String)
}