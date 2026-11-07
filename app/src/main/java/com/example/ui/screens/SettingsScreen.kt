package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.ClaudeModelOption
import com.example.model.PermissionMode
import com.example.ui.EvaWorkstationViewModel
import com.example.ui.components.WorkstationCard
import com.example.ui.theme.*

@Composable
fun SettingsScreen(viewModel: EvaWorkstationViewModel) {
    val apiKey by viewModel.claudeEngine.apiKey.collectAsState()
    val selectedModel by viewModel.claudeEngine.selectedModel.collectAsState()
    val permissionMode by viewModel.claudeEngine.permissionMode.collectAsState()
    val currentProject by viewModel.projectManager.currentProject.collectAsState()

    var keyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var showKey by remember { mutableStateOf(false) }
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
                text = "WORKSTATION SETTINGS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        // Section 1: Claude Code Authentication
        item {
            WorkstationCard {
                Text(
                    text = "Claude Code Authentication",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Configure your official Anthropic API credential for Claude Code agentic operations. Keys are kept strictly local in memory and never logged.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Anthropic API Key (sk-ant-...)") },
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (saveFeedback) {
                        Text("✓ Key saved successfully", fontSize = 12.sp, color = EmeraldSuccess)
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.claudeEngine.setApiKey(keyInput.trim())
                            saveFeedback = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Key", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2: Model Selection
        item {
            WorkstationCard {
                Text(
                    text = "Active AI Model",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Select from models officially supported by Claude Code CLI.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                viewModel.claudeEngine.modelOptions.forEach { model ->
                    val isSelected = selectedModel.id == model.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) DarkSurfaceElevated else Color.Transparent)
                            .clickable { viewModel.claudeEngine.setModel(model) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = model.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanAccent else TextPrimary
                            )
                            Text(
                                text = "Context: ${model.contextWindow} • Thinking: ${if (model.supportsThinking) "Supported" else "Off"}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.claudeEngine.setModel(model) },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                        )
                    }
                }
            }
        }

        // Section 3: Permission Preferences
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

        // Section 4: Workspace Information
        item {
            WorkstationCard {
                Text(
                    text = "Project Workspace & REA Path",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current Project Root:",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = currentProject?.path ?: "None",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Official REA Repository:",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "https://github.com/morluto/rea",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = PurpleRea
                )
            }
        }
    }
}
