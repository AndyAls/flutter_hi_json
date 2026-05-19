package com.hijson.flutter.plugin.generator

data class GenerationOptions(
    val nullable: Boolean = false,
    val defaultValue: Boolean = true,
    val useJsonKeyName: Boolean = true,
    val copyWith: Boolean = false,
    val toString: Boolean = false
)

class DartClassGenerator(private val options: GenerationOptions) {

    fun generate(classInfo: ClassInfo): GeneratedResult {
        val nestedClasses = mutableListOf<String>()
        val mainClassCode = buildClassCode(classInfo, nestedClasses)
        val parts = nestedClasses + mainClassCode
        val fullCode = buildFileHeader(
            classInfo.className
        ) + "\n" + parts.joinToString("\n")
        return GeneratedResult(
            className = classInfo.className,
            fileName = toSnakeCase(classInfo.className),
            code = fullCode
        )
    }

    private fun buildFileHeader(className: String): String = buildString {
        appendLine("import 'package:json_annotation/json_annotation.dart';")
        appendLine()
        appendLine("part '${toSnakeCase(className)}.g.dart';")
    }

    private fun buildClassCode(classInfo: ClassInfo, nestedCollector: MutableList<String>): String {
        for (field in classInfo.fields) {
            if (field.nestedClass != null) {
                nestedCollector.add(buildClassCode(field.nestedClass, nestedCollector))
            }
        }

        return buildString {
            appendLine()
            appendLine("@JsonSerializable()")
            appendLine("class ${classInfo.className} {")

            if (classInfo.fields.isEmpty()) {
                appendLine()
                appendLine("  ${classInfo.className}();")
                appendLine()
                appendLine("  factory ${classInfo.className}.fromJson(Map<String, dynamic> json) =>")
                appendLine("      _$${classInfo.className}FromJson(json);")
                appendLine()
                appendLine("  Map<String, dynamic> toJson() => _$${classInfo.className}ToJson(this);")
                if (options.copyWith) {
                    appendLine()
                    buildCopyWith(classInfo)
                }
                if (options.toString) {
                    appendLine()
                    buildToString(classInfo)
                }
            } else {
                buildFields(classInfo)
                appendLine()
                buildConstructor(classInfo)
                appendLine()
                buildFromJson(classInfo)
                appendLine()
                buildToJson(classInfo)
                if (options.copyWith) {
                    appendLine()
                    buildCopyWith(classInfo)
                }
                if (options.toString) {
                    appendLine()
                    buildToString(classInfo)
                }
            }

            appendLine("}")
        }
    }

    private fun StringBuilder.buildFields(classInfo: ClassInfo) {
        for (field in classInfo.fields) {
            val annotation = buildJsonKeyAnnotation(field)
            val type = buildType(field.dartType)
            if (annotation != null) {
                appendLine("  $annotation")
            }
            appendLine("  final $type ${field.dartName};")
        }
    }

    private fun buildJsonKeyAnnotation(field: FieldInfo): String? {
        val parts = mutableListOf<String>()

        val needsName = if (options.useJsonKeyName) {
            true
        } else {
            !field.isCamelCase
        }

        if (needsName) {
            parts.add("name: '${field.jsonKey}'")
        }

        if (options.defaultValue && !field.dartType.startsWith("List") &&
            field.nestedClass == null && field.defaultValue != null
        ) {
            parts.add("defaultValue: ${field.defaultValue}")
        } else if (options.defaultValue && field.dartType.startsWith("List")) {
            parts.add("defaultValue: const []")
        }

        return if (parts.isEmpty()) null else "@JsonKey(${parts.joinToString(", ")})"
    }

    private fun buildType(dartType: String): String {
        return if (options.nullable) {
            if (dartType.endsWith("?")) dartType else "$dartType?"
        } else {
            dartType
        }
    }

    private fun StringBuilder.buildConstructor(classInfo: ClassInfo) {
        val useRequired = !options.nullable || options.defaultValue

        appendLine("  ${classInfo.className}({")
        for (field in classInfo.fields) {
            if (useRequired) {
                appendLine("    required this.${field.dartName},")
            } else {
                appendLine("    this.${field.dartName},")
            }
        }
        appendLine("  });")
    }

    private fun StringBuilder.buildFromJson(classInfo: ClassInfo) {
        val name = classInfo.className
        appendLine("  factory $name.fromJson(Map<String, dynamic> json) =>")
        appendLine("      _\$${name}FromJson(json);")
    }

    private fun StringBuilder.buildToJson(classInfo: ClassInfo) {
        val name = classInfo.className
        appendLine("  Map<String, dynamic> toJson() => _\$${name}ToJson(this);")
    }

    private fun StringBuilder.buildCopyWith(classInfo: ClassInfo) {
        val name = classInfo.className
        appendLine("  $name copyWith({")
        for (field in classInfo.fields) {
            val paramType = if (field.dartType.endsWith("?")) field.dartType else "${field.dartType}?"
            appendLine("    $paramType ${field.dartName},")
        }
        appendLine("  }) {")
        if (classInfo.fields.isEmpty()) {
            appendLine("    return $name();")
        } else {
            appendLine("    return $name(")
            for (field in classInfo.fields) {
                appendLine("      ${field.dartName}: ${field.dartName} ?? this.${field.dartName},")
            }
            appendLine("    );")
        }
        appendLine("  }")
    }

    private fun StringBuilder.buildToString(classInfo: ClassInfo) {
        val name = classInfo.className
        val inner = classInfo.fields.joinToString(", ") { "${it.dartName}: \$${it.dartName}" }
        appendLine("  @override")
        appendLine("  String toString() => '$name($inner)';")
    }

    companion object {
        fun toSnakeCase(className: String): String {
            val sb = StringBuilder()
            for ((i, ch) in className.withIndex()) {
                if (ch.isUpperCase()) {
                    if (i > 0) sb.append('_')
                    sb.append(ch.lowercaseChar())
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        private fun StringBuilder.appendLine(s: String = "") {
            append(s)
            append('\n')
        }
    }
}

data class GeneratedResult(
    val className: String,
    val fileName: String,
    val code: String
)
