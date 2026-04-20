package com.phos.core.data.sync

import android.content.Context
import androidx.datastore.core.DataStore
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import com.phos.core.data.proto.PhosState
import com.phos.core.data.serializer.PhosStateSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

/**
 * Repository responsible for cross-device synchronization using Horologist DataLayer.
 */
class DataLayerRepository(
    private val context: Context,
    private val dataStore: DataStore<PhosState>
) {
    private val phoneDataLayerAppHelper by lazy { PhoneDataLayerAppHelper(context, null) }

    val phosStateFlow: Flow<PhosState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(PhosState.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun updateTWake(tWakeEpoch: Long) {
        dataStore.updateData { currentState ->
            currentState.toBuilder()
                .setTWakeEpoch(tWakeEpoch)
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
}
