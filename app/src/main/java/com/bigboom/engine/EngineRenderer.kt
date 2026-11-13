package com.bigboom.engine

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/** OpenGL ES 3 renderer: depth-tested grid, lit cube meshes and orbit camera. */
class EngineRenderer : GLSurfaceView.Renderer {
    @Volatile private var objects: List<SceneObject> = emptyList()
    @Volatile var yawDegrees: Float = 35f; private set
    @Volatile var pitchDegrees: Float = 24f; private set
    private var program = 0
    private var mvpHandle = 0
    private var modelHandle = 0
    private var colorHandle = 0
    private var lightHandle = 0
    private var cubeBuffer = 0
    private var gridBuffer = 0
    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val mvp = FloatArray(16)
    private val identity = FloatArray(16)
    private val light = floatArrayOf(-0.4f, 0.8f, 0.5f)

    fun setScene(scene: SceneDocument) { objects = scene.objects.toList() }
    fun orbit(dx: Float, dy: Float) {
        yawDegrees = (yawDegrees + dx) % 360f
        pitchDegrees = (pitchDegrees + dy).coerceIn(-10f, 78f)
    }
    fun resetCamera() { yawDegrees = 35f; pitchDegrees = 24f }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(.055f, .067f, .085f, 1f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)
        program = createProgram(VERTEX_SHADER.trimIndent(), FRAGMENT_SHADER.trimIndent())
        mvpHandle = GLES30.glGetUniformLocation(program, "uMvp")
        modelHandle = GLES30.glGetUniformLocation(program, "uModel")
        colorHandle = GLES30.glGetUniformLocation(program, "uColor")
        lightHandle = GLES30.glGetUniformLocation(program, "uLight")
        cubeBuffer = createBuffer(cubeVertices())
        gridBuffer = createBuffer(gridVertices())
        Matrix.setIdentityM(identity, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.coerceAtLeast(1)
        Matrix.perspectiveM(projection, 0, 55f, ratio, .1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val yaw = Math.toRadians(yawDegrees.toDouble()).toFloat()
        val pitch = Math.toRadians(pitchDegrees.toDouble()).toFloat()
        val radius = 9f
        val eyeX = cos(yaw) * cos(pitch) * radius
        val eyeY = sin(pitch) * radius + 1.2f
        val eyeZ = sin(yaw) * cos(pitch) * radius
        Matrix.setLookAtM(view, 0, eyeX, eyeY, eyeZ, 0f, 0f, 0f, 0f, 1f, 0f)
        GLES30.glUseProgram(program)
        GLES30.glUniform3fv(lightHandle, 1, light, 0)
        drawGrid()
        objects.forEach { obj ->
            Matrix.setIdentityM(model, 0)
            Matrix.translateM(model, 0, obj.x, obj.y, obj.z)
            Matrix.scaleM(model, 0, obj.scale, obj.scale, obj.scale)
            Matrix.multiplyMM(mvp, 0, view, 0, model, 0)
            Matrix.multiplyMM(mvp, 0, projection, 0, mvp, 0)
            GLES30.glUniformMatrix4fv(modelHandle, 1, false, model, 0)
            GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)
            GLES30.glUniform4fv(colorHandle, 1, obj.color, 0)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, cubeBuffer)
            GLES30.glEnableVertexAttribArray(0)
            GLES30.glEnableVertexAttribArray(1)
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * 4, 0)
            GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 6 * 4, 3 * 4)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
            GLES30.glDisableVertexAttribArray(0); GLES30.glDisableVertexAttribArray(1)
        }
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }

    private fun drawGrid() {
        Matrix.setIdentityM(model, 0)
        Matrix.multiplyMM(mvp, 0, view, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, projection, 0, mvp, 0)
        GLES30.glUniformMatrix4fv(modelHandle, 1, false, model, 0)
        GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)
        GLES30.glUniform4f(colorHandle, .27f, .32f, .38f, 1f)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, gridBuffer)
        GLES30.glEnableVertexAttribArray(0); GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * 4, 0)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 6 * 4, 3 * 4)
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, GRID_VERTEX_COUNT)
        GLES30.glDisableVertexAttribArray(0); GLES30.glDisableVertexAttribArray(1)
    }

    private fun createBuffer(data: FloatArray): Int {
        val ids = IntArray(1); GLES30.glGenBuffers(1, ids, 0)
        val buffer = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(data).position(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, ids[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.size * 4, buffer, GLES30.GL_STATIC_DRAW)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        return ids[0]
    }

    private fun createProgram(vertex: String, fragment: String): Int {
        fun compile(type: Int, source: String): Int {
            val shader = GLES30.glCreateShader(type)
            GLES30.glShaderSource(shader, source); GLES30.glCompileShader(shader)
            val status = IntArray(1); GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
            if (status[0] == 0) { val log = GLES30.glGetShaderInfoLog(shader); GLES30.glDeleteShader(shader); error("GLSL compile failed: $log") }
            return shader
        }
        val vs = compile(GLES30.GL_VERTEX_SHADER, vertex); val fs = compile(GLES30.GL_FRAGMENT_SHADER, fragment)
        val p = GLES30.glCreateProgram(); GLES30.glAttachShader(p, vs); GLES30.glAttachShader(p, fs); GLES30.glLinkProgram(p)
        val status = IntArray(1); GLES30.glGetProgramiv(p, GLES30.GL_LINK_STATUS, status, 0)
        GLES30.glDeleteShader(vs); GLES30.glDeleteShader(fs)
        if (status[0] == 0) { val log = GLES30.glGetProgramInfoLog(p); GLES30.glDeleteProgram(p); error("GLSL link failed: $log") }
        return p
    }

    private fun gridVertices(): FloatArray {
        val out = ArrayList<Float>()
        for (i in -10..10) {
            addLine(out, i.toFloat(), 0f, -10f, i.toFloat(), 0f, 10f)
            addLine(out, -10f, 0f, i.toFloat(), 10f, 0f, i.toFloat())
        }
        return out.toFloatArray()
    }
    private fun addLine(out: MutableList<Float>, ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float) {
        listOf(ax, ay, az, 0f, 1f, 0f, bx, by, bz, 0f, 1f, 0f).forEach(out::add)
    }

    private fun cubeVertices(): FloatArray {
        val out = ArrayList<Float>(36 * 6)
        fun face(a: FloatArray, b: FloatArray, c: FloatArray, d: FloatArray, n: FloatArray) {
            listOf(a,b,c,a,c,d).forEach { v -> out.add(v[0]); out.add(v[1]); out.add(v[2]); out.add(n[0]); out.add(n[1]); out.add(n[2]) }
        }
        val p = .5f
        face(floatArrayOf(-p,-p,p), floatArrayOf(p,-p,p), floatArrayOf(p,p,p), floatArrayOf(-p,p,p), floatArrayOf(0f,0f,1f))
        face(floatArrayOf(p,-p,-p), floatArrayOf(-p,-p,-p), floatArrayOf(-p,p,-p), floatArrayOf(p,p,-p), floatArrayOf(0f,0f,-1f))
        face(floatArrayOf(-p,-p,-p), floatArrayOf(-p,-p,p), floatArrayOf(-p,p,p), floatArrayOf(-p,p,-p), floatArrayOf(-1f,0f,0f))
        face(floatArrayOf(p,-p,p), floatArrayOf(p,-p,-p), floatArrayOf(p,p,-p), floatArrayOf(p,p,p), floatArrayOf(1f,0f,0f))
        face(floatArrayOf(-p,p,p), floatArrayOf(p,p,p), floatArrayOf(p,p,-p), floatArrayOf(-p,p,-p), floatArrayOf(0f,1f,0f))
        face(floatArrayOf(-p,-p,-p), floatArrayOf(p,-p,-p), floatArrayOf(p,-p,p), floatArrayOf(-p,-p,p), floatArrayOf(0f,-1f,0f))
        return out.toFloatArray()
    }

    companion object {
        private const val GRID_VERTEX_COUNT = 21 * 4
        private const val VERTEX_SHADER = """
            #version 300 es
            layout(location=0) in vec3 aPosition;
            layout(location=1) in vec3 aNormal;
            uniform mat4 uMvp; uniform mat4 uModel;
            out vec3 vNormal;
            void main(){ gl_Position=uMvp*vec4(aPosition,1.0); vNormal=mat3(uModel)*aNormal; }
        """
        private const val FRAGMENT_SHADER = """
            #version 300 es
            precision mediump float;
            in vec3 vNormal; uniform vec4 uColor; uniform vec3 uLight;
            out vec4 fragColor;
            void main(){ float diffuse=max(dot(normalize(vNormal),normalize(uLight)),0.0); float lighting=0.28+0.72*diffuse; fragColor=vec4(uColor.rgb*lighting,uColor.a); }
        """
    }
}
