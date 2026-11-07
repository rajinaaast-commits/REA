package com.example.service

import com.example.model.TerminalLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.UUID

class TerminalService {
    private val _logs = MutableStateFlow<List<TerminalLog>>(emptyList())
    val logs: StateFlow<List<TerminalLog>> = _logs.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private var activeProcess: Process? = null

    init {
        appendLog("EVA BUILD CLI Terminal Ready. Type commands or use action chips below.", isError = false)
        appendLog("Environment: Android Sandbox (ProcessBuilder /system/bin/sh)", isError = false)
    }

    fun appendLog(text: String, isError: Boolean = false, isCommand: Boolean = false) {
        val entry = TerminalLog(
            id = UUID.randomUUID().toString(),
            text = text,
            isError = isError,
            isCommand = isCommand
        )
        _logs.value = _logs.value + entry
    }

    suspend fun executeCommand(command: String, workingDir: File? = null): Int = withContext(Dispatchers.IO) {
        if (command.isBlank()) return@withContext 0

        _isExecuting.value = true
        appendLog("$ $command", isError = false, isCommand = true)

        try {
            val shellPath = if (File("/system/bin/sh").exists()) "/system/bin/sh" else "/bin/sh"
            val processBuilder = ProcessBuilder(shellPath, "-c", command)
            if (workingDir != null && workingDir.exists()) {
                processBuilder.directory(workingDir)
            }
            processBuilder.redirectErrorStream(false)

            val process = processBuilder.start()
            activeProcess = process

            // Read standard output
            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            // Read standard error
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            val stdoutJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).run {
                var line: String?
                while (stdoutReader.readLine().also { line = it } != null) {
                    line?.let { appendLog(it, isError = false) }
                }
            }

            var errLine: String?
            while (stderrReader.readLine().also { errLine = it } != null) {
                errLine?.let { appendLog(it, isError = true) }
            }

            val exitCode = process.waitFor()
            activeProcess = null
            _isExecuting.value = false

            if (exitCode == 0) {
                appendLog("[Process completed with exit code 0]", isError = false)
            } else {
                appendLog("[Process exited with code $exitCode]", isError = true)
            }

            return@withContext exitCode
        } catch (e: Exception) {
            activeProcess = null
            _isExecuting.value = false
            appendLog("Execution failed: ${e.localizedMessage}", isError = true)
            return@withContext -1
        }
    }

    fun cancelActiveCommand() {
        activeProcess?.let {
            try {
                it.destroy()
                appendLog("[Process terminated by user]", isError = true)
            } catch (ignored: Exception) {}
            activeProcess = null
            _isExecuting.value = false
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
        appendLog("Terminal cleared.", isError = false)
    }
}
