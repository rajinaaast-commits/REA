package com.example.data.repository

import com.example.data.local.*
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WorkstationRepository(
    private val sessionDao: SessionDao,
    private val artifactDao: ArtifactDao
) {
    val allSessions: Flow<List<ClaudeSessionEntity>> = sessionDao.getAllSessions()

    val allEvents: Flow<List<ActivityEvent>> = sessionDao.getAllEvents().map { entities ->
        entities.map { entity ->
            ActivityEvent(
                id = entity.id,
                timestamp = entity.timestamp,
                type = try { EventType.valueOf(entity.type) } catch (e: Exception) { EventType.AGENT_RESPONSE },
                title = entity.title,
                rawText = entity.rawText,
                toolName = entity.toolName,
                filePath = entity.filePath,
                codeSnippet = entity.codeSnippet,
                command = entity.command,
                status = try { EventStatus.valueOf(entity.status) } catch (e: Exception) { EventStatus.SUCCESS }
            )
        }
    }

    val allArtifacts: Flow<List<ApkArtifact>> = artifactDao.getAllArtifacts().map { entities ->
        entities.map { entity ->
            ApkArtifact(
                id = entity.id,
                fileName = entity.fileName,
                filePath = entity.filePath,
                sizeBytes = entity.sizeBytes,
                variant = entity.variant,
                createdAt = entity.createdAt,
                verified = entity.verified,
                packageName = entity.packageName,
                sha256 = entity.sha256,
                md5 = entity.md5
            )
        }
    }

    suspend fun saveEvent(event: ActivityEvent, sessionId: String = "default-session") {
        val entity = ActivityEventEntity(
            id = event.id,
            sessionId = sessionId,
            timestamp = event.timestamp,
            type = event.type.name,
            title = event.title,
            rawText = event.rawText,
            toolName = event.toolName,
            filePath = event.filePath,
            codeSnippet = event.codeSnippet,
            command = event.command,
            status = event.status.name
        )
        sessionDao.insertEvent(entity)
    }

    suspend fun saveArtifact(artifact: ApkArtifact) {
        val entity = BuildArtifactEntity(
            id = artifact.id,
            fileName = artifact.fileName,
            filePath = artifact.filePath,
            sizeBytes = artifact.sizeBytes,
            variant = artifact.variant,
            createdAt = artifact.createdAt,
            verified = artifact.verified,
            packageName = artifact.packageName,
            sha256 = artifact.sha256,
            md5 = artifact.md5
        )
        artifactDao.insertArtifact(entity)
    }

    suspend fun deleteArtifact(id: String) {
        artifactDao.deleteArtifactById(id)
    }

    suspend fun createNewSession(title: String, model: String, providerUrl: String): ClaudeSessionEntity {
        val session = ClaudeSessionEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            model = model,
            providerUrl = providerUrl
        )
        sessionDao.insertSession(session)
        return session
    }
}
