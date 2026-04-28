package com.phos.core.data.sync

import android.content.Context
import androidx.datastore.core.DataStore
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.WearDataLayerRegistry
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import com.phos.core.data.proto.PhosState
import com.phos.core.data.serializer.PhosStateSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

/**
 * Repository responsible for cross-device synchronization using Horologist DataLayer.
 */
@OptIn(ExperimentalHorologistApi::class)
class DataLayerRepository(
    private val context: Context,
    private val dataStore: DataStore<PhosState>
) {
    // Temporarily disabled due to contradictory compiler errors regarding constructor arguments in Horologist 0.5.21
    /*
    private val phoneDataLayerAppHelper by lazy { 
        PhoneDataLayerAppHelper(
            context = context, 
            registry = WearDataLayerRegistry.fromContext(context)
        ) 
    }
    */

    val phosStateFlow: Flow<PhosState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(PhosState.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun updateTWake(tWakeEpoch: Long, wasInterrupted: Boolean = false) {
        dataStore.updateData { currentState ->
            currentState.toBuilder()
                .setTWakeEpoch(tWakeEpoch)
                .setWasInterrupted(wasInterrupted)
                .build()
        }
    }

    suspend fun updateAutonomicStrain(detected: Boolean) {
        dataStore.updateData { currentState ->
            currentState.toBuilder()
                .setAutonomicStrainDetected(detected)
                .build()
        }
    }

    suspend fun updateSafetyTightening(millis: Long) {
        dataStore.updateData { currentState ->
            currentState.toBuilder()
                .setSafetyTighteningMillis(millis)
                .build()
        }
    }

    suspend fun addMedication(medicationId: String, name: String, scheduledTime: Long) {
        dataStore.updateData { currentState ->
            val medication = com.phos.core.data.proto.Medication.newBuilder()
                .setId(medicationId)
                .setName(name)
                .setScheduledTime(scheduledTime)
                .setStatus("PENDING")
                .build()
            
            currentState.toBuilder()
                .addMedications(medication)
                .build()
        }
    }

    suspend fun updateMealPreferences(
        breakfastStart: Long, breakfastEnd: Long,
        lunchStart: Long, lunchEnd: Long,
        dinnerStart: Long, dinnerEnd: Long
    ) {
        dataStore.updateData { currentState ->
            val newPrefs = com.phos.core.data.proto.MealPreferences.newBuilder()
                .setBreakfastStartOffset(breakfastStart)
                .setBreakfastEndOffset(breakfastEnd)
                .setLunchStartOffset(lunchStart)
                .setLunchEndOffset(lunchEnd)
                .setDinnerStartOffset(dinnerStart)
                .setDinnerEndOffset(dinnerEnd)
                .build()

            currentState.toBuilder()
                .setMealPreferences(newPrefs)
                .build()
        }
    }
}
