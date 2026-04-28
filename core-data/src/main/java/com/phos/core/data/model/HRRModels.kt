package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "hrr_records")
data class HRRRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val workoutEndTime: Instant,
    val peakHr: Double,
    val hrAtOneMin: Double?,
    val hrAtTwoMin: Double?,
    val hrrOneMin: Double?,
    val hrrTwoMin: Double?,
    val medicationVersion: Long // Link to versioning
)

data class HRRAudit(
    val date: String,
    val sevenDayAvgOneMin: Double,
    val currentOneMin: Double,
    val trendDelta: Double, // Percent change
    val isStrained: Boolean,
    val advice: String
)
