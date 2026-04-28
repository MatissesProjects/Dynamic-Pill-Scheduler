package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.HRRRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HRRDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HRRRecord): Long

    @Query("SELECT * FROM hrr_records ORDER BY workoutEndTime DESC")
    fun getAllRecordsFlow(): Flow<List<HRRRecord>>

    @Query("SELECT * FROM hrr_records WHERE date >= :startDate ORDER BY workoutEndTime DESC")
    suspend fun getRecordsSince(startDate: String): List<HRRRecord>
}
