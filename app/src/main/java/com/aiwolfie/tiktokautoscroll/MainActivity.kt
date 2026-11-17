package com.aiwolfie.tiktokautoscroll

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences(PREFS, MODE_PRIVATE) }
    private lateinit var mode: RadioGroup
    private lateinit var interval: EditText
    private lateinit var minDelay: EditText
    private lateinit var maxDelay: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
        }
        page.addView(box)
        box.addView(TextView(this).apply {
            text = "TikTok AutoScroll"
            textSize = 27f
            gravity = Gravity.CENTER_VERTICAL
        })
        box.addView(TextView(this).apply {
            text = "Dua mode otomatisasi scroll untuk penggunaan pribadi"
            textSize = 14f
            setPadding(0, 5, 0, 18)
        })

        box.addView(label("Pilih mode"))
        mode = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        mode.addView(RadioButton(this).apply { id = MODE_TIMER; text = "Opsi 1 — Timer Scroll" })
        mode.addView(RadioButton(this).apply { id = MODE_DETECT; text = "Opsi 2 — Auto Detect (best effort)" })
        box.addView(mode)

        box.addView(label("Interval timer (detik)"))
        interval = numberField("30"); box.addView(interval)
        box.addView(label("Jeda minimum sebelum swipe (ms)"))
        minDelay = numberField("500"); box.addView(minDelay)
        box.addView(label("Jeda maksimum sebelum swipe (ms)"))
        maxDelay = numberField("1500"); box.addView(maxDelay)

        box.addView(TextView(this).apply {
            text = "Swipe berlangsung sekitar 250 ms. Jeda dipilih acak antara batas minimum dan maksimum."
            textSize = 12f; setPadding(0, 8, 0, 12)
        })
        box.addView(Button(this).apply {
            text = "1. Aktifkan Accessibility Service"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        box.addView(Button(this).apply {
            text = "2. Simpan Pengaturan"
            setOnClickListener { save() }
        })
        box.addView(Button(this).apply {
            text = "START"
            setOnClickListener {
                save()
                prefs.edit().putBoolean(KEY_ENABLED, true).apply()
                status.text = "Status: AKTIF — buka TikTok. Tekan STOP untuk berhenti."
            }
        })
        box.addView(Button(this).apply {
            text = "STOP"
            setOnClickListener {
                prefs.edit().putBoolean(KEY_ENABLED, false).apply()
                status.text = "Status: berhenti"
            }
        })
        status = TextView(this).apply { textSize = 16f; setPadding(0, 14, 0, 12) }
        box.addView(status)
        box.addView(TextView(this).apply {
            text = "Auto Detect bergantung pada label aksesibilitas seperti Replay/Watch again. TikTok tidak selalu mengekspos indikator akhir video; gunakan Timer jika deteksi tidak bekerja."
            textSize = 12f; setPadding(0, 8, 0, 12)
        })
        box.addView(TextView(this).apply {
            text = "Privasi: Accessibility Service dapat membaca struktur tampilan aplikasi aktif dan mengirim swipe. Aktifkan hanya saat diperlukan. Aplikasi tidak membaca password, pesan, atau mengirim data ke server."
            textSize = 12f
        })
        setContentView(page)
        load()
    }

    private fun label(s: String) = TextView(this).apply { text = s; textSize = 14f; setPadding(0, 12, 0, 4) }
    private fun numberField(v: String) = EditText(this).apply {
        setSingleLine(true); inputType = InputType.TYPE_CLASS_NUMBER; setText(v)
    }
    private fun load() {
        mode.check(if (prefs.getString(KEY_MODE, TIMER) == DETECT) MODE_DETECT else MODE_TIMER)
        interval.setText(prefs.getInt(KEY_INTERVAL, 30).toString())
        minDelay.setText(prefs.getInt(KEY_MIN, 500).toString())
        maxDelay.setText(prefs.getInt(KEY_MAX, 1500).toString())
        status.text = if (prefs.getBoolean(KEY_ENABLED, false)) "Status: AKTIF" else "Status: berhenti"
    }
    private fun save() {
        val seconds = interval.text.toString().toIntOrNull()?.coerceIn(5, 3600) ?: 30
        val min = minDelay.text.toString().toIntOrNull()?.coerceIn(100, 10000) ?: 500
        val max = maxOf(min, maxDelay.text.toString().toIntOrNull()?.coerceIn(100, 10000) ?: 1500)
        prefs.edit()
            .putString(KEY_MODE, if (mode.checkedRadioButtonId == MODE_DETECT) DETECT else TIMER)
            .putInt(KEY_INTERVAL, seconds).putInt(KEY_MIN, min).putInt(KEY_MAX, max).apply()
        minDelay.setText(min.toString()); maxDelay.setText(max.toString())
        Toast.makeText(this, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val PREFS = "autoscroll_settings"
        const val KEY_ENABLED = "enabled"
        const val KEY_MODE = "mode"
        const val KEY_INTERVAL = "interval_seconds"
        const val KEY_MIN = "min_delay_ms"
        const val KEY_MAX = "max_delay_ms"
        const val TIMER = "timer"
        const val DETECT = "detect"
        const val MODE_TIMER = 1001
        const val MODE_DETECT = 1002
    }
}
