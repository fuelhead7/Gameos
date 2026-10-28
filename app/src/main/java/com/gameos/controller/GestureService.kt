package com.gameos.controller

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class GestureService : AccessibilityService() {
    companion object { var current: GestureService? = null }
    override fun onServiceConnected() { super.onServiceConnected(); current = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { if (current === this) current = null; super.onDestroy() }

    fun gesture(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long): Boolean {
        val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    fun dragIcon(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        // First stroke holds the icon without lifting; second continues as a drag.
        val hold = Path().apply { moveTo(x1, y1) }
        val first = GestureDescription.StrokeDescription(hold, 0L, 650L, true)
        val move = Path().apply {
            moveTo(x1, y1)
            lineTo(x1 + 5f, y1 + 5f)
            lineTo(x2, y2)
        }
        val second = first.continueStroke(move, 650L, 850L, false)
        return dispatchGesture(
            GestureDescription.Builder().addStroke(first).addStroke(second).build(),
            null, null
        )
    }
}
