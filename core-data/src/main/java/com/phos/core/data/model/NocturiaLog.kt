package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Tracks a nighttime bathroom break or sleep interruption.
 */
@Entity(tableName = "nocturia_logs")
data class NocturiaLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Long,
    val isAutomatic: Boolean = true, // True if detected via sleep bridging
    val notes: String? = null
)
