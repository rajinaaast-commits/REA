package com.example.model

enum class NavDestination(val label: String) {
    WORKSPACE("Workspace"),
    PROJECTS("Projects"),
    BUILD("Build"),
    DIAGNOSTICS("Diagnostics"),
    SETTINGS("Settings")
}

enum class WorkspaceTab(val label: String) {
    PLAN("Plan"),
    ACTIVITY("Activity"),
    TERMINAL("Terminal")
}

enum class PermissionMode(val label: String, val description: String) {
    ASK_ALL("Strict (Ask All)", "Require explicit approval for file edits and terminal commands"),
    AUTO_READ("Auto Read", "Auto-approve file reading; ask for edits, deletions, and commands"),
    AUTONOMOUS("Autonomous", "Auto-approve all safe tools; ask only for destructive actions")
}

data class ProjectItem(
    val id: String,
    val name: String,
    val path: String,
    val isAndroidProject: Boolean = true,
    val buildSystem: String = "Gradle",
    val gitBranch: String = "main",
    val uncommittedChanges: Int = 0,
    val lastOpened: Long = System.currentTimeMillis()
)

data class ClaudeModelOption(
    val id: String,
    val displayName: String,
    val contextWindow: String,
    val supportsThinking: Boolean = true
)

enum class EventType {
    USER_PROMPT,
    AGENT_RESPONSE,
    PLAN_UPDATE,
    TOOL_CALL,
    TOOL_RESULT,
    FILE_READ,
    FILE_WRITE,
    TERMINAL_COMMAND,
    PERMISSION_REQUEST,
    BUILD_EVENT,
    ERROR
}

enum class EventStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUCCESS,
    FAILED
}

data class ActivityEvent(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: EventType,
    val title: String,
    val description: String,
    val toolName: String? = null,
    val filePath: String? = null,
    val codeSnippet: String? = null,
    val command: String? = null,
    val exitCode: Int? = null,
    val status: EventStatus = EventStatus.SUCCESS
)

enum class PlanStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class PlanStep(
    val id: String,
    val title: String,
    val detail: String = "",
    val status: PlanStatus = PlanStatus.PENDING
)

data class TerminalLog(
    val id: String,
    val text: String,
    val isError: Boolean = false,
    val isCommand: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class BuildVariant(val taskName: String, val artifactName: String) {
    DEBUG_APK(":app:assembleDebug", "app-debug.apk"),
    RELEASE_APK(":app:assembleRelease", "app-release-unsigned.apk"),
    AAB(":app:bundleRelease", "app-release.aab"),
    CLEAN("clean", ""),
    TEST(":app:testDebugUnitTest", ""),
    LINT(":app:lintDebug", "")
}

data class ApkArtifact(
    val id: String,
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val variant: String,
    val createdAt: Long,
    val verified: Boolean,
    val packageName: String = "com.aistudio.evabuildcli.mkvz",
    val versionName: String = "1.0",
    val versionCode: Int = 1
) {
    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            return if (kb > 1024.0) {
                String.format("%.2f MB", kb / 1024.0)
            } else {
                String.format("%.1f KB", kb)
            }
        }
}

enum class SystemSupportStatus(val label: String) {
    WORKING("Working"),
    PARTIALLY_SUPPORTED("Partially Supported"),
    UNSUPPORTED("Unsupported"),
    NOT_INSTALLED("Not Installed"),
    NOT_TESTED("Not Tested"),
    FAILED("Failed")
}

data class ReaDiagnostics(
    val status: SystemSupportStatus = SystemSupportStatus.PARTIALLY_SUPPORTED,
    val version: String = "0.4.1",
    val repository: String = "https://github.com/morluto/rea",
    val capabilities: List<String> = listOf(
        "Static APK Decompilation & Manifest Audit",
        "Smali & Bytecode Graph Resolution",
        "DEX Class & Symbol Extractor",
        "Android Architecture Reverse-Engineering",
        "Third-party Library & Dependency Fingerprinting"
    ),
    val pythonDetected: Boolean = false,
    val toolDiscoveryWorking: Boolean = true,
    val errorDetails: String? = null,
    val lastCheckTime: Long = System.currentTimeMillis()
)

data class CompatibilityReport(
    val claudeCliStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val claudeCliVersion: String = "Claude Code 0.2.29",
    val androidRuntimeStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val osVersion: String = "Android 15 (API 35)",
    val architecture: String = "arm64-v8a",
    val reaStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val reaVersion: String = "0.4.1-android-embedded",
    val reaIntegrationStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val authStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val selectedProvider: String = "Anthropic Official",
    val selectedModel: String = "claude-3-7-sonnet",
    val javaJdkStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val javaVersion: String = "OpenJDK 17.0.10",
    val androidSdkStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val gradleStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val gradleVersion: String = "Gradle 9.3.1",
    val flutterStatus: SystemSupportStatus = SystemSupportStatus.NOT_INSTALLED,
    val projectBuildSystem: String = "Gradle + AGP 9.1.1",
    val debugApkCapability: SystemSupportStatus = SystemSupportStatus.WORKING,
    val releaseApkCapability: SystemSupportStatus = SystemSupportStatus.WORKING,
    val aabCapability: SystemSupportStatus = SystemSupportStatus.WORKING,
    val logcatStatus: SystemSupportStatus = SystemSupportStatus.WORKING
)

data class PermissionRequest(
    val id: String,
    val title: String,
    val actionDetail: String,
    val isDestructive: Boolean = false,
    val onApprove: () -> Unit,
    val onDeny: () -> Unit
)
