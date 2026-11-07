package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavDestination
import com.example.ui.EvaWorkstationViewModel
import com.example.ui.components.HeaderStatusBar
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: EvaWorkstationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EvaWorkstationApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EvaWorkstationApp(viewModel: EvaWorkstationViewModel) {
    val currentNav by viewModel.currentNav.collectAsState()
    val currentProject by viewModel.projectManager.currentProject.collectAsState()
    val isBuilding by viewModel.buildService.isBuilding.collectAsState()
    val reaDiag by viewModel.reaService.diagnostics.collectAsState()
    val pendingPermission by viewModel.claudeEngine.pendingPermission.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("eva_workstation_root"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            HeaderStatusBar(
                projectName = currentProject?.name ?: "EVA Core",
                gitBranch = currentProject?.gitBranch ?: "main",
                reaActive = reaDiag.toolDiscoveryWorking,
                isBuilding = isBuilding,
                onReaClick = { viewModel.setNav(NavDestination.DIAGNOSTICS) }
            )
        },
        bottomBar = {
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    val navItems = listOf(
                        Pair(NavDestination.WORKSPACE, Icons.Default.Dashboard),
                        Pair(NavDestination.PROJECTS, Icons.Default.Folder),
                        Pair(NavDestination.BUILD, Icons.Default.Hardware),
                        Pair(NavDestination.DIAGNOSTICS, Icons.Default.QueryStats),
                        Pair(NavDestination.SETTINGS, Icons.Default.Settings)
                    )

                    navItems.forEach { (dest, icon) ->
                        val isSelected = currentNav == dest
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setNav(dest) },
                            icon = {
                                Icon(
                                    icon,
                                    contentDescription = dest.label,
                                    tint = if (isSelected) CyanAccent else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = dest.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyanAccent else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = CyanAccentMuted,
                                selectedIconColor = CyanAccent,
                                selectedTextColor = CyanAccent,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (currentNav) {
                NavDestination.WORKSPACE -> WorkspaceScreen(viewModel = viewModel)
                NavDestination.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                NavDestination.BUILD -> BuildScreen(viewModel = viewModel)
                NavDestination.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                NavDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }

            // Claude Code Permission Request Dialog
            pendingPermission?.let { request ->
                AlertDialog(
                    onDismissRequest = { request.onDeny() },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = if (request.isDestructive) RoseError else AmberWarning
                            )
                            Text(request.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Claude Code requires your explicit authorization before continuing with this action:",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TerminalBackground)
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = request.actionDetail,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { request.onApprove() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (request.isDestructive) RoseError else CyanAccent)
                        ) {
                            Text(
                                "Authorize Action",
                                color = if (request.isDestructive) Color.White else Color(0xFF042F2E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { request.onDeny() }) {
                            Text("Reject", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurface
                )
            }
        }
    }
}
