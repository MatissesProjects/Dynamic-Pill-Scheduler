package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Tracks appetite and eating difficulty levels to help optimize medication windows.
 */
@Entity(tableName = "appetite_logs")
data class AppetiteLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hungerLevel: Int, // 1-10 (1 = Nausea/No hunger, 10 = Very hungry)
    val difficultyLevel: Int, // 1-10 (1 = Easy, 10 = Very hard to eat)
    val timestamp: Instant = Instant.now(),
    val notes: String? = null
)
