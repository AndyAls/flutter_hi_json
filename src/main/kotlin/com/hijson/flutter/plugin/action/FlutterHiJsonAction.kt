package com.hijson.flutter.plugin.action

import com.hijson.flutter.plugin.ui.HiJsonDialogWrapper
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.terminal.TerminalView
import java.io.File

class FlutterHiJsonAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedDir = resolveTargetDirectory(e)
            ?: project.basePath?.let { LocalFileSystem.getInstance().findFileByPath(it) }
        if (selectedDir == null) {
            showNotification(project, "Please select a directory first.", NotificationType.WARNING)
            return
        }

        val dialog = HiJsonDialogWrapper(project) { dartCode, fileName ->
            generateFile(project, selectedDir, dartCode, fileName)
        }

        if (dialog.showAndGet()) {
            // OK was clicked - file generation handled in onOk callback
        }
    }

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = e.project != null
    }

    private fun resolveTargetDirectory(e: AnActionEvent): VirtualFile? {
        val dataContext = e.dataContext
        val virtualFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext)
        if (virtualFiles != null && virtualFiles.isNotEmpty()) {
            val first = virtualFiles.first()
            return if (first.isDirectory) first else first.parent
        }

        val singleFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext)
        if (singleFile != null) {
            return if (singleFile.isDirectory) singleFile else singleFile.parent
        }

        val psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext)
        if (psiElement != null) {
            val vf = psiElement.containingFile?.virtualFile ?: psiElement.containingFile?.originalFile?.virtualFile
            if (vf != null) {
                return if (vf.isDirectory) vf else vf.parent
            }
        }

        return null
    }

    private fun generateFile(project: Project, targetDir: VirtualFile, dartCode: String, fileName: String) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project, "Generating Dart entity class...", false
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Writing file..."
                indicator.isIndeterminate = true

                try {
                    val filePath = "${targetDir.path}/$fileName.dart"
                    val file = File(filePath)

                    ApplicationManager.getApplication().invokeAndWait {
                        ApplicationManager.getApplication().runWriteAction {
                            file.writeText(dartCode)
                        }
                    }

                    LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath)

                    val flutterRoot = findFlutterProjectRoot(targetDir)
                    if (flutterRoot != null) {
                        ApplicationManager.getApplication().invokeLater {
                            openTerminalAndRunBuildRunner(project, flutterRoot)
                        }
                    } else {
                        showNotification(
                            project,
                            "File generated at $filePath. Could not find Flutter project root (pubspec.yaml) to run build_runner.",
                            NotificationType.WARNING
                        )
                        return
                    }

                    showNotification(
                        project,
                        "Generated $fileName.dart. Running build_runner in the Terminal.",
                        NotificationType.INFORMATION
                    )
                } catch (ex: Exception) {
                    showNotification(
                        project,
                        "Error generating file: ${ex.message}",
                        NotificationType.ERROR
                    )
                }
            }
        })
    }

    private fun findFlutterProjectRoot(directory: VirtualFile): File? {
        var current: File = File(directory.path)
        while (true) {
            if (File(current, "pubspec.yaml").exists()) {
                return current
            }
            val parent = current.parentFile ?: return null
            current = parent
        }
    }

    private fun openTerminalAndRunBuildRunner(project: Project, projectRoot: File) {
        try {
            val terminalView = TerminalView.getInstance(project)
            val widget = terminalView.createLocalShellWidget(
                projectRoot.absolutePath,
                "Build Runner"
            )
            widget.executeCommand(
                "dart run build_runner build --delete-conflicting-outputs"
            )
        } catch (e: Exception) {
            showNotification(
                project,
                "Failed to open terminal: ${e.message}",
                NotificationType.ERROR
            )
        }
    }


    private fun showNotification(project: Project, content: String, type: NotificationType) {
        ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Flutter Hi Json")
                .createNotification(content, type)
                .notify(project)
        }
    }
}
