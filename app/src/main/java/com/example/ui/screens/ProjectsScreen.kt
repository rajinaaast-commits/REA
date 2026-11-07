package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectItem
import com.example.ui.EvaWorkstationViewModel
import com.example.ui.components.WorkstationCard
import com.example.ui.theme.*
import java.io.File

@Composable
fun ProjectsScreen(viewModel: EvaWorkstationViewModel) {
    val projects by viewModel.projectManager.projects.collectAsState()
    val currentProject by viewModel.projectManager.currentProject.collectAsState()
    val selectedFile by viewModel.projectManager.selectedFile.collectAsState()
    val fileContent by viewModel.projectManager.fileContent.collectAsState()
    val isDirty by viewModel.projectManager.isDirty.collectAsState()

    var showProjectSwitcher by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var newFileName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Project Selector & Git Header
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showProjectSwitcher = !showProjectSwitcher }
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Text(
                        text = currentProject?.name ?: "Select Project",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Icon(
                        if (showProjectSwitcher) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🌿 ${currentProject?.gitBranch ?: "main"}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = CyanAccent
                        )
                    }

                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New File", tint = TextPrimary)
                    }
                }
            }
        }

        // Project Switcher Dropdown
        if (showProjectSwitcher) {
            Surface(
                color = DarkSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Switch Active Project",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    projects.forEach { proj ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (proj.id == currentProject?.id) DarkSurface else Color.Transparent)
                                .clickable {
                                    viewModel.selectProject(proj)
                                    showProjectSwitcher = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(proj.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                Text(proj.buildSystem, fontSize = 11.sp, color = TextMuted)
                            }
                            if (proj.id == currentProject?.id) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Split Explorer & Editor
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // File Explorer Drawer (Left Column - 140dp)
            Surface(
                color = DarkSurface,
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight(),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "EXPLORER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )

                    val projectFolder = currentProject?.let { File(it.path) }
                    val files = projectFolder?.walkTopDown()?.maxDepth(2)?.filter { it.isFile }?.toList() ?: emptyList()

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(files) { file ->
                            val isSelected = selectedFile?.absolutePath == file.absolutePath
                            val ext = file.extension
                            val icon = when (ext) {
                                "kt" -> Icons.Default.Code
                                "xml" -> Icons.Default.DataObject
                                "gradle", "kts" -> Icons.Default.Settings
                                "md" -> Icons.Default.Description
                                else -> Icons.AutoMirrored.Filled.InsertDriveFile
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) CyanAccentMuted else Color.Transparent)
                                    .clickable { viewModel.projectManager.selectFile(file) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isSelected) CyanAccent else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = file.name,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Code Editor (Right Column)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(TerminalBackground)
            ) {
                // Editor File Header
                Surface(
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedFile?.name ?: "No file open",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (isDirty) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AmberWarning)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.saveEditorFile() },
                            enabled = isDirty,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                disabledContainerColor = DarkSurfaceBorder
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Save", fontSize = 11.sp, color = if (isDirty) Color(0xFF042F2E) else TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Interactive Code Text Editor
                OutlinedTextField(
                    value = fileContent,
                    onValueChange = { viewModel.projectManager.updateEditorContent(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 18.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = TerminalBackground,
                        unfocusedContainerColor = TerminalBackground
                    )
                )
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New File", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("e.g. MyService.kt", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parent = currentProject?.let { File(it.path) } ?: File(".")
                        if (newFileName.isNotBlank()) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).run {
                                File(parent, newFileName).writeText("// New module\n")
                            }
                            newFileName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Create", color = Color(0xFF042F2E))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
