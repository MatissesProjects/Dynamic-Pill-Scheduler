package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.phos.core.data.model.EnvironmentalLog
import com.phos.core.data.model.SymptomLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface IntelligenceDao {
    @Insert
    suspend fun insertSymptom(log: SymptomLog)

    @Insert
    suspend fun insertEnvironmental(log: EnvironmentalLog)

    @Query("SELECT * FROM symptom_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getSymptomsSince(since: Instant): Flow<List<SymptomLog>>

    @Query("SELECT * FROM environmental_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getEnvironmentalSince(since: Instant): Flow<List<EnvironmentalLog>>

    @Query("SELECT * FROM symptom_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSymptoms(limit: Int): List<SymptomLog>

    @Query("SELECT * FROM symptom_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getRecentSymptoms(since: Instant): List<SymptomLog>

    @Query("SELECT * FROM environmental_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEnvironmental(limit: Int): List<EnvironmentalLog>
}
