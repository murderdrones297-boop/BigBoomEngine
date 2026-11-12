package com.bigboom.engine

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.io.File

class EditorActivity : Activity() {
    private lateinit var rendererView: EngineSurfaceView
    private lateinit var status: TextView
    private var scene = SceneDocument()
    private var selectedIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 19, 24)
        window.navigationBarColor = Color.rgb(16, 19, 24)
        buildEditor()
    }

    private fun buildEditor() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(16, 19, 24)) }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6)); setBackgroundColor(Color.rgb(25, 30, 38))
        }
        val brand = TextView(this).apply { text = "BIGBOOM ENGINE 0.1"; setTextColor(Color.WHITE); textSize = 14f; setPadding(dp(6), 0, dp(10), 0) }
        toolbar.addView(brand, LinearLayout.LayoutParams(0, dp(44), 1f))
        toolbar.addView(toolButton("+ CUBE") { addCube() })
        toolbar.addView(toolButton("SAVE") { saveScene() })
        toolbar.addView(toolButton("LOAD") { loadScene() })
        root.addView(toolbar)

        rendererView = EngineSurfaceView(this).apply {
            onTap = { selectNextObject() }
            onCameraChanged = { yaw, pitch -> status.text = "Objects: ${scene.objects.size}   Camera: ${yaw.toInt()}° / ${pitch.toInt()}°   Tap viewport to select" }
        }
        root.addView(rendererView, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(7), dp(10), dp(7)); setBackgroundColor(Color.rgb(25, 30, 38))
        }
        status = TextView(this).apply { text = "Objects: 1   Selected: Cube"; setTextColor(Color.rgb(202, 211, 222)); textSize = 12f }
        bottom.addView(status, LinearLayout.LayoutParams(0, dp(38), 1f))
        bottom.addView(toolButton("DELETE") { deleteSelected() })
        bottom.addView(toolButton("RESET") { resetCamera() })
        root.addView(bottom)
        setContentView(root)
        rendererView.setScene(scene)
    }

    private fun toolButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label; textSize = 10f; isAllCaps = false; setTextColor(Color.WHITE)
        setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(57, 67, 82)))
        setPadding(dp(7), 0, dp(7), 0); minWidth = dp(58); minimumWidth = dp(58)
        setOnClickListener { action() }
    }

    private fun addCube() {
        val n = scene.objects.size + 1
        scene.objects += SceneObject(name = "Cube_$n", x = ((n - 1) % 3 - 1) * 1.8f, z = -((n - 1) / 3) * 1.8f,
            color = floatArrayOf(.25f + (n % 3) * .2f, .48f, .9f - (n % 2) * .25f, 1f))
        selectedIndex = scene.objects.lastIndex
        refreshScene("Created ${scene.objects[selectedIndex].name}")
    }

    private fun selectNextObject() {
        if (scene.objects.isNotEmpty()) selectedIndex = (selectedIndex + 1) % scene.objects.size
        refreshScene("Selected: ${scene.objects.getOrNull(selectedIndex)?.name ?: "None"}")
    }

    private fun deleteSelected() {
        if (scene.objects.isEmpty()) return
        val removed = scene.objects.removeAt(selectedIndex.coerceIn(scene.objects.indices))
        selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
        refreshScene("Deleted ${removed.name}")
    }

    private fun refreshScene(message: String) {
        rendererView.setScene(scene)
        status.text = "Objects: ${scene.objects.size}   Selected: ${scene.objects.getOrNull(selectedIndex)?.name ?: "None"}"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveScene() {
        runCatching {
            File(filesDir, "Scenes").apply { mkdirs() }
            File(filesDir, "Scenes/Main.bbscene").writeText(scene.toJson())
        }.onSuccess { Toast.makeText(this, "Saved: Scenes/Main.bbscene", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(this, "Save failed: ${it.message}", Toast.LENGTH_LONG).show() }
    }

    private fun loadScene() {
        val file = File(filesDir, "Scenes/Main.bbscene")
        if (!file.exists()) { Toast.makeText(this, "No saved scene yet", Toast.LENGTH_SHORT).show(); return }
        runCatching { SceneDocument.fromJson(file.readText()) }
            .onSuccess { scene = it; selectedIndex = 0; rendererView.setScene(scene); refreshScene("Scene loaded") }
            .onFailure { Toast.makeText(this, "Load failed: ${it.message}", Toast.LENGTH_LONG).show() }
    }

    private fun resetCamera() { rendererView.resetCamera() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
