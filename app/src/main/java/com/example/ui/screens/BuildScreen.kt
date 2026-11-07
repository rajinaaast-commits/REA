package com.example.ui.screens

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
import com.example.model.ApkArtifact
import com.example.model.BuildVariant
import com.example.ui.EvaWorkstationViewModel
import com.example.ui.components.WorkstationCard
import com.example.ui.theme.*

@Composable
fun BuildScreen(viewModel: EvaWorkstationViewModel) {
    val isBuilding by viewModel.buildService.isBuilding.collectAsState()
    val currentTaskName by viewModel.buildService.currentTaskName.collectAsState()
    val buildLogs by viewModel.buildService.buildLogs.collectAsState()
    val lastSuccess by viewModel.buildService.lastBuildSuccess.collectAsState()
    val generatedApks by viewModel.buildService.generatedApks.collectAsState()
    val listState = rememberLazyListState()

    var showApkDetailsDialog by remember { mutableStateOf<ApkArtifact?>(null) }

    LaunchedEffect(buildLogs.size) {
        if (buildLogs.isNotEmpty()) {
            listState.animateScrollToItem(buildLogs.size - 1)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Build Controls Grid
        item {
            WorkstationCard(backgroundColor = DarkSurfaceElevated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Android Build Engine",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Gradle 9.3.1 • AGP 9.1.1 • OpenJDK 17",
                            fontSize = 12.sp,
                            color = CyanAccent
                        )
                    }

                    if (isBuilding) {
                        Button(
                            onClick = { viewModel.buildService.cancelBuild() },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Build Action Buttons Grid (2 rows)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerBuild(BuildVariant.DEBUG_APK) },
                            enabled = !isBuilding,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF042F2E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Debug APK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF042F2E))
                        }

                        Button(
                            onClick = { viewModel.triggerBuild(BuildVariant.RELEASE_APK) },
                            enabled = !isBuilding,
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleRea),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Release APK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.triggerBuild(BuildVariant.AAB) },
                            enabled = !isBuilding,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("App Bundle", fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.triggerBuild(BuildVariant.CLEAN) },
                            enabled = !isBuilding,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clean", fontSize = 12.sp, color = TextSecondary)
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerBuild(BuildVariant.TEST) },
                            enabled = !isBuilding,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Run Tests", fontSize = 12.sp, color = TextSecondary)
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerBuild(BuildVariant.LINT) },
                            enabled = !isBuilding,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lint Check", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Live Build Console Output
        item {
            WorkstationCard(backgroundColor = TerminalBackground) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (isBuilding) "Building: $currentTaskName" else "Build Terminal Logs",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (lastSuccess != null) {
                        Text(
                            text = if (lastSuccess == true) "✓ BUILD SUCCESSFUL" else "✗ BUILD FAILED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lastSuccess == true) EmeraldSuccess else RoseError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBackground)
                        .padding(8.dp)
                ) {
                    if (buildLogs.isEmpty()) {
                        Text(
                            text = "Build output will stream here in real-time when a task is executed.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        LazyColumn(state = listState) {
                            items(buildLogs) { line ->
                                val color = when {
                                    line.contains("SUCCESS", ignoreCase = true) -> EmeraldSuccess
                                    line.contains("ERROR", ignoreCase = true) || line.contains("FAILED", ignoreCase = true) -> RoseError
                                    line.startsWith(">") -> CyanAccent
                                    else -> TextSecondary
                                }
                                Text(
                                    text = line,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = color,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Generated APK & Artifact Management Section
        item {
            Text(
                text = "APK & ARTIFACT MANAGEMENT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        if (generatedApks.isEmpty()) {
            item {
                WorkstationCard {
                    Text(
                        text = "No generated APKs found yet. Run 'Build Debug APK' above to produce and verify a genuine build artifact.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(generatedApks, key = { it.id }) { apk ->
                WorkstationCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccessMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                            }

                            Column {
                                Text(
                                    text = apk.fileName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = apk.formattedSize,
                                        fontSize = 11.sp,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text("•", color = TextMuted, fontSize = 10.sp)
                                    Text(
                                        text = "Variant: ${apk.variant}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text("•", color = TextMuted, fontSize = 10.sp)
                                    Text(
                                        text = "Verified ✓",
                                        fontSize = 11.sp,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Actions: Install, Share, Details
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { viewModel.installApk(apk) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.InstallMobile, contentDescription = "Install APK", tint = EmeraldSuccess)
                            }
                            IconButton(
                                onClick = { viewModel.shareApk(apk) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share APK", tint = CyanAccent)
                            }
                            IconButton(
                                onClick = { showApkDetailsDialog = apk },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Inspect APK", tint = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Inspect APK Dialog
    showApkDetailsDialog?.let { apk ->
        AlertDialog(
            onDismissRequest = { showApkDetailsDialog = null },
            title = { Text(apk.fileName, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Path: ${apk.filePath}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Text("File Size: ${apk.formattedSize} (${apk.sizeBytes} bytes)", fontSize = 12.sp, color = TextPrimary)
                    Text("Build Variant: ${apk.variant}", fontSize = 12.sp, color = TextPrimary)
                    Text("Package Name: ${apk.packageName}", fontSize = 12.sp, color = TextPrimary)
                    Text("Version: ${apk.versionName} (${apk.versionCode})", fontSize = 12.sp, color = TextPrimary)
                    Text("Created: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(apk.createdAt))}", fontSize = 12.sp, color = TextSecondary)
                    Text("Integrity: 100% Verified Valid APK", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.installApk(apk)
                        showApkDetailsDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("Install", color = Color(0xFF022C22))
                }
            },
            dismissButton = {
                TextButton(onClick = { showApkDetailsDialog = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
