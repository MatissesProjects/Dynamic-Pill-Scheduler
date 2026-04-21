package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Tracks the current supply and refill requirements for a medication.
 */
@Entity(tableName = "inventory_records")
data class InventoryRecord(
    @PrimaryKey val medicationId: String,
    val currentPillCount: Int,
    val refillThreshold: Int, // Notify when count falls below this
    val pillsPerDose: Int = 1,
    val lastRefillDate: Instant? = null,
    val pharmacyContact: String? = null // For Refill Intent automation
)
