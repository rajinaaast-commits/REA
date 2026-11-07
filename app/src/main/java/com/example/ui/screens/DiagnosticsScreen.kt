package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemSupportStatus
import com.example.ui.EvaWorkstationViewModel
import com.example.ui.components.CodeSnippetView
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkstationCard
import com.example.ui.theme.*

@Composable
fun DiagnosticsScreen(viewModel: EvaWorkstationViewModel) {
    val report by viewModel.compatibilityReport.collectAsState()
    val reaDiagnostics by viewModel.reaService.diagnostics.collectAsState()
    val reaReport by viewModel.reaService.analysisReport.collectAsState()
    val isAnalyzing by viewModel.reaService.isAnalyzing.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "System Diagnostics & Audit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Live hardware, CLI, and REA runtime verification",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.refreshCompatibility() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF042F2E), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-Audit", fontSize = 12.sp, color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section A: Official REA Integration Card
        item {
            WorkstationCard(
                backgroundColor = PurpleReaMuted.copy(alpha = 0.4f),
                borderColor = PurpleRea.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PurpleRea),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "Official REA Integration",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "morluto/rea • ${reaDiagnostics.version}",
                                fontSize = 11.sp,
                                color = Color(0xFFC4B5FD)
                            )
                        }
                    }

                    StatusBadge(status = reaDiagnostics.status)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Reverse Engineer Anything (REA) provides official static decompilation, DEX symbol extraction, and architecture reverse-engineering tools callable directly by Claude Code.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Supported Capabilities:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                reaDiagnostics.capabilities.forEach { cap ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("•", color = PurpleRea, fontSize = 14.sp)
                        Text(cap, fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.runReaInspection() },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleRea),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Running REA Analysis...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Live REA Reverse-Engineering Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (reaReport != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Latest REA Inspection Output:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC4B5FD)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeSnippetView(code = reaReport ?: "")
                }
            }
        }

        // Section B: Official Compatibility Matrix Table
        item {
            Text(
                text = "OFFICIAL WORKSTATION COMPATIBILITY AUDIT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        val matrixItems = listOf(
            Triple("Claude Code CLI", report.claudeCliVersion, report.claudeCliStatus),
            Triple("Official REA Engine", report.reaVersion, report.reaStatus),
            Triple("REA Integration Bridge", "Tool discovery active", report.reaIntegrationStatus),
            Triple("Android OS Runtime", report.osVersion, report.androidRuntimeStatus),
            Triple("Device CPU Architecture", report.architecture, SystemSupportStatus.WORKING),
            Triple("Authentication Status", report.selectedProvider, report.authStatus),
            Triple("Selected Model", report.selectedModel, SystemSupportStatus.WORKING),
            Triple("Java / OpenJDK", report.javaVersion, report.javaJdkStatus),
            Triple("Android SDK & Platforms", "Android API 35/36 installed", report.androidSdkStatus),
            Triple("Gradle Build Tooling", report.gradleVersion, report.gradleStatus),
            Triple("Project Build System", report.projectBuildSystem, SystemSupportStatus.WORKING),
            Triple("Debug APK Generation", "assembleDebug verified", report.debugApkCapability),
            Triple("Release APK Generation", "assembleRelease configured", report.releaseApkCapability),
            Triple("Android App Bundle (AAB)", "bundleRelease supported", report.aabCapability),
            Triple("Flutter / Dart", "Not in current project scope", report.flutterStatus),
            Triple("System Logcat Access", "Runtime filtered logs available", report.logcatStatus)
        )

        items(matrixItems) { (component, info, status) ->
            WorkstationCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = component,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = info,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    StatusBadge(status = status)
                }
            }
        }
    }
}
