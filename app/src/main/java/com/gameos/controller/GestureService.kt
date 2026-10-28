package com.gameos.controller

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class GestureService : AccessibilityService() {
    companion object { var current: GestureService? = null }
    private val handler = Handler(Looper.getMainLooper())
    override fun onServiceConnected() { super.onServiceConnected(); current = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { if (current === this) current = null; super.onDestroy() }

    fun gesture(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long): Boolean {
        val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        return dispatchGesture(GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, duration))
            .build(), null, handler)
    }

    fun dragIcon(x1: Float, y1: Float, x2: Float, y2: Float, report: (String) -> Unit): Boolean {
        // One continuous touch: hold at source for 900ms, then move smoothly over 1.2s.
        // Repeated path points do not guarantee timing; use a continued stroke instead.
        val holdPath = Path().apply { moveTo(x1, y1) }
        val hold = GestureDescription.StrokeDescription(holdPath, 0L, 900L, true)
        val movePath = Path().apply {
            moveTo(x1, y1)
            val steps = 24
            for (i in 1..steps) {
                val t = i.toFloat() / steps
                lineTo(x1 + (x2 - x1) * t, y1 + (y2 - y1) * t)
            }
        }
        val holdAccepted = dispatchGesture(
            GestureDescription.Builder().addStroke(hold).build(),
            object : GestureResultCallback() {
                override fun onCompleted(description: GestureDescription?) {
                    val continuation = hold.continueStroke(movePath, 0L, 1200L, false)
                    val moveAccepted = dispatchGesture(
                        GestureDescription.Builder().addStroke(continuation).build(),
                        object : GestureResultCallback() {
                            override fun onCompleted(description: GestureDescription?) { report("Drag completed") }
                            override fun onCancelled(description: GestureDescription?) { report("Drag cancelled by Android") }
                        }, handler
                    )
                    if (!moveAccepted) report("Drag dispatch rejected")
                }
                override fun onCancelled(description: GestureDescription?) { report("Long-press cancelled") }
            }, handler
        )
        if (!holdAccepted) report("Long-press dispatch rejected")
        return holdAccepted
    }
}
