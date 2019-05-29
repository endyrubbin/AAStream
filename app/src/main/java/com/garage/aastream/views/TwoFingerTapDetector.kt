package com.garage.aastream.views

import android.content.Context
import android.util.ArrayMap
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.ViewConfiguration
import com.garage.aastream.interfaces.OnTwoFingerTapCallback

/**
 * Created by Endy Rubbin on 27.05.2019 13:34.
 * For project: AAStream
 */
class TwoFingerTapDetector(context: Context, callback: OnTwoFingerTapCallback) {

    private var callback: OnTwoFingerTapCallback? = null
    private var eventMap: ArrayMap<Int, PointerCoords> = ArrayMap()
    private var touchSlopSquare: Int = 0

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
                    if (eventMap.size == 2) {
                        callback?.onTwoFingerTapped()
                    }
                    eventMap.clear()
                }
                ACTION_CANCEL -> eventMap.remove(id)
            }
        }
        return false
    }

    private fun getSquaredDistance(p1: PointerCoords, p2: PointerCoords): Double {
        val dx = (p1.x - p2.x).toDouble()
        val dy = (p1.y - p2.y).toDouble()
        return dx * dx + dy * dy
    }
}