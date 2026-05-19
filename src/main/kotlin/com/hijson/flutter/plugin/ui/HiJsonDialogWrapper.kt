package com.hijson.flutter.plugin.ui

import com.hijson.flutter.plugin.config.PluginSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import java.awt.Dimension
import javax.swing.JComponent

class HiJsonDialogWrapper(
    project: Project,
    private val onOk: (dartCode: String, fileName: String) -> Unit
) : DialogWrapper(project) {

    private val settings = PluginSettings.getInstance(project)
    private val mainPanel = HiJsonMainPanel(settings)

    init {
        title = settings.dialogTitle
        setOKButtonText("OK")
        init()
        isResizable = true
    }

    override fun createCenterPanel(): JComponent = mainPanel

    override fun getPreferredFocusedComponent(): JComponent = mainPanel

    override fun doOKAction() {
        val error = mainPanel.validateJson()
        if (error != null) {
            Messages.showErrorDialog(
                rootPane, error, "Invalid JSON"
            )
            return
        }
        val code = mainPanel.generateDartCode()
        if (code != null) {
            val fileName = mainPanel.getRawClassName()
            onOk(code, fileName)
            super.doOKAction()
        }
    }

    override fun doCancelAction() {
        super.doCancelAction()
    }

    override fun getDimensionServiceKey(): String = "FlutterHiJsonDialog"

    fun getJsonText(): String = mainPanel.getJsonText()
}
