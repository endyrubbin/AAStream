package com.garage.aastream.activities.controllers

import android.os.Handler
import android.os.Looper
import android.view.View
import kotlinx.android.synthetic.main.view_car_terminal.view.*
import com.garage.aastream.interfaces.OnLogCallback
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 28.05.2019 13:32.
 * For project: AAStream
 */
class TerminalController : OnLogCallback {

    private lateinit var rootView: View

    /**
     * Initialize the controller and add listener for logs
     */
    fun init(rootView: View) {
        this.rootView = rootView
        DevLog.setCallback(this)

        this.rootView.terminal_input_button.setOnClickListener {
            this.rootView.terminal_input.text.toString().takeIf { it.isNotEmpty() }?.let {
                DevLog.d("root@aa-stream:-$ $it")
                this.rootView.terminal_input.setText("")
            }
        }
    }

    /**
     * Remove log event callbacks
     */
    fun stop() {
        DevLog.removeCallback()
    }

    /**
     * Called when logs are written
     */
    override fun onLogWritten(log: String) {
        Handler(Looper.getMainLooper()).post {
            rootView.terminal_console.append(if (rootView.terminal_console.text.isEmpty()) "" else "\n")
            rootView.terminal_console.append(log)
            rootView.terminal_scroller.post { rootView.terminal_scroller.fullScroll(View.FOCUS_DOWN) }
        }
    }
}