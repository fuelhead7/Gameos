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
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x1, y1)
            // Tiny movement helps transition from long press to drag.
            lineTo(x1 + 3f, y1 + 3f)
            lineTo(x2, y2)
        }
        // GestureDescription does not support timed path segments directly.
        // A 1.4-second stroke provides a deliberate, slow drag.
        val stroke = GestureDescription.StrokeDescription(path, 0L, 1400L)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }
}
