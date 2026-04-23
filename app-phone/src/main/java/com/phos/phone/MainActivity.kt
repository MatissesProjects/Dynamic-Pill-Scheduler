package com.phos.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.phos.phone.ui.dashboard.MainDashboard
import com.phos.phone.ui.dashboard.DashboardViewModel
import com.phos.core.data.proto.PhosState
import com.phos.phone.ui.theme.PhosTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val medications by viewModel.medications.collectAsState()
            val phosState by viewModel.phosState.collectAsState(initial = PhosState.getDefaultInstance())
            val healthInsights by viewModel.healthInsights.collectAsState()
            val sideEffectAlerts by viewModel.sideEffectAlerts.collectAsState()
            val napOverlaps by viewModel.napOverlaps.collectAsState()
            val postureRecommendation by viewModel.postureRecommendation.collectAsState()
            
            PhosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainDashboard(
                        medications = medications,
                        tWakeEpoch = phosState.tWakeEpoch,
                        lastAiInsight = phosState.lastAiInsight,
                        is24Hour = phosState.is24Hour,
                        healthInsights = healthInsights,
                        sideEffectAlerts = sideEffectAlerts,
                        napOverlaps = napOverlaps,
                        postureRecommendation = postureRecommendation,
                        onAddMedication = { name, dosage, offset, frequency ->
                            viewModel.addMedication(name, dosage, offset, frequency)
                        },
                        onUpdateMedication = { record ->
                            viewModel.updateMedication(record)
                        },
                        onDeleteMedication = { id ->
                            viewModel.deleteMedication(id)
                        },
                        onDuplicateMedication = { record ->
                            viewModel.duplicateMedication(record)
                        },
                        onUpdateWakeTime = { newEpoch ->
                            viewModel.updateWakeTime(newEpoch)
                        },
                        onToggleTimeFormat = { is24Hour ->
                            viewModel.toggleTimeFormat(is24Hour)
                        },
                        onDismissInsight = { id ->
                            viewModel.dismissInsight(id)
                        }
                    )
                }
            }
        }
    }
}
