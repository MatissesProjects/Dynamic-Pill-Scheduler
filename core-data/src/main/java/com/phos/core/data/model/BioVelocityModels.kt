package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * M1: Bio-Velocity Log for tracking biological age trends.
 */
@Entity(tableName = "bio_velocity_logs")
data class BioVelocityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Instant = Instant.now(),
    val biologicalAge: Double,
    val chronologicalAge: Double,
    val paceOfAging: Double, // 1.0 is neutral, < 1.0 is slower (good), > 1.0 is faster (bad)
    val hrvDelta: Double,
    val rhrDelta: Double,
    val sleepDelta: Double,
    val adherenceImpact: Double // Years improved due to adherence in the last calculation period
)

/**
 * M1: Baseline metrics for bio-velocity calculations.
 */
@Entity(tableName = "bio_baseline")
data class BioBaseline(
    @PrimaryKey val id: Int = 1,
    val baselineHrv: Double,
    val baselineRhr: Double,
    val baselineSleepConsistency: Double, // 0.0 to 1.0
    val lastCalculatedAt: Instant = Instant.now()
)
