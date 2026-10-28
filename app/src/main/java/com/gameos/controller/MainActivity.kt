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

class MainActivity : Activity() {
    private lateinit var status: TextView
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
            val dm = resources.displayMetrics
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
        action("Open Samsung Home") {
            val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            startActivity(i)
        }
        root.addView(heading("Only run gesture tests on your own home screen. v0.1 does not move icons or change Good Lock settings.", 15f))
        setContentView(root)
    }
    override fun onResume() { super.onResume(); updateStatus() }
    private fun updateStatus() {
        status.text = "Accessibility: " + if (GestureService.current != null) "Connected" else "Not connected" +
            " | Saved size: ${prefs.getInt("width", 0)} x ${prefs.getInt("height", 0)}"
    }
}
