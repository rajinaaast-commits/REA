package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class ClaudeSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val model: String = "claude-3-7-sonnet",
    val providerUrl: String = "http://localhost:11434/v1"
)

@Entity(tableName = "activity_events")
data class ActivityEventEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String,
    val title: String,
    val rawText: String,
    val toolName: String? = null,
    val filePath: String? = null,
    val codeSnippet: String? = null,
    val command: String? = null,
    val status: String = "SUCCESS"
)

@Entity(tableName = "build_artifacts")
data class BuildArtifactEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val variant: String,
    val createdAt: Long,
    val verified: Boolean = true,
    val packageName: String = "com.aistudio.evabuildcli.mkvz",
    val sha256: String = "",
    val md5: String = ""
)
