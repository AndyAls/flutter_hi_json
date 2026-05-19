package com.hijson.flutter.plugin.ui

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTree
import javax.swing.border.TitledBorder
import javax.swing.tree.DefaultTreeCellRenderer

class PreviewPanel : JPanel(BorderLayout()) {

    private val cardLayout = CardLayout()
    private val cardPanel = JPanel(cardLayout)

    val codePreview: JTextArea = JTextArea().apply {
        font = Font("JetBrains Mono", Font.PLAIN, 13)
        isEditable = false
        lineWrap = false
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
    }

    private val codeScrollPane: JScrollPane = JScrollPane(codePreview).apply {
        border = BorderFactory.createLineBorder(Color(0xBB, 0xBB, 0xBB))
    }

    private var tree: JTree? = null
    private var treeScrollPane: JScrollPane? = null

    private val emptyLabel: JTextArea = JTextArea().apply {
        font = Font("SansSerif", Font.PLAIN, 13)
        foreground = Color(0x99, 0x99, 0x99)
        text = "Click \"Format\" to preview Dart entity class\nClick \"Format Only\" to view JSON tree structure"
        isEditable = false
        border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
    }
    private val emptyScrollPane: JScrollPane = JScrollPane(emptyLabel).apply {
        border = BorderFactory.createLineBorder(Color(0xBB, 0xBB, 0xBB))
    }

    init {
        cardPanel.add(codeScrollPane, "code")
        cardPanel.add(emptyScrollPane, "empty")
        add(cardPanel, BorderLayout.CENTER)
        cardLayout.show(cardPanel, "empty")
    }

    fun showCodePreview(code: String) {
        ensureTreeCard()
        codePreview.text = code
        codePreview.caretPosition = 0
        cardLayout.show(cardPanel, "code")
    }

    fun showJsonTree(jsonString: String) {
        try {
            val model = JsonTreeModel(jsonString)
            val newTree = JTree(model).apply {
                setCellRenderer(object : DefaultTreeCellRenderer() {
                    override fun getTreeCellRendererComponent(
                        tree: JTree, value: Any, sel: Boolean, expanded: Boolean,
                        leaf: Boolean, row: Int, hasFocus: Boolean
                    ): java.awt.Component {
                        super.getTreeCellRendererComponent(
                            tree, value, sel, expanded, leaf, row, hasFocus
                        )
                        icon = null
                        font = Font("JetBrains Mono", Font.PLAIN, 13)
                        border = BorderFactory.createEmptyBorder(2, 4, 2, 4)
                        return this
                    }
                })
                isRootVisible = true
                setShowsRootHandles(true)
            }
            treeScrollPane = JScrollPane(newTree).apply {
                border = BorderFactory.createLineBorder(Color(0xBB, 0xBB, 0xBB))
            }
            tree = newTree
            cardPanel.add(treeScrollPane, "tree")
            cardLayout.show(cardPanel, "tree")
        } catch (e: Exception) {
            codePreview.text = "Failed to parse JSON for tree view"
            cardLayout.show(cardPanel, "code")
        }
    }

    private fun ensureTreeCard() {
        tree?.let {
            cardPanel.remove(treeScrollPane)
            tree = null
            treeScrollPane = null
        }
    }

    fun clear() {
        cardLayout.show(cardPanel, "empty")
    }
}
