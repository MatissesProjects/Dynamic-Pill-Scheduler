package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temporal_anchors")
data class TemporalAnchor(
    @PrimaryKey val date: String, // ISO-8601 date YYYY-MM-DD
    val wakeTime: Long, // Epoch millis of detected T-Wake
    val source: String // e.g., "HealthConnect", "Manual", "Default"
)
