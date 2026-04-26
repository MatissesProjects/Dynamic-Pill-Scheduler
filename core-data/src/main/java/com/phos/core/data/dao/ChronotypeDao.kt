package com.phos.core.data.dao

import androidx.room.*
import com.phos.core.data.model.ChronotypeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ChronotypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateChronotype(record: ChronotypeRecord)

    @Query("SELECT * FROM chronotype_records WHERE id = 1")
    fun getChronotypeFlow(): Flow<ChronotypeRecord?>

    @Query("SELECT * FROM chronotype_records WHERE id = 1")
    suspend fun getChronotype(): ChronotypeRecord?
}
