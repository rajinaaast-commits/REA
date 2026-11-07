package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.EvaWorkstationViewModel
import com.example.ui.components.CodeSnippetView
import com.example.ui.components.WorkstationCard
import com.example.ui.theme.*

@Composable
fun WorkspaceScreen(viewModel: EvaWorkstationViewModel) {
    val activeTab by viewModel.workspaceTab.collectAsState()
    val isStreaming by viewModel.claudeEngine.isStreaming.collectAsState()
    val promptInput by viewModel.promptInput.collectAsState()
    val events by viewModel.claudeEngine.events.collectAsState()
    val planSteps by viewModel.claudeEngine.planSteps.collectAsState()
    val terminalLogs by viewModel.terminalService.logs.collectAsState()
    val isTerminalExecuting by viewModel.terminalService.isExecuting.collectAsState()
    val terminalInput by viewModel.terminalInput.collectAsState()
    val currentProject by viewModel.projectManager.currentProject.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tab Segmented Control (Plan, Activity, Terminal)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceElevated)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WorkspaceTab.values().forEach { tab ->
                val isSelected = activeTab == tab
                val badgeCount = when (tab) {
                    WorkspaceTab.PLAN -> planSteps.count { it.status == PlanStatus.IN_PROGRESS || it.status == PlanStatus.PENDING }
                    WorkspaceTab.ACTIVITY -> events.size
                    WorkspaceTab.TERMINAL -> if (isTerminalExecuting) 1 else 0
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DarkSurface else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) DarkSurfaceBorder else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.setWorkspaceTab(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                        if (badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyanAccent else DarkSurfaceBorder)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$badgeCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF042F2E) else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                WorkspaceTab.PLAN -> PlanPanel(planSteps = planSteps, prompt = promptInput)
                WorkspaceTab.ACTIVITY -> ActivityPanel(events = events)
                WorkspaceTab.TERMINAL -> TerminalPanel(
                    logs = terminalLogs,
                    isExecuting = isTerminalExecuting,
                    input = terminalInput,
                    onInputChange = { viewModel.setTerminalInput(it) },
                    onSubmit = { viewModel.submitTerminalCommand(it) },
                    onCancel = { viewModel.terminalService.cancelActiveCommand() },
                    onClear = { viewModel.terminalService.clearLogs() }
                )
            }
        }

        // Bottom AI Prompt Console (accessible on all tabs)
        Surface(
            color = DarkSurface,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Quick suggestions / chips when input empty
                if (promptInput.isEmpty() && !isStreaming) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Audit Android APK with REA",
                            "Build Debug APK with AGP 9.1.1",
                            "Explore project structure"
                        ).forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setPromptInput(suggestion) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { viewModel.setPromptInput(it) },
                        placeholder = {
                            Text(
                                "Ask Claude Code to build, refactor, or test...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (isStreaming) {
                        IconButton(
                            onClick = { viewModel.claudeEngine.cancelActiveSession() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(RoseError)
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Cancel Execution",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.submitPrompt() },
                            enabled = promptInput.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (promptInput.isNotBlank()) CyanAccent else DarkSurfaceElevated)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send to Claude Code",
                                tint = if (promptInput.isNotBlank()) Color(0xFF042F2E) else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanPanel(planSteps: List<PlanStep>, prompt: String) {
    if (planSteps.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Plan Mode Standby",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Enter a coding task prompt below to generate an autonomous, multi-step execution plan with progress tracking and tool execution.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                WorkstationCard(backgroundColor = DarkSurfaceElevated) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Claude Code Plan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            val completed = planSteps.count { it.status == PlanStatus.COMPLETED }
                            Text(
                                text = "$completed of ${planSteps.size} tasks finished",
                                fontSize = 12.sp,
                                color = CyanAccent
                            )
                        }
                        CircularProgressIndicator(
                            progress = { planSteps.count { it.status == PlanStatus.COMPLETED }.toFloat() / planSteps.size },
                            modifier = Modifier.size(28.dp),
                            color = CyanAccent,
                            trackColor = DarkSurfaceBorder
                        )
                    }
                }
            }

            items(planSteps) { step ->
                val (statusIcon, statusColor, statusBg) = when (step.status) {
                    PlanStatus.PENDING -> Triple(Icons.Default.Schedule, TextMuted, DarkSurfaceElevated)
                    PlanStatus.IN_PROGRESS -> Triple(Icons.Default.Sync, CyanAccent, CyanAccentMuted)
                    PlanStatus.COMPLETED -> Triple(Icons.Default.CheckCircle, EmeraldSuccess, EmeraldSuccessMuted)
                    PlanStatus.FAILED -> Triple(Icons.Default.Cancel, RoseError, RoseErrorMuted)
                }

                WorkstationCard(backgroundColor = DarkSurface) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(statusBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Task ${step.id}: ${step.cleanTitle}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (step.cleanDetail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = step.cleanDetail,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityPanel(events: List<ActivityEvent>) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(events, key = { it.id }) { event ->
            ActivityEventCard(event = event)
        }
    }
}

@Composable
fun ActivityEventCard(event: ActivityEvent) {
    val (icon, tint) = when (event.type) {
        EventType.USER_PROMPT -> Pair(Icons.Default.Person, CyanAccent)
        EventType.AGENT_RESPONSE -> Pair(Icons.Default.SmartToy, CyanAccentLight)
        EventType.PLAN_UPDATE -> Pair(Icons.Default.AccountTree, PurpleRea)
        EventType.TOOL_CALL -> Pair(Icons.Default.Build, AmberWarning)
        EventType.TOOL_RESULT -> Pair(Icons.Default.Code, EmeraldSuccess)
        EventType.FILE_READ -> Pair(Icons.Default.Description, CyanAccent)
        EventType.FILE_WRITE -> Pair(Icons.Default.Edit, EmeraldSuccess)
        EventType.TERMINAL_COMMAND -> Pair(Icons.Default.Terminal, Color(0xFF38BDF8))
        EventType.PERMISSION_REQUEST -> Pair(Icons.Default.Security, AmberWarning)
        EventType.BUILD_EVENT -> Pair(Icons.Default.Hardware, EmeraldSuccess)
        EventType.ERROR -> Pair(Icons.Default.Error, RoseError)
    }

    WorkstationCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date(event.timestamp)),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                if (event.cleanText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.cleanText,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                val code = event.extractedCode
                if (code != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeSnippetView(code = code)
                }
            }
        }
    }
}

@Composable
fun TerminalPanel(
    logs: List<TerminalLog>,
    isExecuting: Boolean,
    input: String,
    onInputChange: (String) -> Unit,
    onSubmit: (String?) -> Unit,
    onCancel: () -> Unit,
    onClear: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Terminal Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "sh - Android Workspace",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        val fullLog = logs.joinToString("\n") { it.text }
                        clipboardManager.setText(AnnotatedString(fullLog))
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Log", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Quick Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ls -la", "git status", "df -h", "uname -m", "ps").forEach { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                        .clickable { onSubmit(cmd) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cmd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                }
            }
        }

        // Terminal Log Output
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                val color = when {
                    log.isCommand -> CyanAccent
                    log.isError -> RoseError
                    else -> TerminalText
                }

                Text(
                    text = log.text,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Terminal Input Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$",
                fontFamily = FontFamily.Monospace,
                color = CyanAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text("Enter shell command...", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            if (isExecuting) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Stop", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = { onSubmit(null) },
                    enabled = input.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Run", fontSize = 12.sp, color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
