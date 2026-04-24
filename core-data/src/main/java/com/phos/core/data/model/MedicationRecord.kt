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
    val colorHex: String? = null, // Material You theme primary color
    val category: String? = "General", // e.g., "Heart", "Supplements", "Pain"
    val foodRequirement: String = "NONE", // "NONE", "WITH_FOOD", "EMPTY_STOMACH"
    
    // Temporal Versioning
    val validFrom: Long,
    val validTo: Long = Long.MAX_VALUE
)
