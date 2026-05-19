package com.hijson.flutter.plugin.ui

import com.hijson.flutter.plugin.config.PluginSettings
import com.hijson.flutter.plugin.generator.ClassInfo
import com.hijson.flutter.plugin.generator.DartClassGenerator
import com.hijson.flutter.plugin.generator.GenerationOptions
import com.hijson.flutter.plugin.generator.JsonValidationException
import com.hijson.flutter.plugin.generator.validateAndAnalyze
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSplitPane
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class HiJsonMainPanel(
    private val settings: PluginSettings
) : JPanel(BorderLayout(0, 10)) {

    private val editorPanel = JsonEditorPanel { onEditorTextChanged() }
    private val previewPanel = PreviewPanel()
    private val splitPane: JSplitPane
    private val errorLabel: JLabel
    private val classNameField = JTextField("HiJson", 20)
    private val nullableRadio = JRadioButton("nullable")
    private val defaultValueRadio = JRadioButton("default value")
    private val useJsonKeyNameCheck = JCheckBox("use JsonKey.name")
    private val copyWithCheck = JCheckBox("copyWith")
    private val toStringCheck = JCheckBox("toString")

    private var currentClassInfo: ClassInfo? = null
    private var isTreeMode: Boolean = false

    private val dartGenerator: DartClassGenerator
        get() = DartClassGenerator(currentOptions())

    init {
        nullableRadio.isSelected = settings.nullableDefault
        defaultValueRadio.isSelected = settings.defaultValueDefault
        useJsonKeyNameCheck.isSelected = settings.useJsonKeyNameDefault
        copyWithCheck.isSelected = settings.copyWithDefault
        toStringCheck.isSelected = settings.toStringDefault

        border = BorderFactory.createEmptyBorder(10, 12, 10, 12)

        add(createHeaderPanel(), BorderLayout.NORTH)
        splitPane = createSplitPane()
        add(splitPane, BorderLayout.CENTER)
        add(createOptionsPanel(), BorderLayout.SOUTH)

        errorLabel = editorPanel.errorLabel

        preferredSize = Dimension(900, 700)
    }

    private fun createHeaderPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            insets = Insets(0, 0, 4, 0)
        }

        val titleLabel = JBLabel(settings.dialogTitle).apply {
            font = Font("SansSerif", Font.BOLD, 15)
        }
        gbc.gridy = 0
        panel.add(titleLabel, gbc)

        val promptLabel = JBLabel(settings.promptText).apply {
            font = Font("SansSerif", Font.PLAIN, 12)
            foreground = Color(0x88, 0x88, 0x88)
        }
        gbc.gridy = 1
        gbc.insets = Insets(0, 0, 0, 0)
        panel.add(promptLabel, gbc)

        return panel
    }

    private fun createSplitPane(): JSplitPane {
        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(editorPanel, BorderLayout.CENTER)

        return JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, previewPanel).apply {
            resizeWeight = 0.5
            dividerSize = 4
            border = BorderFactory.createEmptyBorder()
        }
    }

    private fun createOptionsPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            anchor = GridBagConstraints.WEST
        }

        // Row 1: Format and Format Only buttons
        val buttonRow = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
        val formatBtn = JButton("Format").apply {
            addActionListener { onFormat() }
        }
        val formatOnlyBtn = JButton("Format Only").apply {
            addActionListener { onFormatOnly() }
        }
        buttonRow.add(formatBtn)
        buttonRow.add(formatOnlyBtn)

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 3
        gbc.insets = Insets(8, 0, 6, 0)
        panel.add(buttonRow, gbc)
        gbc.gridwidth = 1

        // Row 2: Class Name
        gbc.gridy = 1
        gbc.insets = Insets(4, 0, 4, 0)
        panel.add(JLabel("Class Name:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 0.0
        gbc.insets = Insets(4, 6, 4, 0)
        classNameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = onClassNameChanged()
            override fun removeUpdate(e: DocumentEvent) = onClassNameChanged()
            override fun changedUpdate(e: DocumentEvent) = onClassNameChanged()
        })
        panel.add(classNameField, gbc)

        // Row 3: Radio buttons (nullable / default value)
        ButtonGroup().apply {
            add(nullableRadio)
            add(defaultValueRadio)
        }
        nullableRadio.addActionListener { regeneratePreview() }
        defaultValueRadio.addActionListener { regeneratePreview() }

        gbc.gridx = 0
        gbc.gridy = 2
        gbc.weightx = 0.0
        gbc.insets = Insets(6, 0, 0, 18)
        panel.add(nullableRadio, gbc)

        gbc.gridx = 1
        gbc.insets = Insets(6, 0, 0, 18)
        panel.add(defaultValueRadio, gbc)

        // Row 4: Checkboxes (use JsonKey.name / copyWith / toString)
        useJsonKeyNameCheck.addActionListener { regeneratePreview() }
        copyWithCheck.addActionListener { regeneratePreview() }
        toStringCheck.addActionListener { regeneratePreview() }

        gbc.gridx = 0
        gbc.gridy = 3
        gbc.insets = Insets(6, 0, 0, 12)
        panel.add(useJsonKeyNameCheck, gbc)

        gbc.gridx = 1
        gbc.insets = Insets(6, 0, 0, 12)
        panel.add(copyWithCheck, gbc)

        gbc.gridx = 2
        gbc.insets = Insets(6, 0, 0, 0)
        panel.add(toStringCheck, gbc)

        return panel
    }

    private fun onEditorTextChanged() {
        editorPanel.showError(null)
    }

    private fun onFormat() {
        isTreeMode = false
        formatAndGenerate()
    }

    private fun onFormatOnly() {
        isTreeMode = true
        formatAndShowTree()
    }

    private fun onClassNameChanged() {
        if (!isTreeMode) {
            regeneratePreview()
        }
    }

    private fun formatAndGenerate() {
        val text = editorPanel.getText().trim()
        if (text.isEmpty()) {
            editorPanel.showError("JSON input is empty. Please paste valid JSON data.")
            previewPanel.clear()
            return
        }

        try {
            val formatted = formatJson(text)
            editorPanel.setText(formatted)
            editorPanel.showError(null)

            val className = getClassName()
            currentClassInfo = validateAndAnalyze(formatted, className)
            regeneratePreview()
        } catch (e: JsonValidationException) {
            editorPanel.showError(e.message)
            previewPanel.clear()
            currentClassInfo = null
        }
    }

    private fun formatAndShowTree() {
        val text = editorPanel.getText().trim()
        if (text.isEmpty()) {
            editorPanel.showError("JSON input is empty. Please paste valid JSON data.")
            previewPanel.clear()
            return
        }

        try {
            val formatted = formatJson(text)
            editorPanel.setText(formatted)
            editorPanel.showError(null)
            previewPanel.showJsonTree(formatted)
        } catch (e: JsonValidationException) {
            editorPanel.showError(e.message)
            previewPanel.clear()
        } catch (e: Exception) {
            editorPanel.showError("Invalid JSON format: ${e.message}")
            previewPanel.clear()
        }
    }

    private fun regeneratePreview() {
        val info = currentClassInfo ?: return
        val className = getClassName()
        if (className.isBlank()) return

        val updatedInfo = if (className != info.className) {
            info.copy(className = className)
        } else {
            info
        }

        try {
            val result = dartGenerator.generate(updatedInfo)
            previewPanel.showCodePreview(result.code)
        } catch (e: Exception) {
            previewPanel.clear()
        }
    }

    fun getRawClassName(): String = classNameField.text.trim().ifEmpty { "HiJson" }

    private fun getClassName(): String {
        val raw = classNameField.text.trim()
        if (raw.isEmpty()) return "HiJson"
        val camel = com.hijson.flutter.plugin.generator.JsonAnalyzer.toCamelCase(raw)
        return com.hijson.flutter.plugin.generator.JsonAnalyzer.toUpperCamelCase(camel)
    }

    fun currentOptions() = GenerationOptions(
        nullable = nullableRadio.isSelected,
        defaultValue = defaultValueRadio.isSelected,
        useJsonKeyName = useJsonKeyNameCheck.isSelected,
        copyWith = copyWithCheck.isSelected,
        toString = toStringCheck.isSelected
    )

    fun validateJson(): String? {
        val text = editorPanel.getText().trim()
        if (text.isEmpty()) return "JSON input is empty. Please paste valid JSON data."
        return try {
            com.google.gson.JsonParser.parseString(text)
            if (!com.google.gson.JsonParser.parseString(text).isJsonObject) {
                "JSON must be an object ({...}) at the top level."
            } else {
                null
            }
        } catch (e: Exception) {
            "Invalid JSON format: ${e.message}"
        }
    }

    fun generateDartCode(): String? {
        val text = editorPanel.getText().trim()
        if (text.isEmpty()) {
            editorPanel.showError("JSON input is empty. Please paste valid JSON data.")
            return null
        }

        return try {
            val className = getClassName()
            val classInfo = validateAndAnalyze(text, className)
            val result = dartGenerator.generate(classInfo)
            result.code
        } catch (e: JsonValidationException) {
            editorPanel.showError(e.message)
            null
        }
    }

    fun getJsonText(): String = editorPanel.getText()

    companion object {
        fun formatJson(jsonString: String): String {
            val element = com.google.gson.JsonParser.parseString(jsonString.trim())
            val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
            return gson.toJson(element)
        }
    }
}
