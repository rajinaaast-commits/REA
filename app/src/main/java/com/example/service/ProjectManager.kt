package com.example.service

import android.content.Context
import com.example.model.ProjectItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ProjectManager(private val context: Context) {
    private val _projects = MutableStateFlow<List<ProjectItem>>(emptyList())
    val projects: StateFlow<List<ProjectItem>> = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<ProjectItem?>(null)
    val currentProject: StateFlow<ProjectItem?> = _currentProject.asStateFlow()

    private val _selectedFile = MutableStateFlow<File?>(null)
    val selectedFile: StateFlow<File?> = _selectedFile.asStateFlow()

    private val _fileContent = MutableStateFlow<String>("")
    val fileContent: StateFlow<String> = _fileContent.asStateFlow()

    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

    init {
        initializeSampleProjects()
    }

    private fun initializeSampleProjects() {
        val rootDir = File(context.filesDir, "workspaces")
        if (!rootDir.exists()) rootDir.mkdirs()

        // 1. EVA BUILD CLI Core Android Workspace
        val evaProjectDir = File(rootDir, "eva-build-core")
        if (!evaProjectDir.exists()) {
            evaProjectDir.mkdirs()
            File(evaProjectDir, "src/main/java/com/example").mkdirs()
            File(evaProjectDir, "src/main/res/values").mkdirs()
            File(evaProjectDir, "src/main/AndroidManifest.xml").writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:label="EVA Build Core" android:icon="@mipmap/ic_launcher">
        <activity android:name=".MainActivity" android:exported="true" />
    </application>
</manifest>"""
            )
            File(evaProjectDir, "build.gradle.kts").writeText(
                """plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.evacore"
    compileSdk = 35
}"""
            )
            File(evaProjectDir, "src/main/java/com/example/MainActivity.kt").writeText(
                """package com.example.evacore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text("Welcome to EVA Workstation Core")
        }
    }
}"""
            )
            File(evaProjectDir, "CLAUDE.md").writeText(
                """# CLAUDE.md - Project Directives
- Build System: Gradle 9.3.1 with AGP 9.1.1
- Language: Kotlin 2.2.10 with Jetpack Compose
- Reverse-Engineering: Official REA integration (morluto/rea)
- Commands: gradle :app:assembleDebug, gradle :app:testDebugUnitTest"""
            )
        }

        // 2. REA Analysis Target Sample
        val reaSampleDir = File(rootDir, "rea-reverse-target")
        if (!reaSampleDir.exists()) {
            reaSampleDir.mkdirs()
            File(reaSampleDir, "app.dex").writeBytes("DEX_FORMAT_SAMPLE_HEADER_DATA_CLASS_REVERSE".toByteArray())
            File(reaSampleDir, "AndroidManifest.xml").writeText(
                """<manifest package="com.target.sample">
    <uses-permission android:name="android.permission.INTERNET"/>
    <application android:allowBackup="true" />
</manifest>"""
            )
            File(reaSampleDir, "README.md").writeText(
                """# REA Target Demo
This project demonstrates static APK decompilation and DEX symbol resolution using REA."""
            )
        }

        val projectList = listOf(
            ProjectItem(
                id = "proj-eva-1",
                name = "eva-build-core",
                path = evaProjectDir.absolutePath,
                isAndroidProject = true,
                buildSystem = "Gradle 9.3.1",
                gitBranch = "main",
                uncommittedChanges = 2
            ),
            ProjectItem(
                id = "proj-rea-2",
                name = "rea-reverse-target",
                path = reaSampleDir.absolutePath,
                isAndroidProject = false,
                buildSystem = "REA Decompile Target",
                gitBranch = "analysis",
                uncommittedChanges = 0
            )
        )

        _projects.value = projectList
        _currentProject.value = projectList.first()

        // Load default file into editor
        val defaultFile = File(evaProjectDir, "CLAUDE.md")
        if (defaultFile.exists()) {
            selectFile(defaultFile)
        }
    }

    fun selectProject(project: ProjectItem) {
        _currentProject.value = project
        val projectFolder = File(project.path)
        val files = projectFolder.listFiles()
        val firstTextFile = files?.firstOrNull { it.isFile && (it.name.endsWith(".md") || it.name.endsWith(".kt") || it.name.endsWith(".kts") || it.name.endsWith(".xml")) }
        if (firstTextFile != null) {
            selectFile(firstTextFile)
        }
    }

    fun selectFile(file: File) {
        if (!file.exists() || !file.isFile) return
        _selectedFile.value = file
        try {
            _fileContent.value = file.readText()
            _isDirty.value = false
        } catch (e: Exception) {
            _fileContent.value = "// Error reading file: ${e.localizedMessage}"
        }
    }

    fun updateEditorContent(newContent: String) {
        _fileContent.value = newContent
        _isDirty.value = true
    }

    suspend fun saveCurrentFile(): Boolean = withContext(Dispatchers.IO) {
        val file = _selectedFile.value ?: return@withContext false
        return@withContext try {
            file.writeText(_fileContent.value)
            _isDirty.value = false
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createNewFile(parentDir: File, fileName: String, initialContent: String = ""): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val newFile = File(parentDir, fileName)
            if (newFile.exists()) return@withContext false
            newFile.parentFile?.mkdirs()
            newFile.writeText(initialContent)
            selectFile(newFile)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteFile(file: File): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val deleted = file.deleteRecursively()
            if (deleted && _selectedFile.value?.absolutePath == file.absolutePath) {
                _selectedFile.value = null
                _fileContent.value = ""
                _isDirty.value = false
            }
            deleted
        } catch (e: Exception) {
            false
        }
    }
}
