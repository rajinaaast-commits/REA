package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ClaudeSessionEntity::class,
        ActivityEventEntity::class,
        BuildArtifactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EvaAppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun artifactDao(): ArtifactDao

    companion object {
        @Volatile
        private var INSTANCE: EvaAppDatabase? = null

        fun getDatabase(context: Context): EvaAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EvaAppDatabase::class.java,
                    "eva_workstation.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
