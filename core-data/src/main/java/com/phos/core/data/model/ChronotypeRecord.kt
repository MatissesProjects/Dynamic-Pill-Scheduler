package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class Chronotype {
    LARK,      // Early bird
    OWL,       // Night owl
    NEUTRAL,   // Third bird
    UNKNOWN
}

/**
 * Stores the user's biological clock classification based on sleep-wake patterns.
 */
@Entity(tableName = "chronotype_records")
data class ChronotypeRecord(
    @PrimaryKey val id: Int = 1, // Only one record needed
    val type: Chronotype,
    val midSleepTimeMillis: Long, // Average mid-sleep point relative to midnight
    val lastUpdated: Instant = Instant.now(),
    val confidence: Float // 0.0 to 1.0 based on data volume
)
