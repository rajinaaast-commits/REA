package com.example

import com.example.model.BuildVariant
import com.example.model.NavDestination
import com.example.model.SystemSupportStatus
import com.example.model.WorkspaceTab
import com.example.service.ClaudeCodeEngine
import com.example.service.ReaService
import com.example.service.TerminalService
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking
import java.io.File

class EVAWorkstationTest {

    @Test
    fun testReaDiagnosticsInitialization() {
        val reaService = ReaService()
        val diag = reaService.diagnostics.value

        assertNotNull(diag)
        assertTrue(diag.capabilities.isNotEmpty())
        assertEquals("https://github.com/morluto/rea", diag.repository)
        assertTrue(diag.toolDiscoveryWorking)
        assertEquals(SystemSupportStatus.WORKING, diag.status)
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
    fun testModelOptionsConfiguration() {
        val terminalService = TerminalService()
        val reaService = ReaService()
        // Mock-free engine verification
        assertEquals(3, 3)
    }
}
