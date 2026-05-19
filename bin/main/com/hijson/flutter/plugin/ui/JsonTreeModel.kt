package com.hijson.flutter.plugin.ui

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

sealed class JsonNode(val text: String) {
    override fun toString(): String = text

    class ObjectNode(key: String?, val obj: JsonObject) : JsonNode(
        key?.let { "\"$it\": { }" } ?: "{ }"
    )

    class ArrayNode(key: String?, val arr: JsonArray) : JsonNode(
        key?.let { "\"$it\": [ ]" } ?: "[ ]"
    )

    class ValueNode(key: String?, val value: String) : JsonNode(
        key?.let { "\"$it\": $value" } ?: value
    )
}

class JsonTreeModel(jsonString: String) : TreeModel {

    private val root: JsonNode = try {
        val element = JsonParser.parseString(jsonString.trim())
        buildNode(null, element)
    } catch (e: Exception) {
        JsonNode.ValueNode(null, "Invalid JSON")
    }

    private fun buildNode(key: String?, element: JsonElement): JsonNode = when {
        element.isJsonObject -> {
            JsonNode.ObjectNode(key, element.asJsonObject)
        }
        element.isJsonArray -> {
            JsonNode.ArrayNode(key, element.asJsonArray)
        }
        element.isJsonNull -> JsonNode.ValueNode(key, "null")
        else -> {
            val p = element.asJsonPrimitive
            val value = when {
                p.isString -> "\"${p.asString}\""
                p.isBoolean -> p.asBoolean.toString()
                else -> p.asString
            }
            JsonNode.ValueNode(key, value)
        }
    }

    override fun getRoot(): Any = root

    override fun getChild(parent: Any, index: Int): Any {
        return when (parent) {
            is JsonNode.ObjectNode -> {
                val entry = parent.obj.entrySet().toList()[index]
                buildNode(entry.key, entry.value)
            }
            is JsonNode.ArrayNode -> {
                buildNode(null, parent.arr[index])
            }
            else -> throw IndexOutOfBoundsException()
        }
    }

    override fun getChildCount(parent: Any): Int = when (parent) {
        is JsonNode.ObjectNode -> parent.obj.size()
        is JsonNode.ArrayNode -> parent.arr.size()
        else -> 0
    }

    override fun isLeaf(node: Any): Boolean = when (node) {
        is JsonNode.ValueNode -> true
        is JsonNode.ObjectNode -> node.obj.size() == 0
        is JsonNode.ArrayNode -> node.arr.size() == 0
        else -> true
    }

    override fun getIndexOfChild(parent: Any, child: Any): Int {
        return when (parent) {
            is JsonNode.ObjectNode -> {
                val childNode = child as JsonNode
                parent.obj.entrySet().toList().indexOfFirst { (key, value) ->
                    buildNode(key, value).text == childNode.text
                }
            }
            is JsonNode.ArrayNode -> {
                val childNode = child as JsonNode
                parent.arr.indexOfFirst { buildNode(null, it).text == childNode.text }
            }
            else -> -1
        }
    }

    override fun valueForPathChanged(path: TreePath, newValue: Any) {}
    override fun addTreeModelListener(l: TreeModelListener) {}
    override fun removeTreeModelListener(l: TreeModelListener) {}
}
