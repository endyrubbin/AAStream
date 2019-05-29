package com.garage.aastream.utils
import android.util.Log
import com.garage.aastream.interfaces.OnLogCallback

import java.lang.reflect.Array

/**
 * Created by Endy Rubbin on 22.05.2019 13:11.
 * For project: AAStream
 */
@Suppress("unused")
object DevLog {

    private var DEBUG = false
    private var TAG = "DevLog"

    private var callback: OnLogCallback? = null

    /**
     * Init logger with default TAG
     */
    fun init() {
        DEBUG = true
    }

    /**
     * Init logger with TAG and set enabled / disabled
     * @param tag   String Log TAG
     * @param debug boolean logging enabled / disabled
     */
    fun init(tag: String, debug: Boolean = true) {
        DEBUG = debug
        TAG = tag
    }

    /**
     * Set callback for log events
     */
    fun setCallback(callback: OnLogCallback) {
        this.callback = callback
    }

    /**
     * Remove callback for log events
     */
    fun removeCallback() {
        this.callback = null
    }

    /**
     * Logger - outputs log messages and delivers callback if set
     */
    fun d(vararg msg: Any?) {
        if (DEBUG) {
            val stackTraces = Thread.currentThread().stackTrace
            val stackTraceElement = stackTraces[3]
            val lineNumber = stackTraceElement.lineNumber.toString()
            var className = getClassName(stackTraceElement.className)
            val extension = getClassName(stackTraceElement.fileName)
            className =
                if (className.contains("$")) className.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] else className
            val out = StringBuilder("($className.$extension:$lineNumber): ")
            for (o in msg) {
                if (o == null) {
                    out.append("NULL, ")
                } else {
                    if (o.javaClass.isArray) {
                        out.append("[")
                        for (i in 0 until Array.getLength(o)) {
                            out.append(Array.get(o, i)).append(", ")
                        }
                        out.append("], ")
                    } else {
                        out.append(o.toString()).append(", ")
                    }
                }
            }
            val log = out.substring(0, out.length - 2)
            callback?.onLogWritten(log)
            Log.d(TAG, log)
        }
    }

    /**
     * Returns current class name
     * @param className String
     * @return String
     */
    private fun getClassName(className: String): String {
        val parts = className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return parts[parts.size - 1]
    }
}