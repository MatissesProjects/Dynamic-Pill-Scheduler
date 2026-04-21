package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "symptom_logs")
data class SymptomLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symptomName: String,
    val severity: Int, // 1-10
    val timestamp: Instant = Instant.now(),
    val notes: String? = null
)
