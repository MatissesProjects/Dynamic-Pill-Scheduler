package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.MedicationInducedDepletion
import com.phos.core.data.model.NutrientReference
import kotlinx.coroutines.flow.Flow

@Dao
interface NutrientDao {
    @Query("SELECT * FROM nutrient_references")
    fun getAllReferences(): Flow<List<NutrientReference>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReference(ref: NutrientReference)

    @Query("SELECT * FROM medication_depletions")
    fun getAllDepletions(): Flow<List<MedicationInducedDepletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepletion(depletion: MedicationInducedDepletion)

    @Query("SELECT * FROM nutrient_references WHERE ref_proteinG >= :minProtein")
    suspend fun findHighProteinFoods(minProtein: Double): List<NutrientReference>
}
