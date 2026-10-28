package com.gameos.minimalhome

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.drawable.GradientDrawable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val background = Color.rgb(17, 18, 20)
    private val foreground = Color.rgb(242, 242, 242)
    private val muted = Color.rgb(150, 154, 160)
    private val surface = Color.rgb(34, 36, 39)
    private lateinit var root: LinearLayout
    private val prefs by lazy { getSharedPreferences("minimal_home", MODE_PRIVATE) }
    private var drawer = false
    private val apps by lazy {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName + "/" + it.activityInfo.name }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = background
        window.decorView.systemUiVisibility = 0
        showHome()
    }

    private fun dp(n: Int): Int = (resources.displayMetrics.density * n).toInt()
    private fun rounded(color: Int, radius: Int = 18): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun label(text: String, size: Float, color: Int = foreground, bold: Boolean = false) =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
    private fun column(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(background)
        setPadding(dp(24), dp(24), dp(24), dp(20))
    }
    private fun addSpace(parent: LinearLayout, weight: Float = 1f) =
        parent.addView(View(this), LinearLayout.LayoutParams(1, 0, weight))
    private fun init(title: String) {
        root = column()
        setContentView(root)
    }
    private fun savedPins(): MutableSet<String> =
        prefs.getStringSet("pins", emptySet())!!.toMutableSet()
    private fun key(item: ResolveInfo) = item.activityInfo.packageName + "/" + item.activityInfo.name
    private fun start(item: ResolveInfo) {
        try {
            startActivity(Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(ComponentName(item.activityInfo.packageName, item.activityInfo.name))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show()
        }
    }
    private fun appTile(item: ResolveInfo, compact: Boolean = false): LinearLayout {
        val tile = LinearLayout(this).apply {
            orientation = if (compact) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(12))
            background = rounded(surface, 16)
        }
        val icon = ImageView(this).apply {
            setImageDrawable(item.loadIcon(packageManager))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val iconParams = LinearLayout.LayoutParams(dp(if (compact) 43 else 32), dp(if (compact) 43 else 32))
        if (!compact) iconParams.marginEnd = dp(16)
        tile.addView(icon, iconParams)
        val title = label(item.loadLabel(packageManager).toString(), if (compact) 11f else 15f)
        title.maxLines = 1
        title.ellipsize = android.text.TextUtils.TruncateAt.END
        if (compact) {
            title.gravity = Gravity.CENTER
            val p = LinearLayout.LayoutParams(-1, dp(26))
            p.topMargin = dp(8)
            tile.addView(title, p)
        } else tile.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        tile.setOnClickListener { start(item) }
        tile.setOnLongClickListener {
            val pins = savedPins()
            val k = key(item)
            if (pins.contains(k)) pins.remove(k) else pins.add(k)
            prefs.edit().putStringSet("pins", pins).apply()
            Toast.makeText(this, if (pins.contains(k)) "Added to favorites" else "Removed from favorites", Toast.LENGTH_SHORT).show()
            if (drawer) showDrawer() else showHome()
            true
        }
        return tile
    }
    private fun showHome() {
        drawer = false
        init("Home")
        root.addView(label(SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault()), 12f, muted))
        root.addView(label(SimpleDateFormat("h:mm", Locale.getDefault()).format(Date()), 66f, foreground, true))
        root.addView(label("Your space. Just what matters.", 14f, muted))
        addSpace(root, 2f)
        root.addView(label("FAVORITES", 12f, muted, true))
        val pinned = savedPins()
        val favorites = apps.filter { key(it) in pinned }.take(8).ifEmpty { apps.take(4) }
        val grid = GridLayout(this).apply { columnCount = 4 }
        favorites.forEach { app ->
            val tile = appTile(app, true)
            val p = GridLayout.LayoutParams().apply {
                width = (resources.displayMetrics.widthPixels - dp(48 + 24)) / 4
                height = dp(106)
                setMargins(dp(3), dp(5), dp(3), dp(5))
            }
            grid.addView(tile, p)
        }
        root.addView(grid)
        addSpace(root, 1f)
        val all = label("Apps   ·   Search all", 17f, foreground, true).apply {
            background = rounded(surface)
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(18), dp(12), dp(18))
            setOnClickListener { showDrawer() }
        }
        root.addView(all)
    }
    private fun showDrawer() {
        drawer = true
        init("Apps")
        val back = label("‹   Home", 16f, muted).apply {
            setPadding(0, 0, 0, dp(14))
            setOnClickListener { showHome() }
        }
        root.addView(back)
        root.addView(label("Apps", 30f, foreground, true))
        root.addView(label("Long-press to add or remove favorites", 12f, muted))
        val query = EditText(this).apply {
            hint = "Search apps"
            setHintTextColor(muted)
            setTextColor(foreground)
            background = rounded(surface, 14)
            singleLine = true
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        val qp = LinearLayout.LayoutParams(-1, dp(54)).apply {
            topMargin = dp(20); bottomMargin = dp(10)
        }
        root.addView(query, qp)
        val scroller = ScrollView(this).apply { fillViewport = true }
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroller.addView(list)
        root.addView(scroller, LinearLayout.LayoutParams(-1, 0, 1f))
        fun render(filter: String) {
            list.removeAllViews()
            apps.filter { it.loadLabel(packageManager).toString().contains(filter, true) }
                .forEach { item ->
                    val p = LinearLayout.LayoutParams(-1, dp(64)).apply { bottomMargin = dp(6) }
                    list.addView(appTile(item), p)
                }
        }
        render("")
        query.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (drawer) showHome() else super.onBackPressed() }
    override fun onResume() {
        super.onResume()
        if (!drawer && ::root.isInitialized) showHome()
    }
}
