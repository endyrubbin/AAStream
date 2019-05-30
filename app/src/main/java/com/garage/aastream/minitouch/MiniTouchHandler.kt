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
import com.garage.aastream.interfaces.OnTwoFingerTapCallback
import com.garage.aastream.utils.DevLog
import com.garage.aastream.views.TwoFingerTapDetector
import eu.chainfire.libsuperuser.Shell
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Created by Endy Rubbin on 22.05.2019 13:25.
 * For project: AAStream
 */
class MiniTouchHandler(private val context: Context): View.OnTouchListener {

    private var twoFingerTapDetector: TwoFingerTapDetector? = null
    private val miniTouchSocket: MiniTouchSocket = MiniTouchSocket()
    private var surfaceView: SurfaceView? = null

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
    private var isInstalled = false

    /**
     * Reads and updates current screen values
     */
    fun updateValues() {
        val windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealSize(deviceScreenSize)
        deviceScreenRotation = windowManager.defaultDisplay.rotation
        if (deviceScreenRotation == ROTATION_0 || deviceScreenRotation == ROTATION_180) {
            deviceDisplaySize.x = deviceScreenSize.x
            deviceDisplaySize.y = deviceScreenSize.y
        } else {
            deviceDisplaySize.x = deviceScreenSize.y
            deviceDisplaySize.y = deviceScreenSize.x
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun init(surfaceView: SurfaceView, callback: OnTwoFingerTapCallback) {
        DevLog.d("Mini Touch initialized")
        this.surfaceView = surfaceView
        this.surfaceView?.setOnTouchListener(this)
        twoFingerTapDetector = TwoFingerTapDetector(context, callback)
    }

    /**
     * Start mini touch handler
     */
    fun start() {
        val path = install()
        DevLog.d("Mini Touch path: $path")
        if (path?.isNotEmpty() == true) {
            Shell.Pool.SU.run("chmod 755 $path")
            Shell.Pool.SU.run(path)
            isInstalled = true
            DevLog.d("Mini Touch installed: $path")
        }
    }

    /**
     * Stop mini touch handler
     */
    fun stop() {
        val pid = miniTouchSocket.getPid()
        miniTouchSocket.disconnect()
        DevLog.d("Mini Touch stopped: $pid")
        surfaceView = null
        twoFingerTapDetector?.removeCallback()
        twoFingerTapDetector = null
        if (pid != 0) {
            Shell.Pool.SU.run("kill $pid")
        }
    }

    /**
     * Install minitouch library
     */
    private fun install(): String? {
        var fileOutputStream: FileOutputStream? = null
        var assetFile: InputStream? = null
        try {
            val assetName = "libs/" + detectAbi() + "/minitouch"
            fileOutputStream = context.openFileOutput("minitouch", 0)
            assetFile = context.assets.open(assetName)
            assetFile.use {
                it.copyTo(fileOutputStream)
            }
        } catch (e: Exception) {
            DevLog.d("Failed to install Mini Touch: $e")
            return null
        } finally {
            try {
                assetFile?.close()
                fileOutputStream?.close()
            } catch (e: Exception) {
                DevLog.d("Failed to close asset files")
            }
        }

        DevLog.d("Mini Touch installed")
        return context.getFileStreamPath("minitouch").absolutePath
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
                stop()
                start()
            }
            miniTouchSocket.connect(true)
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

        twoFingerTapDetector?.onTouchEvent(event)
        return true
    }

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