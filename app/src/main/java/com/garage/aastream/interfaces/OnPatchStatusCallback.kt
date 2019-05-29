package com.garage.aastream.interfaces

/**
 * Created by Endy Rubbin on 27.05.2019 10:33.
 * For project: AAStream
 */
interface OnPatchStatusCallback {

    /**
     * Called if patch was successful
     */
    fun onPatchSuccessful()

    /**
     * Called if patch has failed
     */
    fun onPatchFailed()
}