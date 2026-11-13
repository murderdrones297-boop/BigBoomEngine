package com.bigboom.engine

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Minimal serializable scene model. Renderer state is deliberately kept outside this model. */
data class SceneObject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val scale: Float = 1f,
    val color: FloatArray = floatArrayOf(0.95f, 0.34f, 0.12f, 1f)
)

data class SceneDocument(
    val name: String = "Untitled Scene",
    val objects: MutableList<SceneObject> = mutableListOf(SceneObject(name = "Cube"))
) {
    fun toJson(): String {
        val root = JSONObject().put("format", "BBScene").put("version", 1).put("name", name)
        val array = JSONArray()
        objects.forEach { obj ->
            array.put(JSONObject()
                .put("id", obj.id).put("name", obj.name)
                .put("position", JSONArray().put(obj.x).put(obj.y).put(obj.z))
                .put("scale", obj.scale)
                .put("color", JSONArray().put(obj.color[0]).put(obj.color[1]).put(obj.color[2]).put(obj.color[3])))
        }
        return root.put("objects", array).toString(2)
    }

    companion object {
        fun fromJson(source: String): SceneDocument {
            val root = JSONObject(source)
            require(root.optString("format") == "BBScene") { "Unsupported scene format" }
            require(root.optInt("version") == 1) { "Unsupported BBScene version" }
            val objects = mutableListOf<SceneObject>()
            val array = root.optJSONArray("objects") ?: JSONArray()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val p = item.optJSONArray("position") ?: JSONArray()
                val c = item.optJSONArray("color") ?: JSONArray()
                objects += SceneObject(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    name = item.optString("name", "Object $i"),
                    x = p.optDouble(0, 0.0).toFloat(), y = p.optDouble(1, 0.0).toFloat(), z = p.optDouble(2, 0.0).toFloat(),
                    scale = item.optDouble("scale", 1.0).toFloat().coerceIn(0.1f, 10f),
                    color = floatArrayOf(c.optDouble(0, .95).toFloat(), c.optDouble(1, .34).toFloat(), c.optDouble(2, .12).toFloat(), c.optDouble(3, 1.0).toFloat())
                )
            }
            return SceneDocument(root.optString("name", "Untitled Scene"), objects)
        }
    }
}
