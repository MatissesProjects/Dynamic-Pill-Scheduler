package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a medication that is taken on an as-needed basis (Pro Re Nata).
 */
@Entity(tableName = "prn_medications")
data class PRNMedication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: String,
    val name: String,
    val dosage: String,
    val maxDosesPer24h: Int,
    val minGapMinutes: Int,
    val reasonForUse: String? = null,
    val colorHex: String? = null,
    
    // Temporal Versioning
    val validFrom: Long,
    val validTo: Long = Long.MAX_VALUE
)
