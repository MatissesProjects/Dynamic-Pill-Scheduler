package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.SymptomLog
import java.time.Instant

@Dao
interface ReportingDao {
    @Query("SELECT * FROM dose_logs WHERE scheduledTime >= :since ORDER BY scheduledTime ASC")
    suspend fun getDoseLogsSince(since: Long): List<DoseLog>

    @Query("SELECT * FROM symptom_logs WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getSymptomsSince(since: Instant): List<SymptomLog>

    @Query("""
        SELECT 
            medicationId, 
            COUNT(*) as total,
            SUM(CASE WHEN status = 'TAKEN' THEN 1 ELSE 0 END) as takenCount
        FROM dose_logs 
        WHERE scheduledTime >= :since 
        GROUP BY medicationId
    """)
    suspend fun getAdherenceStats(since: Long): List<AdherenceStat>
}

data class AdherenceStat(
    val medicationId: String,
    val total: Int,
    val takenCount: Int
)
