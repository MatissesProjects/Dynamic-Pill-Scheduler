package com.phos.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.phos.phone.ui.dashboard.VerticalTimeline
import com.phos.core.data.datastore.phosDataStore
import com.phos.core.data.proto.Medication
import com.phos.core.data.proto.PhosState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Populate initial data for testing
        lifecycleScope.launch {
            val currentData = phosDataStore.data.first()
            if (currentData.medicationsCount == 0) {
                phosDataStore.updateData { state ->
                    state.toBuilder()
                        .setTWakeEpoch(System.currentTimeMillis())
                        .setLastAiInsight("AI Insight: Biometric baseline established. Your current HR response is optimal.")
                        .addMedications(
                            Medication.newBuilder()
                                .setId("med_001")
                                .setName("Metoprolol")
                                .setScheduledTime(System.currentTimeMillis() + 3600000)
                                .setStatus("PENDING")
                        )
                        .addMedications(
                            Medication.newBuilder()
                                .setId("med_002")
                                .setName("Vitamin D")
                                .setScheduledTime(System.currentTimeMillis() + 7200000)
                                .setStatus("TAKEN")
                        )
                        .build()
                }
            }
        }

        setContent {
            val state by phosDataStore.data.collectAsState(initial = PhosState.getDefaultInstance())
            
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VerticalTimeline(state = state)
                }
            }
        }
    }
}
