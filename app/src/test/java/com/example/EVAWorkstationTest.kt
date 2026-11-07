package com.example

import com.example.model.AiProviderConfig
import com.example.model.AiProviderType
import com.example.model.SystemSupportStatus
import com.example.model.cleanMarkdownDecorators
import com.example.model.extractFirstCodeBlock
import com.example.service.ReaService
import com.example.service.TerminalService
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking

class EVAWorkstationTest {

    @Test
    fun testReaDiagnosticsInitialization() {
        val reaService = ReaService()
        val diag = reaService.diagnostics.value

        assertNotNull(diag)
        assertTrue(diag.capabilities.isNotEmpty())
        assertEquals("https://github.com/morluto/rea", diag.repository)
        assertEquals("https://rea.tools", diag.webDocs)
        assertTrue(diag.toolDiscoveryWorking)
        assertEquals(SystemSupportStatus.WORKING, diag.status)
        assertTrue(diag.modules.any { it.commandName == "rea decompile" })
        assertTrue(diag.modules.any { it.commandName == "rea doctor" })
    }

    @Test
    fun testReaDoctorExecution() = runBlocking {
        val reaService = ReaService()
        val doctorReport = reaService.runDoctorAudit()
        assertNotNull(doctorReport)
        assertTrue(doctorReport.contains("rea.tools"))
        assertTrue(doctorReport.contains("PRE-INSTALLED"))
        assertTrue(doctorReport.contains("100% operational"))
    }

    @Test
    fun testCleanMarkdownDecorators() {
        val input = "### Title with **bold text** and ## subsection with `inlineCode`"
        val cleaned = cleanMarkdownDecorators(input)
        assertFalse(cleaned.contains("**"))
        assertFalse(cleaned.contains("##"))
        assertFalse(cleaned.contains("`"))
        assertEquals("Title with bold text and subsection with inlineCode", cleaned)
    }

    @Test
    fun testExtractCodeBlock() {
        val input = """
            Here is the implementation:
            ```kotlin
            fun hello(): String = "world"
            ```
            End of text.
        """.trimIndent()

        val extracted = extractFirstCodeBlock(input)
        assertNotNull(extracted)
        assertTrue(extracted!!.contains("fun hello(): String = \"world\""))

        val cleaned = cleanMarkdownDecorators(input)
        assertFalse(cleaned.contains("```"))
    }

    @Test
    fun testAiProviderLocalhostConfig() {
        val config = AiProviderConfig(
            providerType = AiProviderType.CUSTOM_LOCAL,
            baseUrl = "http://localhost:11434/v1",
            apiKey = "local-key",
            customModel = "claude-3-7-sonnet"
        )

        assertEquals("http://localhost:11434/v1", config.baseUrl)
        assertEquals("claude-3-7-sonnet", config.customModel)
        assertEquals("local-key", config.apiKey)
    }

    @Test
    fun testTerminalServiceExecution() = runBlocking {
        val terminalService = TerminalService()
        val exitCode = terminalService.executeCommand("echo 'Hello EVA BUILD CLI'")
        assertEquals(0, exitCode)
        val logs = terminalService.logs.value
        assertTrue(logs.any { it.text.contains("Hello EVA BUILD CLI") })
    }

    @Test
    fun testSha256AndMd5HashComputation() {
        val tempFile = java.io.File.createTempFile("test-apk", ".apk")
        tempFile.writeText("EVA_BUILD_CLI_TEST_BYTE_STREAM")

        val digestSha = java.security.MessageDigest.getInstance("SHA-256")
        val sha256 = tempFile.inputStream().use { input ->
            val buf = ByteArray(1024)
            var read: Int
            while (input.read(buf).also { read = it } != -1) {
                digestSha.update(buf, 0, read)
            }
            digestSha.digest().joinToString("") { "%02x".format(it) }
        }

        assertNotNull(sha256)
        assertEquals(64, sha256.length)

        val digestMd5 = java.security.MessageDigest.getInstance("MD5")
        val md5 = tempFile.inputStream().use { input ->
            val buf = ByteArray(1024)
            var read: Int
            while (input.read(buf).also { read = it } != -1) {
                digestMd5.update(buf, 0, read)
            }
            digestMd5.digest().joinToString("") { "%02x".format(it) }
        }

        assertNotNull(md5)
        assertEquals(32, md5.length)
        tempFile.delete()
    }

    @Test
    fun testGitDiffModels() {
        val diffLine = com.example.model.GitDiffLine("+ val x = 1", com.example.model.DiffLineType.ADDED)
        assertEquals(com.example.model.DiffLineType.ADDED, diffLine.type)

        val fileDiff = com.example.model.GitFileDiff(
            filePath = "app/build.gradle.kts",
            status = "M",
            isStaged = true,
            additions = 5,
            deletions = 1,
            diffLines = listOf(diffLine)
        )
        assertTrue(fileDiff.isStaged)
        assertEquals(5, fileDiff.additions)
        assertEquals("M", fileDiff.status)
    }
}
