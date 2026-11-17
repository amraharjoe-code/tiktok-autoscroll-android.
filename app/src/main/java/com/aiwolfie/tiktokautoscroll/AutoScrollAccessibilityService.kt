package com.aiwolfie.tiktokautoscroll

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import kotlin.random.Random

class AutoScrollAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE) }
    private var lastSwipe = 0L
    private var pending = false

    private val timer = object : Runnable {
        override fun run() {
            if (prefs.getBoolean(MainActivity.KEY_ENABLED, false) &&
                prefs.getString(MainActivity.KEY_MODE, MainActivity.TIMER) == MainActivity.TIMER) {
                val intervalMs = prefs.getInt(MainActivity.KEY_INTERVAL, 30).coerceIn(5, 3600) * 1000L
                val now = System.currentTimeMillis()
                if (lastSwipe == 0L) lastSwipe = now
                if (now - lastSwipe >= intervalMs) queueSwipe()
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        lastSwipe = System.currentTimeMillis()
        handler.post(timer)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !prefs.getBoolean(MainActivity.KEY_ENABLED, false)) return
        if (prefs.getString(MainActivity.KEY_MODE, MainActivity.TIMER) != MainActivity.DETECT) return

        val packageName = event.packageName?.toString().orEmpty()
        val allowed = packageName == "com.zhiliaoapp.musically" ||
            packageName == "com.ss.android.ugc.trill" ||
            packageName == "com.zhiliaoapp.musically.go"
        if (!allowed) return

        val observed = buildString {
            event.text.forEach { append(it).append(' ') }
            event.contentDescription?.let { append(it).append(' ') }
            try {
                event.source?.let { node ->
                    append(node.text ?: "").append(' ')
                    append(node.contentDescription ?: "")
                    node.recycle()
                }
            } catch (_: Exception) { }
        }.lowercase()

        val endHints = listOf("watch again", "replay", "video ended", "putar lagi", "tonton lagi", "video selesai")
        if (endHints.any { observed.contains(it) }) queueSwipe()
    }

    private fun queueSwipe() {
        if (pending || !prefs.getBoolean(MainActivity.KEY_ENABLED, false)) return
        if (System.currentTimeMillis() - lastSwipe < 2500L) return
        pending = true
        val low = prefs.getInt(MainActivity.KEY_MIN, 500).coerceIn(100, 10000)
        val high = prefs.getInt(MainActivity.KEY_MAX, 1500).coerceIn(low, 10000)
        handler.postDelayed({
            if (prefs.getBoolean(MainActivity.KEY_ENABLED, false)) swipe()
            pending = false
        }, Random.nextLong(low.toLong(), high.toLong() + 1L))
    }

    private fun swipe() {
        val dm = resources.displayMetrics
        val x = dm.widthPixels * 0.5f
        val path = Path().apply {
            moveTo(x, dm.heightPixels * 0.82f)
            lineTo(x, dm.heightPixels * 0.25f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 250))
            .build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                lastSwipe = System.currentTimeMillis()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                lastSwipe = System.currentTimeMillis()
            }
        }, null)
    }

    override fun onInterrupt() {
        prefs.edit().putBoolean(MainActivity.KEY_ENABLED, false).apply()
        handler.removeCallbacks(timer)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
