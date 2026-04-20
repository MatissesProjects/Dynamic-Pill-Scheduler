package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "environmental_logs")
data class EnvironmentalLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barometricPressure: Float, // hPa
    val temperature: Float? = null,
    val humidity: Float? = null,
    val timestamp: Instant = Instant.now()
)
