package com.hijson.flutter.plugin.ui

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class JsonEditorPanel(
    private val onTextChanged: () -> Unit
) : JPanel(BorderLayout()) {

    val editor: JTextArea = JTextArea().apply {
        font = Font("JetBrains Mono", Font.PLAIN, 13)
        tabSize = 2
        lineWrap = false
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = onTextChanged()
            override fun removeUpdate(e: DocumentEvent) = onTextChanged()
            override fun changedUpdate(e: DocumentEvent) = onTextChanged()
        })
    }

    val errorLabel: JLabel = JLabel().apply {
        foreground = Color(0xCC, 0x33, 0x33)
        font = Font("SansSerif", Font.PLAIN, 12)
        border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
        isVisible = false
    }

    private val scrollPane: JScrollPane = JScrollPane(editor).apply {
        border = BorderFactory.createLineBorder(Color(0xBB, 0xBB, 0xBB))
        horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
    }

    init {
        add(scrollPane, BorderLayout.CENTER)
        add(errorLabel, BorderLayout.SOUTH)
    }

    fun showError(message: String?) {
        if (message != null) {
            errorLabel.text = message
            errorLabel.isVisible = true
            scrollPane.border = BorderFactory.createLineBorder(Color(0xCC, 0x33, 0x33))
        } else {
            errorLabel.text = ""
            errorLabel.isVisible = false
            scrollPane.border = BorderFactory.createLineBorder(Color(0xBB, 0xBB, 0xBB))
        }
    }

    fun setText(text: String) {
        SwingUtilities.invokeLater {
            editor.text = text
            editor.caretPosition = 0
        }
    }

    fun getText(): String = editor.text
}
