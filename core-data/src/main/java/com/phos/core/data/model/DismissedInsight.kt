package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Tracks which health insights or alerts the user has dismissed.
 */
@Entity(tableName = "dismissed_insights")
data class DismissedInsight(
    @PrimaryKey val insightId: String, // A unique key for the specific insight (e.g., "absorption_sucralfate")
    val dismissedAt: Instant = Instant.now()
)
