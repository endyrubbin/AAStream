package com.garage.aastream.minitouch

import android.os.AsyncTask
import com.garage.aastream.interfaces.OnMinitouchCallback
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 10.06.2019 14:27.
 * For project: AAStream
 */
class MinitouchDaemon(
    private val miniTouchHandler: MiniTouchHandler,
    val callback: OnMinitouchCallback
) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg voids: Void): Void? {
        DevLog.d("Minitouch daemon started")
        miniTouchHandler.start(callback)
        return null
    }
}