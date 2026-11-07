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

    val gitDiffs by viewModel.gitDiffs.collectAsState()
    val gitCommits by viewModel.gitCommits.collectAsState()
    val selectedDiff by viewModel.selectedDiffFile.collectAsState()
    val commitMessage by viewModel.commitMessage.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Files, 1: Git Control
    var showProjectSwitcher by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Project Selector & Sub-Tabs Header
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
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

                // Sub-Tabs: [Files] vs [Git Control]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeSubTab == 0) DarkSurface else Color.Transparent)
                            .clickable { activeSubTab = 0 }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Files",
                            fontSize = 12.sp,
                            fontWeight = if (activeSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeSubTab == 0) TextPrimary else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeSubTab == 1) DarkSurface else Color.Transparent)
                            .clickable { activeSubTab = 1 }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Git Control",
                                fontSize = 12.sp,
                                fontWeight = if (activeSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeSubTab == 1) CyanAccent else TextSecondary
                            )
                            if (gitDiffs.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(AmberWarning)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${gitDiffs.size}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
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

        // SubTab 0: Files Explorer & Code Editor
        if (activeSubTab == 0) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // File Explorer Drawer (Left Column - 140dp)
                Surface(
                    color = DarkSurface,
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXPLORER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            IconButton(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "New File", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

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

                // Code Editor with Line Numbers Gutter (Right Column)
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
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
                    }

                    // Editor Body: Line numbers gutter + text input
                    val lines = fileContent.split("\n")
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Line number column (Step 5)
                        Column(
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight()
                                .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            lines.take(60).forEachIndexed { index, _ ->
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }

                        // Interactive Code Text Editor
                        OutlinedTextField(
                            value = fileContent,
                            onValueChange = { viewModel.projectManager.updateEditorContent(it) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(2.dp),
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

                    // Editor Footer status bar
                    Surface(
                        color = DarkSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lines: ${lines.size} • Characters: ${fileContent.length}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                            Text(
                                text = if (isDirty) "Unsaved changes" else "Saved",
                                fontSize = 10.sp,
                                color = if (isDirty) AmberWarning else EmeraldSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // SubTab 1: Git Control & Visual Diff Viewer (Step 3)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Commit Box Section
                WorkstationCard(backgroundColor = DarkSurfaceElevated) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Git Commit Changes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            val stagedCount = gitDiffs.count { it.isStaged }
                            Text(
                                text = "$stagedCount staged file(s)",
                                fontSize = 11.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commitMessage,
                                onValueChange = { viewModel.setCommitMessage(it) },
                                placeholder = { Text("Commit message (e.g. feat: update build logic)...", color = TextMuted, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Button(
                                onClick = { viewModel.commitStagedChanges() },
                                enabled = commitMessage.isNotBlank() && gitDiffs.any { it.isStaged },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("Commit", fontSize = 12.sp, color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Split Changed Files List & Unified Diff Viewer
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Changed Files Column (140dp)
                    Column(
                        modifier = Modifier
                            .width(140.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "CHANGED FILES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(gitDiffs) { diff ->
                                val isSelected = selectedDiff?.filePath == diff.filePath
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CyanAccentMuted else Color.Transparent)
                                        .clickable { viewModel.selectDiffFile(diff) }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = diff.filePath.substringAfterLast("/"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "+${diff.additions} -${diff.deletions}",
                                            fontSize = 9.sp,
                                            color = if (diff.additions > 0) EmeraldSuccess else RoseError
                                        )
                                    }

                                    // Staged toggle checkbox
                                    IconButton(
                                        onClick = { viewModel.toggleStageFile(diff) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            if (diff.isStaged) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Toggle Stage",
                                            tint = if (diff.isStaged) CyanAccent else TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Visual Unified Diff Viewer
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBackground)
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                    ) {
                        // Diff Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedDiff?.filePath ?: "Select a changed file",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (selectedDiff != null) {
                                Text(
                                    text = if (selectedDiff!!.isStaged) "STAGED" else "UNSTAGED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDiff!!.isStaged) CyanAccent else AmberWarning
                                )
                            }
                        }

                        // Diff Lines
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            val lines = selectedDiff?.diffLines ?: emptyList()
                            items(lines) { diffLine ->
                                val (textColor, bgColor) = when (diffLine.type) {
                                    com.example.model.DiffLineType.ADDED -> Pair(Color(0xFF86EFAC), Color(0x2210B981))
                                    com.example.model.DiffLineType.DELETED -> Pair(Color(0xFFFCA5A5), Color(0x22EF4444))
                                    com.example.model.DiffLineType.HEADER -> Pair(CyanAccent, Color(0x2206B6D4))
                                    com.example.model.DiffLineType.CONTEXT -> Pair(TextSecondary, Color.Transparent)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = diffLine.lineText,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = textColor,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
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
