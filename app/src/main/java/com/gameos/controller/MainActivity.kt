package com.gameos.controller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import android.text.InputType
import android.util.DisplayMetrics
import android.view.WindowManager

class MainActivity : Activity() {
    private lateinit var status: TextView
    private var stopLayout = false
    private val prefs by lazy { getSharedPreferences("gameos", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 65, 40, 24)
            setBackgroundColor(Color.rgb(6, 14, 35))
            gravity = Gravity.TOP
        }
        fun heading(value: String, size: Float = 19f): TextView =
            TextView(this).apply { text = value; textSize = size; setTextColor(Color.WHITE); setPadding(0, 8, 0, 20) }
        fun action(label: String, click: () -> Unit) {
            root.addView(Button(this).apply { text = label; setOnClickListener { click() } })
        }
        root.addView(heading("GAMEOS  /  CONTROLLER", 25f))
        root.addView(heading("PS5-inspired Samsung DIY Home calibration"))
        status = heading("")
        root.addView(status)
        action("1. Enable accessibility service") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        action("2. Save screen dimensions") {
            val dm = DisplayMetrics().also { (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(it) }
            prefs.edit().putInt("width", dm.widthPixels).putInt("height", dm.heightPixels).apply()
            Toast.makeText(this, "Saved: ${dm.widthPixels} x ${dm.heightPixels}", Toast.LENGTH_LONG).show()
            updateStatus()
        }
        action("3. Test: tap middle of screen (3-second delay)") {
            val width = prefs.getInt("width", resources.displayMetrics.widthPixels)
            val height = prefs.getInt("height", resources.displayMetrics.heightPixels)
            Toast.makeText(this, "Navigate to a safe blank home-screen area now", Toast.LENGTH_LONG).show()
            android.os.Handler(mainLooper).postDelayed({
                val svc = GestureService.current
                if (svc == null) Toast.makeText(this, "Accessibility service not enabled", Toast.LENGTH_LONG).show()
                else svc.gesture(width / 2f, height / 2f, width / 2f, height / 2f, 100L)
            }, 3000L)
        }
        root.addView(heading("PHASE 2 — ICON DRAG TEST", 19f))
        root.addView(heading("Enter source and target coordinates in screen pixels. Test on a spare icon. A 5-second countdown gives you time to open DIY Home.", 14f))
        fun coordinate(label: String, key: String, fallback: String): EditText {
            val input = EditText(this).apply {
                hint = label
                setText(prefs.getString(key, fallback))
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setTextColor(Color.WHITE)
                setHintTextColor(Color.LTGRAY)
            }
            root.addView(input)
            return input
        }
        val sx = coordinate("Source X (screenshot pixels)", "sx", "242")
        val sy = coordinate("Source Y (screenshot pixels)", "sy", "805")
        val tx = coordinate("Target X (screenshot pixels)", "tx", "242")
        val ty = coordinate("Target Y (screenshot pixels)", "ty", "485")
        action("Save icon movement") {
            prefs.edit().putString("sx", sx.text.toString()).putString("sy", sy.text.toString())
                .putString("tx", tx.text.toString()).putString("ty", ty.text.toString()).apply()
            Toast.makeText(this, "Movement saved", Toast.LENGTH_SHORT).show()
        }
        action("Test saved drag (5-second countdown)") {
            val values = listOf("sx", "sy", "tx", "ty").map { prefs.getString(it, "")?.toFloatOrNull() }
            if (values.any { it == null }) {
                Toast.makeText(this, "Save valid coordinates first", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Open DIY Home now — drag begins in 5 seconds", Toast.LENGTH_LONG).show()
                android.os.Handler(mainLooper).postDelayed({
                    val service = GestureService.current
                    if (service == null) {
                        Toast.makeText(this, "Accessibility not connected", Toast.LENGTH_LONG).show()
                    } else {
                        val dm = DisplayMetrics().also {
                            (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(it)
                        }
                        val scaleX = dm.widthPixels / 709f
                        val scaleY = dm.heightPixels / 1536f
                        service.dragIcon(
                            values[0]!! * scaleX, values[1]!! * scaleY,
                            values[2]!! * scaleX, values[3]!! * scaleY
                        ) { message ->
                            runOnUiThread {
                                prefs.edit().putString("last_result", message).apply()
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                                updateStatus()
                            }
                        }
                    }
                }, 5000L)
            }
        }
        root.addView(heading("PHASE 3 — AUTOMATIC PS5 ROW", 19f))
        root.addView(heading("Based on your latest 709 x 1536 screenshot. Moves YouTube, Play Store, Gmail and Photos into a horizontal row. Keep the screen unlocked and unchanged during the countdown.", 14f))
        action("Arrange 4 icons automatically (8-second countdown)") {
            if (GestureService.current == null) {
                Toast.makeText(this, "Enable accessibility first", Toast.LENGTH_LONG).show()
            } else {
                stopLayout = false
                prefs.edit().putString("last_result", "Auto layout scheduled").apply()
                updateStatus()
                val handler = android.os.Handler(mainLooper)
                val moves = listOf(
                    floatArrayOf(232f, 500f, 105f, 620f),  // YouTube
                    floatArrayOf(106f, 875f, 270f, 620f),  // Play Store
                    floatArrayOf(413f, 765f, 435f, 620f),  // Gmail
                    floatArrayOf(620f, 710f, 600f, 620f)   // Photos
                )
                val names = listOf("YouTube", "Play Store", "Gmail", "Photos")
                fun log(message: String) {
                    prefs.edit().putString("last_result", message).apply()
                    updateStatus()
                }
                fun runMove(index: Int) {
                    if (stopLayout) { log("Auto layout stopped"); return }
                    if (index >= moves.size) { log("Auto layout completed: 4 drags dispatched"); return }
                    val service = GestureService.current
                    if (service == null) { log("Accessibility disconnected"); return }
                    val dm = DisplayMetrics().also {
                        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(it)
                    }
                    val v = moves[index]
                    log("Moving ${names[index]} (${index + 1}/4)")
                    service.dragIcon(
                        v[0] * dm.widthPixels / 709f, v[1] * dm.heightPixels / 1536f,
                        v[2] * dm.widthPixels / 709f, v[3] * dm.heightPixels / 1536f
                    ) { result ->
                        runOnUiThread {
                            if (result != "Drag completed") {
                                log("${names[index]}: $result; sequence stopped")
                                stopLayout = true
                            } else if (!stopLayout) {
                                handler.postDelayed({ runMove(index + 1) }, 1400L)
                            }
                        }
                    }
                }
                Toast.makeText(this, "Return to your unchanged home screen now — starts in 8 seconds", Toast.LENGTH_LONG).show()
                handler.postDelayed({ runMove(0) }, 8000L)
            }
        }
        action("STOP automatic layout") {
            stopLayout = true
            prefs.edit().putString("last_result", "Stop requested; current drag may finish").apply()
            updateStatus()
        }
        action("Open Samsung Home") {
            val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            startActivity(i)
        }
        root.addView(heading("Only run gesture tests on your own home screen. Drag tests may rearrange icons.", 15f))
        setContentView(root)
    }
    override fun onResume() { super.onResume(); updateStatus() }
    private fun updateStatus() {
        val connection = if (GestureService.current != null) "Connected" else "Not connected"
        status.text = "Accessibility: $connection | Saved size: ${prefs.getInt("width", 0)} x ${prefs.getInt("height", 0)} | Last drag: ${prefs.getString("last_result", "Not tested")}"
    }
}
