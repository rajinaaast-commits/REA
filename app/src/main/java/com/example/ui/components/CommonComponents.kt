package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemSupportStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: SystemSupportStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderCol) = when (status) {
        SystemSupportStatus.WORKING -> Triple(EmeraldSuccessMuted, EmeraldSuccess, EmeraldSuccess.copy(alpha = 0.4f))
        SystemSupportStatus.PARTIALLY_SUPPORTED -> Triple(AmberWarningMuted, AmberWarning, AmberWarning.copy(alpha = 0.4f))
        SystemSupportStatus.UNSUPPORTED -> Triple(RoseErrorMuted, RoseError, RoseError.copy(alpha = 0.4f))
        SystemSupportStatus.NOT_INSTALLED -> Triple(DarkSurfaceElevated, TextSecondary, DarkSurfaceBorder)
        SystemSupportStatus.FAILED -> Triple(RoseErrorMuted, RoseError, RoseError.copy(alpha = 0.4f))
        SystemSupportStatus.NOT_TESTED -> Triple(DarkSurfaceElevated, TextMuted, DarkSurfaceBorder)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = status.label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun WorkstationCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkSurface,
    borderColor: Color = DarkSurfaceBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

@Composable
fun CodeSnippetView(
    code: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 12
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalBackground)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp,
            maxLines = maxLines
        )
    }
}

@Composable
fun HeaderStatusBar(
    projectName: String,
    gitBranch: String,
    reaActive: Boolean,
    isBuilding: Boolean,
    onReaClick: () -> Unit = {}
) {
    Surface(
        color = DarkBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Project & Git Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "📦 $projectName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "•",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "🌿 $gitBranch",
                    fontSize = 11.sp,
                    color = CyanAccent,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Right Status Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // REA Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (reaActive) PurpleReaMuted else DarkSurfaceElevated)
                        .border(1.dp, if (reaActive) PurpleRea.copy(alpha = 0.5f) else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onReaClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ REA v0.4.1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (reaActive) Color(0xFFC4B5FD) else TextMuted
                    )
                }

                // Build Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBuilding) AmberWarningMuted else EmeraldSuccessMuted)
                        .border(1.dp, if (isBuilding) AmberWarning else EmeraldSuccess.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBuilding) "🔨 Building..." else "✓ AGP 9.1.1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBuilding) AmberWarning else EmeraldSuccess
                    )
                }
            }
        }
    }
}
