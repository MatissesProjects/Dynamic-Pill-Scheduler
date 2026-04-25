package com.phos.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.phos.phone.ui.dashboard.MainDashboard
import com.phos.phone.ui.dashboard.DashboardViewModel
import com.phos.core.data.proto.PhosState
import com.phos.phone.ui.theme.PhosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val scope = rememberCoroutineScope()
            val medications by viewModel.medications.collectAsState()
            val prnMedications by viewModel.prnMedications.collectAsState()
            val phosState by viewModel.phosState.collectAsState(initial = PhosState.getDefaultInstance())
            val healthInsights by viewModel.healthInsights.collectAsState()
            val sideEffectAlerts by viewModel.sideEffectAlerts.collectAsState()
            val napOverlaps by viewModel.napOverlaps.collectAsState()
            val postureRecommendation by viewModel.postureRecommendation.collectAsState()
            val prnAdvisory by viewModel.prnAdvisory.collectAsState()
            val travelProposal by viewModel.travelProposal.collectAsState()
            val eatingWindows by viewModel.eatingWindows.collectAsState()
            val nutrientAdvisory by viewModel.nutrientAdvisory.collectAsState()
            
            val medicationDepletions by viewModel.medicationDepletions.collectAsState()
            val nutrientReferences by viewModel.nutrientReferences.collectAsState()
            
            val healthGoals by viewModel.healthGoals.collectAsState()
            val optimizationSuggestions by viewModel.optimizationSuggestions.collectAsState()
            
            val sleepCalibrationInsight by viewModel.sleepCalibrationInsight.collectAsState()
            val sleepSubjectiveLogs by viewModel.sleepSubjectiveLogs.collectAsState()
            
            val voiceState by viewModel.voiceManager.state.collectAsState()
            val voiceExtractedEntities by viewModel.voiceExtractedEntities.collectAsState()
            
            PhosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainDashboard(
                        medications = medications,
                        prnMedications = prnMedications,
                        phosState = phosState,
                        healthInsights = healthInsights,
                        sideEffectAlerts = sideEffectAlerts,
                        napOverlaps = napOverlaps,
                        postureRecommendation = postureRecommendation,
                        prnAdvisory = prnAdvisory,
                        travelProposal = travelProposal,
                        eatingWindows = eatingWindows,
                        nutrientAdvisory = nutrientAdvisory,
                        medicationDepletions = medicationDepletions,
                        nutrientReferences = nutrientReferences,
                        healthGoals = healthGoals,
                        optimizationSuggestions = optimizationSuggestions,
                        sleepCalibrationInsight = sleepCalibrationInsight,
                        sleepSubjectiveLogs = sleepSubjectiveLogs,
                        voiceState = voiceState,
                        voiceExtractedEntities = voiceExtractedEntities,
                        onAddMedication = { name, dosage, offset, frequency, foodRequirement ->
                            viewModel.addMedication(name, dosage, offset, frequency, foodRequirement)
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
                        },
                        onRequestPRNAdvisory = { med ->
                            viewModel.requestPRNAdvisory(med)
                        },
                        onLogPRNDose = { med ->
                            viewModel.logPRNDose(med)
                        },
                        onClearPRNAdvisory = {
                            viewModel.clearPRNAdvisory()
                        },
                        onStartVoiceListening = {
                            viewModel.voiceManager.startListening()
                        },
                        onStopVoiceListening = {
                            viewModel.voiceManager.stopListening()
                        },
                        onProcessVoiceCommand = { text ->
                            viewModel.processVoiceCommand(text)
                        },
                        onClearVoiceResults = {
                            viewModel.clearVoiceResults()
                        },
                        onAcceptTravelProposal = { proposal ->
                            viewModel.acceptTravelProposal(proposal)
                        },
                        onDismissTravelProposal = {
                            viewModel.dismissTravelProposal()
                        },
                        onDetectTravel = {
                            viewModel.detectUpcomingTravel()
                        },
                        onLogAppetite = { hunger, difficulty ->
                            viewModel.logAppetite(hunger, difficulty)
                        },
                        onLogFood = { name, category, nutrients ->
                            viewModel.logFood(name, category, nutrients)
                        },
                        onRequestNutrientAdvisory = { name, nutrients ->
                            viewModel.requestNutrientAdvisory(name, nutrients)
                        },
                        onClearNutrientAdvisory = {
                            viewModel.clearNutrientAdvisory()
                        },
                        onAddHealthGoal = { desc, symp, off ->
                            viewModel.addHealthGoal(desc, symp, off)
                        },
                        onUpdateMealPreferences = { bStart, bEnd, lStart, lEnd, dStart, dEnd ->
                            viewModel.updateMealPreferences(bStart, bEnd, lStart, lEnd, dStart, dEnd)
                        },
                        onLogSleepSubjective = { qual, rest, mood ->
                            viewModel.logSleepSubjective(qual, rest, mood)
                        },
                        aiTextParser = { text ->
                            viewModel.parseNutritionTextWithNano(text)
                        },
                        aiVisionParser = { bitmap ->
                            viewModel.analyzeMealWithNano(bitmap)
                        }
                    )
                }
            }
        }
    }
}
