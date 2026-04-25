package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Subjective user feedback on sleep quality (e.g. from morning check-in).
 */
@Entity(tableName = "sleep_subjective_logs")
data class SleepSubjectiveLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restfulnessRating: Int, // 1-10
    val morningMood: String, // e.g. "Groggy", "Alert", "Tired"
    val reportedQuality: Int, // 1-10
    val date: String, // YYYY-MM-DD
    val timestamp: Instant = Instant.now()
)
