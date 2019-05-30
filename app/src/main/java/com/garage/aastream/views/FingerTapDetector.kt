package com.garage.aastream.views

import android.content.Context
import android.util.ArrayMap
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.ViewConfiguration
import com.garage.aastream.handlers.PreferenceHandler
import com.garage.aastream.interfaces.OnMenuTapCallback
import com.garage.aastream.utils.Const

/**
 * Created by Endy Rubbin on 27.05.2019 13:34.
 * For project: AAStream
 */
class FingerTapDetector(
    context: Context,
    preferenceHandler: PreferenceHandler,
    callback: OnMenuTapCallback
) {

    private var callback: OnMenuTapCallback? = null
    private var eventMap: ArrayMap<Int, PointerCoords> = ArrayMap()
    private var touchSlopSquare: Int = 0
    private var previousTime: Long = 0
    private var tapCount = 0
    private val openMethod = preferenceHandler.getInt(PreferenceHandler.KEY_OPEN_MENU_METHOD, MenuOpenMethod.TWO_FINGER_TAP.value)
    private val minTapCount = when (openMethod) {
        MenuOpenMethod.DOUBLE_TAP.value -> 2
        MenuOpenMethod.TRIPLE_TAP.value -> 3
        else -> 0
    }

    init {
        this.callback = callback
        val configuration = ViewConfiguration.get(context)
        val touchSlop = configuration.scaledTouchSlop
        touchSlopSquare = touchSlop * touchSlop
    }

    fun removeCallback() {
        this.callback = null
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        for (i in 0 until event.pointerCount) {
            val id = event.getPointerId(i)
            val coords = PointerCoords()

            when (action) {
                ACTION_DOWN, ACTION_POINTER_DOWN -> {
                    event.getPointerCoords(i, coords)
                    eventMap[id] = coords
                }
                ACTION_MOVE -> if (eventMap.containsKey(id)) {
                    event.getPointerCoords(i, coords)
                    eventMap[id]?.let {
                        val dist = getSquaredDistance(it, coords)
                        if (dist > touchSlopSquare) {
                            eventMap.remove(id)
                        }
                    }

                }
                ACTION_UP -> {
                    if (openMethod == MenuOpenMethod.TWO_FINGER_TAP.value && eventMap.size == 2) {
                        callback?.onTapForMenu()
                    }
                    eventMap.clear()
                }
                ACTION_CANCEL -> eventMap.remove(id)
            }
        }

        if (event.action == ACTION_UP && minTapCount > 0) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - previousTime <= Const.CLICK_INTERVAL) {
                tapCount++
            } else {
                tapCount = 0
            }
            previousTime = currentTime
            if (tapCount >= minTapCount) {
                tapCount = 0
                callback?.onTapForMenu()
            }
        }
        return false
    }

    /**
     * Calculate distance between taps
     */
    private fun getSquaredDistance(p1: PointerCoords, p2: PointerCoords): Double {
        val dx = (p1.x - p2.x).toDouble()
        val dy = (p1.y - p2.y).toDouble()
        return dx * dx + dy * dy
    }

    companion object {
        enum class MenuOpenMethod(val value: Int) {
            TWO_FINGER_TAP(0),
            DOUBLE_TAP(1),
            TRIPLE_TAP(2)
        }
    }
}