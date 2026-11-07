package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ClaudeSessionEntity>>

    @Query("SELECT * FROM activity_events WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getEventsForSession(sessionId: String): Flow<List<ActivityEventEntity>>

    @Query("SELECT * FROM activity_events ORDER BY timestamp DESC LIMIT 100")
    fun getAllEvents(): Flow<List<ActivityEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ClaudeSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ActivityEventEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM activity_events WHERE sessionId = :sessionId")
    suspend fun deleteSessionEvents(sessionId: String)
}

@Dao
interface ArtifactDao {
    @Query("SELECT * FROM build_artifacts ORDER BY createdAt DESC")
    fun getAllArtifacts(): Flow<List<BuildArtifactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtifact(artifact: BuildArtifactEntity)

    @Query("DELETE FROM build_artifacts WHERE id = :id")
    suspend fun deleteArtifactById(id: String)
}
