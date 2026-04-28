package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Tracks caffeine intake to model adenosine receptor occupancy.
 */
@Entity(tableName = "caffeine_logs")
data class CaffeineLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mg: Int, // Milligrams of caffeine
    val source: String, // e.g., "Coffee", "Tea", "Soda"
    val timestamp: Instant = Instant.now()
)
