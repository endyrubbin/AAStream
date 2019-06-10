package com.garage.aastream.minitouch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.Surface.*
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import com.garage.aastream.handlers.PreferenceHandler
import com.garage.aastream.interfaces.OnMenuTapCallback
import com.garage.aastream.interfaces.OnMinitouchCallback
import com.garage.aastream.utils.DevLog
import com.garage.aastream.views.FingerTapDetector
import eu.chainfire.libsuperuser.Shell
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Endy Rubbin on 22.05.2019 13:25.
 * For project: AAStream
 */
class MiniTouchHandler(
    private val context: Context,
    private val preferenceHandler: PreferenceHandler
): View.OnTouchListener {

    private var fingerTapDetector: FingerTapDetector? = null
    private val miniTouchSocket: MiniTouchSocket = MiniTouchSocket()
    private var surfaceView: SurfaceView? = null
    private var callback: OnMinitouchCallback? = null

    private var deviceScreenSize = Point()
    private var deviceDisplaySize = Point()
    private var deviceScreenRotation: Int = ROTATION_0
    private var screenRotation: Int = ROTATION_0
    private var screenWidth = 0.0
    private var screenHeight = 0.0
    private var projectionOffsetX = 0.0
    private var projectionOffsetY = 0.0
    private var projectionWidth = 0.0
    private var projectionHeight = 0.0
    var isInstalled = false

    fun getDeviceDisplayWidth(): Int {
        return deviceDisplaySize.x
    }

    /**
     * Reads and updates current screen values
     */
    fun updateValues() {
        val windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealSize(deviceScreenSize)
        deviceScreenRotation = windowManager.defaultDisplay.rotation
        DevLog.d("Updating values: $deviceScreenRotation")
        if (deviceScreenRotation == ROTATION_0 || deviceScreenRotation == ROTATION_180) {
            deviceDisplaySize.x = deviceScreenSize.x
            deviceDisplaySize.y = deviceScreenSize.y
        } else {
            deviceDisplaySize.x = deviceScreenSize.y
            deviceDisplaySize.y = deviceScreenSize.x
        }
        updateTouchTransformations(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun init(surfaceView: SurfaceView, callback: OnMenuTapCallback) {
        this.surfaceView = surfaceView
        this.surfaceView?.setOnTouchListener(this)
        fingerTapDetector = FingerTapDetector(context, preferenceHandler, callback)
    }

    /**
     * Clear views and callbacks
     */
    fun clear() {
        surfaceView = null
        fingerTapDetector?.removeCallback()
        fingerTapDetector = null
    }

    /**
     * Start mini touch handler
     */
    fun start(callback: OnMinitouchCallback) {
        this.callback = callback
        val path = install()
        DevLog.d("Mini Touch path: $path")
        if (path?.isNotEmpty() == true) {
            callback.onInstalled(path)
        }
    }

    /**
     * Stop mini touch handler
     */
    fun stop() {
        this.callback = null
        val pid = miniTouchSocket.getPid()
        miniTouchSocket.disconnect()
        DevLog.d("Mini Touch stopped: $pid")
        if (pid != 0) {
            Shell.Pool.SU.run("kill $pid")
        }
    }

    /**
     * Install minitouch library
     */
    private fun install(): String? {
        val path = context.filesDir.absolutePath
        val target = File("$path/", "minitouch")
        val folder = File(path)
        try {
            val abi = detectAbi()
            val assetName = "libs/$abi/minitouch"
            DevLog.d("Minitouch already exists: ${folder.exists()}")
            if (!folder.exists()) folder.mkdirs()
            DevLog.d("Installing minitouch $assetName")
            context.resources.assets.open(assetName).use { input ->
                DevLog.d("Asset opened, writing to file: ${target.absolutePath}")
                if (!target.exists()) target.createNewFile()
                FileOutputStream(target.absolutePath).use { output ->
                    DevLog.d("Copying asset to file ${target.absolutePath}")
                    input.copyTo(output)
                    input.close()
                    output.flush()
                    output.close()

                    DevLog.d("Mini Touch installed ${target.absolutePath}")
                    return target.absolutePath
                }
            }
        } catch (e: Exception) {
            return if (target.exists()) {
                DevLog.d("Mini Touch installed ${target.absolutePath}")
                target.absolutePath
            } else {
                e.printStackTrace()
                DevLog.d("Failed to install Mini Touch: $e")
                null
            }
        }
    }

    /**
     * Detect device ABI
     */
    private fun detectAbi(): String {
        var abi: String? = null
        Shell.Pool.SH.run("getprop ro.product.cpu.abi", object : Shell.OnSyncCommandLineListener {
            override fun onSTDERR(line: String?) {
                DevLog.d("Failed to detect Abi: $line")
            }

            override fun onSTDOUT(line: String?) {
                DevLog.d("Shell line read: $line")
                if (abi == null) abi = line
            }
        })
        DevLog.d("Detected Abi: $abi")
        return if (abi != null && abi!!.isNotEmpty()) abi!! else "armeabi"
    }

    /**
     * Handle device screen touch events
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (!miniTouchSocket.isConnected()) {
            if (!isInstalled) {
                DevLog.d("Minitouch not installed")
                callback?.onFailed()
                return false
            }
            miniTouchSocket.connect(callback)
            updateTouchTransformations(true)
        } else {
            updateTouchTransformations(false)
        }

        var isConnected = miniTouchSocket.isConnected()
        val action = event.actionMasked
        var i = 0
        while (i < event.pointerCount && isConnected) {
            val id = event.getPointerId(i)
            val x = (event.getX(i) - projectionOffsetX) / projectionWidth
            val y = (event.getY(i) - projectionOffsetY) / projectionHeight
            val pressure = event.getPressure(i).toDouble()

            var rx = x
            var ry = y
            when (screenRotation) {
                ROTATION_0 -> {
                    rx = x
                    ry = y
                }
                ROTATION_90 -> {
                    rx = 1.0 - y
                    ry = x
                }
                ROTATION_180 -> {
                    rx = 1.0 - x
                    ry = 1.0 - y
                }
                ROTATION_270 -> {
                    rx = y
                    ry = 1.0 - x
                }
            }
            when (action) {
                ACTION_DOWN, ACTION_POINTER_DOWN -> isConnected = isConnected && miniTouchSocket.touchDown(id, rx, ry, pressure)
                ACTION_MOVE -> isConnected = isConnected && miniTouchSocket.touchMove(id, rx, ry, pressure)
                ACTION_UP, ACTION_CANCEL -> isConnected = isConnected && miniTouchSocket.touchUpAll()
                ACTION_POINTER_UP -> isConnected = isConnected && miniTouchSocket.touchUp(id)
            }
            i++
        }

        if (isConnected) {
            miniTouchSocket.touchCommit()
        }

        fingerTapDetector?.onTouchEvent(event)
        return true
    }

    /**
     * Update touch coordinate transformations
     */
    fun updateTouchTransformations(force: Boolean) {
        if (surfaceView == null ||
            deviceScreenRotation == screenRotation &&
            deviceScreenSize.equals(screenWidth.toInt(), screenHeight.toInt())
            && !force) {
            return
        }

        screenRotation = deviceScreenRotation
        screenWidth = deviceScreenSize.x.toDouble()
        screenHeight = deviceScreenSize.y.toDouble()
        val surfaceWidth = surfaceView?.width?.toDouble() ?: 0.0
        val surfaceHeight = surfaceView?.height?.toDouble() ?: 0.0
        val factX = surfaceWidth / screenWidth
        val factY = surfaceHeight / screenHeight
        val fact = if (factX < factY) factX else factY

        projectionWidth = fact * screenWidth
        projectionHeight = fact * screenHeight

        projectionOffsetX = (surfaceWidth - projectionWidth) / 2.0
        projectionOffsetY = (surfaceHeight - projectionHeight) / 2.0

        if (screenRotation == ROTATION_0 || screenRotation == ROTATION_180) {
            miniTouchSocket.updateTouchTransformations(this.screenWidth, this.screenHeight, deviceDisplaySize)
        } else {
            miniTouchSocket.updateTouchTransformations(this.screenHeight, this.screenWidth, deviceDisplaySize)
        }
    }
}