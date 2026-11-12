package com.bigboom.engine

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import kotlin.math.abs

class EngineSurfaceView(context: Context) : GLSurfaceView(context) {
    private val engineRenderer = EngineRenderer()
    var onTap: (() -> Unit)? = null
    var onCameraChanged: ((Float, Float) -> Unit)? = null
    private var downX = 0f
    private var downY = 0f
    private var dragged = false

    init {
        setEGLContextClientVersion(3)
        setRenderer(engineRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        preserveEGLContextOnPause = true
    }

    fun setScene(scene: SceneDocument) { engineRenderer.setScene(scene) }
    fun resetCamera() { queueEvent { engineRenderer.resetCamera() } }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { downX = event.x; downY = event.y; dragged = false; return true }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - downX; val dy = event.y - downY
                if (abs(dx) + abs(dy) > 8f) dragged = true
                if (dragged) {
                    queueEvent { engineRenderer.orbit(dx * .35f, dy * .25f) }
                    downX = event.x; downY = event.y
                    post { onCameraChanged?.invoke(engineRenderer.yawDegrees, engineRenderer.pitchDegrees) }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!dragged) onTap?.invoke()
                performClick(); return true
            }
            MotionEvent.ACTION_CANCEL -> return true
        }
        return true
    }

    override fun performClick(): Boolean { super.performClick(); return true }
}
