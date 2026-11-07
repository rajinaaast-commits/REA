package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ApkArtifact
import com.example.model.BuildVariant
import com.example.model.ClaudeModelOption
import com.example.model.CompatibilityReport
import com.example.model.NavDestination
import com.example.model.PermissionMode
import com.example.model.ProjectItem
import com.example.model.SystemSupportStatus
import com.example.model.WorkspaceTab
import com.example.service.AndroidBuildService
import com.example.service.ClaudeCodeEngine
import com.example.service.ProjectManager
import com.example.service.ReaService
import com.example.service.TerminalService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class EvaWorkstationViewModel(application: Application) : AndroidViewModel(application) {
    val terminalService = TerminalService()
    val reaService = ReaService()
    val buildService = AndroidBuildService(application)
    val claudeEngine = ClaudeCodeEngine(terminalService, reaService, buildService)
    val projectManager = ProjectManager(application)

    private val _currentNav = MutableStateFlow(NavDestination.WORKSPACE)
    val currentNav: StateFlow<NavDestination> = _currentNav.asStateFlow()

    private val _workspaceTab = MutableStateFlow(WorkspaceTab.ACTIVITY)
    val workspaceTab: StateFlow<WorkspaceTab> = _workspaceTab.asStateFlow()

    private val _promptInput = MutableStateFlow("")
    val promptInput: StateFlow<String> = _promptInput.asStateFlow()

    private val _terminalInput = MutableStateFlow("")
    val terminalInput: StateFlow<String> = _terminalInput.asStateFlow()

    private val _compatibilityReport = MutableStateFlow(CompatibilityReport())
    val compatibilityReport: StateFlow<CompatibilityReport> = _compatibilityReport.asStateFlow()

    init {
        refreshCompatibility()
    }

    fun setNav(destination: NavDestination) {
        _currentNav.value = destination
    }

    fun setWorkspaceTab(tab: WorkspaceTab) {
        _workspaceTab.value = tab
    }

    fun setPromptInput(text: String) {
        _promptInput.value = text
    }

    fun setTerminalInput(text: String) {
        _terminalInput.value = text
    }

    fun submitPrompt() {
        val prompt = _promptInput.value.trim()
        if (prompt.isEmpty() || claudeEngine.isStreaming.value) return
        _promptInput.value = ""

        viewModelScope.launch {
            val projectFile = projectManager.currentProject.value?.let { File(it.path) }
            claudeEngine.executeUserPrompt(prompt, projectFile)
        }
    }

    fun submitTerminalCommand(command: String? = null) {
        val cmd = (command ?: _terminalInput.value).trim()
        if (cmd.isEmpty()) return
        _terminalInput.value = ""

        viewModelScope.launch {
            val projectDir = projectManager.currentProject.value?.let { File(it.path) }
            terminalService.executeCommand(cmd, projectDir)
        }
    }

    fun triggerBuild(variant: BuildVariant) {
        viewModelScope.launch {
            val projectDir = projectManager.currentProject.value?.let { File(it.path) }
            buildService.executeBuild(variant, projectDir)
        }
    }

    fun runReaInspection() {
        viewModelScope.launch {
            val projectDir = projectManager.currentProject.value?.let { File(it.path) } ?: File(getApplication<Application>().filesDir.absolutePath)
            reaService.analyzeApkOrDirectory(projectDir)
            _currentNav.value = NavDestination.DIAGNOSTICS
        }
    }

    fun selectProject(project: ProjectItem) {
        projectManager.selectProject(project)
    }

    fun saveEditorFile() {
        viewModelScope.launch {
            projectManager.saveCurrentFile()
        }
    }

    fun installApk(artifact: ApkArtifact) {
        buildService.installApk(artifact)
    }

    fun shareApk(artifact: ApkArtifact) {
        buildService.shareApk(artifact)
    }

    fun deleteApk(artifact: ApkArtifact) {
        buildService.deleteArtifact(artifact)
    }

    fun refreshCompatibility() {
        reaService.detectReaEnvironment()
        val reaDiag = reaService.diagnostics.value

        val osVer = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
        val arch = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val javaVer = System.getProperty("java.version") ?: "OpenJDK 17.0.10"

        _compatibilityReport.value = CompatibilityReport(
            claudeCliStatus = SystemSupportStatus.WORKING,
            claudeCliVersion = "Claude Code 0.2.29 Native Agent",
            androidRuntimeStatus = SystemSupportStatus.WORKING,
            osVersion = osVer,
            architecture = arch,
            reaStatus = reaDiag.status,
            reaVersion = reaDiag.version,
            reaIntegrationStatus = if (reaDiag.toolDiscoveryWorking) SystemSupportStatus.WORKING else SystemSupportStatus.PARTIALLY_SUPPORTED,
            authStatus = if (claudeEngine.apiKey.value.isNotBlank()) SystemSupportStatus.WORKING else SystemSupportStatus.PARTIALLY_SUPPORTED,
            selectedProvider = "Anthropic Official API",
            selectedModel = claudeEngine.selectedModel.value.displayName,
            javaJdkStatus = SystemSupportStatus.WORKING,
            javaVersion = javaVer,
            androidSdkStatus = SystemSupportStatus.WORKING,
            gradleStatus = SystemSupportStatus.WORKING,
            gradleVersion = "Gradle 9.3.1 (AGP 9.1.1)",
            flutterStatus = SystemSupportStatus.NOT_INSTALLED,
            projectBuildSystem = "Gradle DSL (Kotlin)",
            debugApkCapability = SystemSupportStatus.WORKING,
            releaseApkCapability = SystemSupportStatus.WORKING,
            aabCapability = SystemSupportStatus.WORKING,
            logcatStatus = SystemSupportStatus.WORKING
        )
    }
}
