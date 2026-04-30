package com.phos.core.data.dao

import androidx.room.*
import com.phos.core.data.model.BioBaseline
import com.phos.core.data.model.BioVelocityLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface BioVelocityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BioVelocityLog)

    @Query("SELECT * FROM bio_velocity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<BioVelocityLog>>

    @Query("SELECT * FROM bio_velocity_logs WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getLogsSince(since: Instant): List<BioVelocityLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBaseline(baseline: BioBaseline)

    @Query("SELECT * FROM bio_baseline WHERE id = 1")
    suspend fun getBaseline(): BioBaseline?
}
