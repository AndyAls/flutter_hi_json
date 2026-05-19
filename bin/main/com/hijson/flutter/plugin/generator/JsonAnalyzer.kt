package com.hijson.flutter.plugin.generator

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

data class FieldInfo(
    val jsonKey: String,
    val dartName: String,
    val dartType: String,
    val defaultValue: String?,
    val isCamelCase: Boolean,
    val nestedClass: ClassInfo? = null
)

data class ClassInfo(
    val className: String,
    val fields: List<FieldInfo>
)

class JsonAnalyzer {

    companion object {
        private val DART_RESERVED = setOf(
            "abstract", "as", "assert", "async", "await", "base", "bool", "break",
            "case", "catch", "class", "const", "continue", "covariant", "default",
            "deferred", "do", "double", "dynamic", "else", "enum", "export",
            "extends", "extension", "external", "factory", "false", "final",
            "finally", "for", "function", "get", "hide", "if", "implements",
            "import", "in", "interface", "is", "late", "library", "mixin", "new",
            "null", "num", "on", "operator", "part", "required", "rethrow",
            "return", "sealed", "set", "show", "static", "string", "super",
            "switch", "sync", "this", "throw", "true", "try", "type", "typedef",
            "var", "void", "while", "with", "yield"
        )

        fun isCamelCase(key: String): Boolean {
            if (key.isEmpty()) return false
            if (!key[0].isLowerCase()) return false
            return !key.contains('_') && key.all { it.isLetterOrDigit() }
        }

        fun toCamelCase(key: String): String {
            if (key.isEmpty()) return key
            val parts = key.split('_').filter { it.isNotEmpty() }
            if (parts.isEmpty()) return key
            val result = StringBuilder(parts[0].lowercase())
            for (i in 1 until parts.size) {
                result.append(parts[i].lowercase().replaceFirstChar { it.uppercaseChar() })
            }
            var name = result.toString()
            if (name in DART_RESERVED) name = "\$$name"
            return name
        }

        fun toUpperCamelCase(name: String): String {
            if (name.isEmpty()) return name
            return name.replaceFirstChar { it.uppercaseChar() }
        }

        fun safeDartName(key: String): String {
            val camel = toCamelCase(key)
            return if (camel in DART_RESERVED) "\$$camel" else camel
        }
    }
}

class JsonValidationException(message: String) : Exception(message)

fun validateAndAnalyze(jsonString: String, className: String): ClassInfo {
    val trimmed = jsonString.trim()
    if (trimmed.isEmpty()) {
        throw JsonValidationException("JSON input is empty. Please paste valid JSON data.")
    }

    val element: JsonElement = try {
        JsonParser.parseString(trimmed)
    } catch (e: Exception) {
        val msg = e.message ?: "Unknown parse error"
        throw JsonValidationException("Invalid JSON format: $msg")
    }

    if (!element.isJsonObject) {
        throw JsonValidationException(
            "JSON must be an object ({...}) at the top level, got: ${describeElement(element)}"
        )
    }

    return analyzeObject(element.asJsonObject, className)
}

private fun describeElement(element: JsonElement): String = when {
    element.isJsonObject -> "object"
    element.isJsonArray -> "array"
    element.isJsonNull -> "null"
    else -> {
        val p = element.asJsonPrimitive
        when {
            p.isString -> "string"
            p.isBoolean -> "boolean"
            p.isNumber -> "number"
            else -> "unknown"
        }
    }
}

private fun analyzeObject(jsonObject: JsonObject, className: String): ClassInfo {
    val fields = mutableListOf<FieldInfo>()
    for ((key, value) in jsonObject.entrySet()) {
        fields.add(analyzeField(key, value))
    }
    return ClassInfo(className = toUpperCamelCase(className), fields = fields)
}

private fun analyzeField(key: String, value: JsonElement): FieldInfo {
    val camel = isCamelCase(key)
    val dartName = safeDartName(key)

    return when {
        value.isJsonNull -> FieldInfo(
            jsonKey = key, dartName = dartName, dartType = "dynamic",
            defaultValue = "null", isCamelCase = camel, nestedClass = null
        )
        value.isJsonPrimitive -> analyzePrimitive(key, value.asJsonPrimitive, dartName, camel)
        value.isJsonArray -> analyzeArray(key, value.asJsonArray, dartName, camel)
        value.isJsonObject -> analyzeNestedObject(key, value.asJsonObject, dartName, camel)
        else -> FieldInfo(
            jsonKey = key, dartName = dartName, dartType = "dynamic",
            defaultValue = "null", isCamelCase = camel, nestedClass = null
        )
    }
}

private fun analyzePrimitive(
    key: String, primitive: JsonPrimitive, dartName: String, camel: Boolean
): FieldInfo {
    val (dartType, defaultValue) = when {
        primitive.isBoolean -> "bool" to "false"
        primitive.isString -> "String" to "''"
        primitive.isNumber -> {
            val numStr = primitive.asString
            if (numStr.contains('.')) "double" to "0.0" else "int" to "0"
        }
        else -> "dynamic" to "null"
    }
    return FieldInfo(
        jsonKey = key, dartName = dartName, dartType = dartType,
        defaultValue = defaultValue, isCamelCase = camel, nestedClass = null
    )
}

private fun analyzeArray(
    key: String, array: JsonArray, dartName: String, camel: Boolean
): FieldInfo {
    if (array.size() == 0) {
        return FieldInfo(
            jsonKey = key, dartName = dartName, dartType = "List<dynamic>",
            defaultValue = "const []", isCamelCase = camel, nestedClass = null
        )
    }

    val first = array[0]
    val itemType = when {
        first.isJsonNull -> "dynamic"
        first.isJsonPrimitive -> {
            val p = first.asJsonPrimitive
            when {
                p.isBoolean -> "bool"
                p.isString -> "String"
                p.isNumber -> {
                    if (p.asString.contains('.')) "double" else "int"
                }
                else -> "dynamic"
            }
        }
        first.isJsonArray -> "List<dynamic>"
        first.isJsonObject -> {
            val nestedClassName = toUpperCamelCase(dartName)
            return FieldInfo(
                jsonKey = key, dartName = dartName, dartType = "List<$nestedClassName>",
                defaultValue = "const []", isCamelCase = camel,
                nestedClass = analyzeObject(first.asJsonObject, nestedClassName)
            )
        }
        else -> "dynamic"
    }

    return FieldInfo(
        jsonKey = key, dartName = dartName, dartType = "List<$itemType>",
        defaultValue = "const []", isCamelCase = camel, nestedClass = null
    )
}

private fun analyzeNestedObject(
    key: String, jsonObject: JsonObject, dartName: String, camel: Boolean
): FieldInfo {
    val nestedClassName = toUpperCamelCase(dartName)
    return FieldInfo(
        jsonKey = key, dartName = dartName, dartType = nestedClassName,
        defaultValue = null, isCamelCase = camel,
        nestedClass = analyzeObject(jsonObject, nestedClassName)
    )
}

private fun safeDartName(key: String): String = JsonAnalyzer.safeDartName(key)
private fun isCamelCase(key: String): Boolean = JsonAnalyzer.isCamelCase(key)
private fun toUpperCamelCase(name: String): String = JsonAnalyzer.toUpperCamelCase(name)
