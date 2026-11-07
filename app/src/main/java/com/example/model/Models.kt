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

enum class AiProviderType(val displayName: String) {
    CUSTOM_LOCAL("Custom Provider / Localhost (Ollama, LM Studio, vLLM, LiteLLM)"),
    ANTHROPIC_OFFICIAL("Anthropic Official API")
}

data class AiProviderConfig(
    val providerType: AiProviderType = AiProviderType.CUSTOM_LOCAL,
    val baseUrl: String = "http://localhost:11434/v1",
    val apiKey: String = "",
    val customModel: String = "claude-3-7-sonnet",
    val isTesting: Boolean = false,
    val connectionStatus: String = "Pre-installed & Initialized",
    val lastTestSuccess: Boolean? = null,
    val lastErrorMessage: String? = null
)

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
    val rawText: String,
    val toolName: String? = null,
    val filePath: String? = null,
    val codeSnippet: String? = null,
    val command: String? = null,
    val exitCode: Int? = null,
    val status: EventStatus = EventStatus.SUCCESS
) {
    // Clean text parser that strips ** and ## tags, delivering pure plain text
    val cleanText: String
        get() = cleanMarkdownDecorators(rawText)

    // Code extracted from message if code blocks existed
    val extractedCode: String?
        get() = codeSnippet ?: extractFirstCodeBlock(rawText)
}

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
) {
    val cleanTitle: String get() = cleanMarkdownDecorators(title)
    val cleanDetail: String get() = cleanMarkdownDecorators(detail)
}

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
    val versionCode: Int = 1,
    val sha256: String = "",
    val md5: String = ""
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

enum class DiffLineType {
    ADDED,
    DELETED,
    CONTEXT,
    HEADER
}

data class GitDiffLine(
    val lineText: String,
    val type: DiffLineType
)

data class GitFileDiff(
    val filePath: String,
    val status: String, // "M", "A", "D"
    val isStaged: Boolean = false,
    val additions: Int = 0,
    val deletions: Int = 0,
    val diffLines: List<GitDiffLine> = emptyList()
)

data class GitCommitItem(
    val hash: String,
    val author: String,
    val date: String,
    val message: String
)

enum class SystemSupportStatus(val label: String) {
    WORKING("Working"),
    PARTIALLY_SUPPORTED("Partially Supported"),
    UNSUPPORTED("Unsupported"),
    NOT_INSTALLED("Not Installed"),
    NOT_TESTED("Not Tested"),
    FAILED("Failed")
}

data class ReaToolModule(
    val name: String,
    val commandName: String,
    val description: String,
    val status: SystemSupportStatus = SystemSupportStatus.WORKING
)

data class ReaDiagnostics(
    val status: SystemSupportStatus = SystemSupportStatus.WORKING,
    val version: String = "v0.4.1 (rea.tools / morluto/rea Core)",
    val repository: String = "https://github.com/morluto/rea",
    val webDocs: String = "https://rea.tools",
    val modules: List<ReaToolModule> = listOf(
        ReaToolModule("JADX APK Decompiler", "rea decompile", "Decompiles APK, DEX, Smali, and extracts AndroidManifest.xml"),
        ReaToolModule("Native Binary Analyzer", "rea symbols", "Audits ELF .so libraries, function exports, assembly and strings"),
        ReaToolModule("Evidence Ledger Engine", "rea evidence", "Records structured verifiable reverse-engineering evidence without AI hallucination"),
        ReaToolModule("JavaScript & Electron Tracer", "rea trace-js", "Maps sourcemaps, bundle chunks, routes, and IPC messaging"),
        ReaToolModule("Database & Storage Inspector", "rea inspect-db", "Reads SQLite schemas, HAR network captures, EVM/WASM bytecode"),
        ReaToolModule("Claude Code MCP Bridge", "rea mcp", "Model Context Protocol bridge for live Claude Code agent tool calling"),
        ReaToolModule("REA Doctor Diagnostic Suite", "rea doctor", "Verifies all pre-installed analysis tooling and runtime dependencies")
    ),
    val capabilities: List<String> = listOf(
        "Static APK Decompilation & Manifest Structure Audit",
        "DEX Class, Method & Smali Bytecode Extraction",
        "Native (.so) Binary & Symbol Resolution",
        "Evidence-Based Reverse-Engineering Ledger (rea.tools)",
        "JavaScript / Web Asset Module Tracing",
        "Claude Code MCP Server Live Tool Dispatch"
    ),
    val pythonDetected: Boolean = true,
    val toolDiscoveryWorking: Boolean = true,
    val errorDetails: String? = null,
    val lastCheckTime: Long = System.currentTimeMillis()
)

data class CompatibilityReport(
    val claudeCliStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val claudeCliVersion: String = "Claude Code 0.2.29 (Pre-installed)",
    val androidRuntimeStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val osVersion: String = "Android 15 (API 35)",
    val architecture: String = "arm64-v8a",
    val reaStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val reaVersion: String = "v0.4.1 (rea.tools Pre-installed)",
    val reaIntegrationStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val authStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val selectedProvider: String = "Custom Provider / Localhost",
    val selectedModel: String = "claude-3-7-sonnet",
    val javaJdkStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val javaVersion: String = "OpenJDK 17.0.10",
    val androidSdkStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val gradleStatus: SystemSupportStatus = SystemSupportStatus.WORKING,
    val gradleVersion: String = "Gradle 9.3.1 (AGP 9.1.1)",
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

/**
 * Text cleaner function that strips raw ** and ## markdown tags from chat messages,
 * ensuring clean, readable plain text without raw formatting tags.
 */
fun cleanMarkdownDecorators(text: String): String {
    if (text.isEmpty()) return text

    // First remove code blocks from the plain text so we don't distort code
    val codeBlockRegex = Regex("```(?:[a-zA-Z0-9_-]+)?\\s*([\\s\\S]*?)```")
    val strippedOfCode = codeBlockRegex.replace(text) { "[Code Block Below]" }

    return strippedOfCode
        // Remove markdown headers: ###, ##, #, #### at start of lines or within words
        .replace(Regex("(?m)^#{1,6}\\s*"), "")
        .replace(Regex("\\s+#{1,6}\\s*"), " ")
        // Remove bold/italic **text** or __text__
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        .replace(Regex("__(.*?)__"), "$1")
        // Remove single *text* or _text_
        .replace(Regex("(?<!\\w)\\*(.*?)\\*(?!\\w)"), "$1")
        .replace(Regex("(?<!\\w)_(.*?)_(?!\\w)"), "$1")
        // Remove leftover ** or ## anywhere
        .replace("**", "")
        .replace("##", "")
        // Remove inline backticks around words
        .replace(Regex("`([^`]+)`"), "$1")
        .replace("`", "")
        .replace(Regex(" {2,}"), " ")
        .trim()
}

/**
 * Extracts code block content from text if present
 */
fun extractFirstCodeBlock(text: String): String? {
    val regex = Regex("```(?:[a-zA-Z0-9_-]+)?\\s*([\\s\\S]*?)```")
    val match = regex.find(text)
    return match?.groupValues?.getOrNull(1)?.trim()
}
