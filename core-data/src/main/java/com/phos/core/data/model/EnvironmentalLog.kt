package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "environmental_logs")
data class EnvironmentalLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Instant,
    val aqi: Int,
    val pm25: Double, // µg/m³
    val ozone: Double, // ppb
    val barometricPressure: Double, // hPa
    val humidity: Double,
    val temperatureCelsius: Double,
    val locationLabel: String? = null
)
