package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: String, // Stable ID across versions
    val name: String,
    val dosage: String,
    val frequencyOffset: Long, // Offset in milliseconds from T-Wake
    
    // Temporal Versioning
    val validFrom: Long,
    val validTo: Long = Long.MAX_VALUE
)
