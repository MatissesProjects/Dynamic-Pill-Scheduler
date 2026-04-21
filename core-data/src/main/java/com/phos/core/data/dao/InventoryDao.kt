package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.InventoryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_records WHERE medicationId = :medicationId")
    suspend fun getInventoryForMedication(medicationId: String): InventoryRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateInventory(record: InventoryRecord)

    @Query("SELECT * FROM inventory_records")
    fun getAllInventory(): Flow<List<InventoryRecord>>

    @Query("UPDATE inventory_records SET currentPillCount = currentPillCount - :amount WHERE medicationId = :medicationId")
    suspend fun decrementPillCount(medicationId: String, amount: Int)
}
