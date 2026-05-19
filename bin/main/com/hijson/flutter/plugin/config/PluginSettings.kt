package com.hijson.flutter.plugin.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JPanel

@State(
    name = "FlutterHiJsonSettings",
    storages = [Storage("flutter_hi_json_settings.xml")]
)
@Service(Service.Level.PROJECT)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {

    data class State(
        var dialogTitle: String = "Flutter Hi Json",
        var promptText: String = "Paste your JSON here...",
        var nullableDefault: Boolean = true,
        var defaultValueDefault: Boolean = false,
        var useJsonKeyNameDefault: Boolean = true,
        var copyWithDefault: Boolean = false,
        var toStringDefault: Boolean = false
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    val dialogTitle: String get() = myState.dialogTitle
    val promptText: String get() = myState.promptText
    val nullableDefault: Boolean get() = myState.nullableDefault
    val defaultValueDefault: Boolean get() = myState.defaultValueDefault
    val useJsonKeyNameDefault: Boolean get() = myState.useJsonKeyNameDefault
    val copyWithDefault: Boolean get() = myState.copyWithDefault
    val toStringDefault: Boolean get() = myState.toStringDefault

    companion object {
        fun getInstance(project: Project): PluginSettings =
            project.getService(PluginSettings::class.java)
    }
}

class PluginSettingsConfigurable(private val project: Project) : Configurable {

    private val settings: PluginSettings get() = PluginSettings.getInstance(project)

    private var titleField: JBTextField? = null
    private var promptField: JBTextField? = null
    private var nullableDefaultCheck: JBCheckBox? = null
    private var defaultValueDefaultCheck: JBCheckBox? = null
    private var useJsonKeyNameDefaultCheck: JBCheckBox? = null
    private var copyWithDefaultCheck: JBCheckBox? = null
    private var toStringDefaultCheck: JBCheckBox? = null

    override fun getDisplayName(): String = "Flutter Hi Json"

    override fun createComponent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            insets = Insets(6, 6, 6, 6)
        }

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        panel.add(JBLabel("Dialog Title:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        titleField = JBTextField(settings.dialogTitle, 30)
        panel.add(titleField, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 0.0
        panel.add(JBLabel("Prompt Text:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        promptField = JBTextField(settings.promptText, 30)
        panel.add(promptField, gbc)

        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 2
        gbc.insets = Insets(14, 6, 2, 6)
        panel.add(JBLabel("Default checkbox states:"), gbc)

        gbc.gridwidth = 1
        gbc.gridy = 3
        gbc.insets = Insets(4, 20, 4, 6)
        nullableDefaultCheck = JBCheckBox("nullable selected by default", settings.nullableDefault)
        panel.add(nullableDefaultCheck, gbc)

        gbc.gridy = 4
        defaultValueDefaultCheck = JBCheckBox("default value selected by default", settings.defaultValueDefault)
        panel.add(defaultValueDefaultCheck, gbc)

        gbc.gridy = 5
        useJsonKeyNameDefaultCheck = JBCheckBox("use JsonKey.name checked by default", settings.useJsonKeyNameDefault)
        panel.add(useJsonKeyNameDefaultCheck, gbc)

        gbc.gridy = 6
        copyWithDefaultCheck = JBCheckBox("copyWith checked by default", settings.copyWithDefault)
        panel.add(copyWithDefaultCheck, gbc)

        gbc.gridy = 7
        toStringDefaultCheck = JBCheckBox("toString checked by default", settings.toStringDefault)
        panel.add(toStringDefaultCheck, gbc)

        gbc.gridy = 8
        gbc.weighty = 1.0
        panel.add(JPanel(), gbc)

        return panel
    }

    override fun isModified(): Boolean {
        return titleField?.text != settings.dialogTitle ||
                promptField?.text != settings.promptText ||
                nullableDefaultCheck?.isSelected != settings.nullableDefault ||
                defaultValueDefaultCheck?.isSelected != settings.defaultValueDefault ||
                useJsonKeyNameDefaultCheck?.isSelected != settings.useJsonKeyNameDefault ||
                copyWithDefaultCheck?.isSelected != settings.copyWithDefault ||
                toStringDefaultCheck?.isSelected != settings.toStringDefault
    }

    override fun apply() {
        val state = settings.state
        titleField?.text?.let { state.dialogTitle = it }
        promptField?.text?.let { state.promptText = it }
        state.nullableDefault = nullableDefaultCheck?.isSelected ?: true
        state.defaultValueDefault = defaultValueDefaultCheck?.isSelected ?: false
        state.useJsonKeyNameDefault = useJsonKeyNameDefaultCheck?.isSelected ?: true
        state.copyWithDefault = copyWithDefaultCheck?.isSelected ?: false
        state.toStringDefault = toStringDefaultCheck?.isSelected ?: false
    }

    override fun reset() {
        titleField?.text = settings.dialogTitle
        promptField?.text = settings.promptText
        nullableDefaultCheck?.isSelected = settings.nullableDefault
        defaultValueDefaultCheck?.isSelected = settings.defaultValueDefault
        useJsonKeyNameDefaultCheck?.isSelected = settings.useJsonKeyNameDefault
        copyWithDefaultCheck?.isSelected = settings.copyWithDefault
        toStringDefaultCheck?.isSelected = settings.toStringDefault
    }
}
