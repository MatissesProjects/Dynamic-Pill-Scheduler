package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_logs")
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: String,
    val scheduledTime: Long, // Epoch millis
    val actualTime: Long?, // Epoch millis, null if not taken yet
    val status: String, // "PENDING", "TAKEN", "SKIPPED", "SNOOZED"
    val notes: String? = null
)
