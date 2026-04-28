package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class SleepStage {
    AWAKE,
    LIGHT,
    DEEP,
    REM,
    UNKNOWN
}

data class SleepStageSample(
    val startTime: Instant,
    val endTime: Instant,
    val stage: SleepStage
)

data class REMFragmentationInsight(
    val date: String,
    val fragmentationIndex: Double, // 0.0 to 1.0 (Higher is more fragmented)
    val awakeSpikeCount: Int,
    val totalRemMinutes: Int,
    val isExcessive: Boolean
)

data class SleepRestorationAudit(
    val date: String,
    val remStabilityScore: Int, // 0-100
    val dreamIntensity: Int?, // 1-10 (from M2)
    val restorationMessage: String
)

@Entity(tableName = "dream_logs")
data class DreamLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val rawText: String,
    val intensity: Int, // 1-10
    val vividness: Int, // 1-10
    val timestamp: Instant = Instant.now()
)
