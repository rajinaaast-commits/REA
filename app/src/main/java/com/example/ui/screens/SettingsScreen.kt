package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiProviderType
import com.example.model.PermissionMode
import com.example.ui.EvaWorkstationViewModel
import com.example.ui.components.WorkstationCard
import com.example.ui.theme.*

@Composable
fun SettingsScreen(viewModel: EvaWorkstationViewModel) {
    val providerConfig by viewModel.claudeEngine.providerConfig.collectAsState()
    val permissionMode by viewModel.claudeEngine.permissionMode.collectAsState()
    val currentProject by viewModel.projectManager.currentProject.collectAsState()

    var selectedProviderType by remember(providerConfig.providerType) { mutableStateOf(providerConfig.providerType) }
    var baseUrlInput by remember(providerConfig.baseUrl) { mutableStateOf(providerConfig.baseUrl) }
    var apiKeyInput by remember(providerConfig.apiKey) { mutableStateOf(providerConfig.apiKey) }
    var customModelInput by remember(providerConfig.customModel) { mutableStateOf(providerConfig.customModel) }
    var showApiKey by remember { mutableStateOf(false) }
    var saveFeedback by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "AI CONNECT & PROVIDER SETTINGS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        // Section 1: AI Connect & Custom Provider (Localhost / Ollama / LM Studio / Anthropic)
        item {
            WorkstationCard(
                backgroundColor = DarkSurfaceElevated,
                borderColor = CyanAccent.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI Connect Engine",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Connect Claude Code to Localhost or Custom Gateway",
                            fontSize = 12.sp,
                            color = CyanAccent
                        )
                    }

                    // Live Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (providerConfig.lastTestSuccess == true) EmeraldSuccessMuted else DarkSurface)
                            .border(1.dp, if (providerConfig.lastTestSuccess == true) EmeraldSuccess else DarkSurfaceBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = providerConfig.connectionStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (providerConfig.lastTestSuccess == true) EmeraldSuccess else CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Provider Mode Selection Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Pair(AiProviderType.CUSTOM_LOCAL, "Localhost / Custom"),
                        Pair(AiProviderType.ANTHROPIC_OFFICIAL, "Anthropic API")
                    ).forEach { (type, label) ->
                        val isSelected = selectedProviderType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyanAccentMuted else Color.Transparent)
                                .border(1.dp, if (isSelected) CyanAccent else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { selectedProviderType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanAccentLight else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Base URL
                Text(
                    text = "Base URL (Localhost Supported)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = baseUrlInput,
                    onValueChange = {
                        baseUrlInput = it
                        saveFeedback = false
                    },
                    placeholder = { Text("http://localhost:11434/v1 or http://10.0.2.2:11434/v1", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Localhost Chips
                Text(
                    text = "Quick Presets:",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Pair("Ollama (Localhost)", "http://localhost:11434/v1"),
                        Pair("Emulator Host", "http://10.0.2.2:11434/v1"),
                        Pair("LM Studio", "http://localhost:1234/v1"),
                        Pair("vLLM / LiteLLM", "http://localhost:8000/v1")
                    ).forEach { (label, url) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    baseUrlInput = url
                                    saveFeedback = false
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(label, fontSize = 10.sp, color = CyanAccent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Custom Model Name
                Text(
                    text = "Custom Model Name",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = customModelInput,
                    onValueChange = {
                        customModelInput = it
                        saveFeedback = false
                    },
                    placeholder = { Text("e.g. claude-3-7-sonnet, deepseek-r1, qwen2.5-coder", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Quick Model Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("claude-3-7-sonnet", "claude-3-5-sonnet", "deepseek-r1", "qwen2.5-coder", "llama3.3").forEach { model ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    customModelInput = model
                                    saveFeedback = false
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(model, fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. API Key (Optional for Localhost/Ollama)
                Text(
                    text = "API Key (Optional for local Ollama/LM Studio)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        saveFeedback = false
                    },
                    placeholder = { Text("Enter key or leave blank for local server", color = TextMuted, fontSize = 12.sp) },
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Test Connection & Save Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.testAiProviderConnection() },
                        enabled = !providerConfig.isTesting,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (providerConfig.isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CyanAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pinging...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Localhost", fontSize = 12.sp, color = CyanAccent)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.saveAiProvider(
                                providerType = selectedProviderType,
                                baseUrl = baseUrlInput,
                                apiKey = apiKeyInput,
                                customModel = customModelInput
                            )
                            saveFeedback = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save & Connect", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (saveFeedback) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ Provider settings applied. Claude Code will route requests through $baseUrlInput with model $customModelInput.",
                        fontSize = 11.sp,
                        color = EmeraldSuccess
                    )
                }

                if (providerConfig.lastErrorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Notice: ${providerConfig.lastErrorMessage}",
                        fontSize = 11.sp,
                        color = AmberWarning
                    )
                }
            }
        }

        // Section 2: Permission Preferences
        item {
            WorkstationCard {
                Text(
                    text = "Permission & Safeguard Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Controls how Claude Code executes file modifications and shell commands.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                PermissionMode.values().forEach { mode ->
                    val isSelected = permissionMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) DarkSurfaceElevated else Color.Transparent)
                            .clickable { viewModel.claudeEngine.setPermissionMode(mode) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanAccent else TextPrimary
                            )
                            Text(
                                text = mode.description,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.claudeEngine.setPermissionMode(mode) },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                        )
                    }
                }
            }
        }

        // Section 3: Official REA & System Information
        item {
            WorkstationCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Official REA Pre-installation",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "morluto/rea • rea.tools",
                            fontSize = 12.sp,
                            color = PurpleRea
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldSuccessMuted)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Pre-installed ✓", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All reverse-engineering components from https://github.com/morluto/rea and https://rea.tools are pre-installed with zero manual setup required. JADX decompiler, native binary symbol analysis, evidence ledger, and Claude Code MCP bridge are active.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Project Workspace Root:",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = currentProject?.path ?: "None",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            }
        }
    }
}
