package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.BiometricBaseline
import com.phos.core.data.model.BiometricLog
import com.phos.core.data.model.BiometricType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface BiometricDao {
    @Insert
    suspend fun insertLog(log: BiometricLog)

    @Query("SELECT * FROM biometric_logs WHERE type = :type AND timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getLogsSince(type: BiometricType, since: Instant): List<BiometricLog>

    @Query("SELECT * FROM biometric_baselines WHERE medicationId = :medicationId")
    suspend fun getBaselineForMedication(medicationId: String): BiometricBaseline?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBaseline(baseline: BiometricBaseline)

    @Query("SELECT * FROM biometric_baselines")
    fun getAllBaselines(): Flow<List<BiometricBaseline>>
}
