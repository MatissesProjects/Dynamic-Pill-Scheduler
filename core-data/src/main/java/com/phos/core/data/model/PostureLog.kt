package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "posture_logs")
data class PostureLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Instant,
    val type: PostureLogType,
    val value: Double, // Pressure in hPa or Slump angle
    val verticalDeltaMeters: Double? = null
)

enum class PostureLogType {
    BAROMETRIC_PRESSURE,
    DESK_SLUMP_ANGLE,
    VERTICAL_TRANSITION_DETECTED
}
