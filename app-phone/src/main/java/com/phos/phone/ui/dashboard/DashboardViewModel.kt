package com.phos.phone.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.phos.core.data.db.PhosDatabase
import com.phos.core.data.model.*
import com.phos.core.data.datastore.phosDataStore
import com.phos.core.data.sync.DataLayerRepository
import com.phos.core.data.proto.PhosState
import com.phos.core.data.engine.CollisionResolver
import com.phos.core.data.engine.NapManager
import com.phos.core.data.engine.NapOverlap
import com.phos.core.data.sync.HealthSyncManager
import com.phos.core.intelligence.PostureIntelligence
import com.phos.core.intelligence.PosturalRecommendation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        PhosDatabase::class.java, "phos-db"
    ).fallbackToDestructiveMigration().build()

    private val medicationDao = db.medicationDao()
    private val temporalAnchorDao = db.temporalAnchorDao()
    private val interactionDao = db.interactionDao()
    private val dismissedInsightDao = db.dismissedInsightDao()

    private val dataLayerRepository = DataLayerRepository(application, application.phosDataStore)
    private val healthSyncManager = HealthSyncManager(application)
    private val napManager = NapManager(healthSyncManager)
    private val postureIntelligence = PostureIntelligence()

    private val collisionResolverFlow = combine(
        interactionDao.getAllRules(),
        interactionDao.getAllAbsorptionRules(),
        interactionDao.getAllSideEffectRules()
    ) { interaction, absorption, sideEffects ->
        CollisionResolver(interaction, absorption, sideEffects)
    }

    init {
        seedKnowledgeBase()
    }

    private fun seedKnowledgeBase() {
        viewModelScope.launch {
            // Seed Absorption Rules
            val absorptionRules = listOf(
                AbsorptionRule(medicationId = "sucralfate", requiredGapMinutes = 120, reason = "Take Sucralfate on an empty stomach, at least 2 hours before other medications to protect gut absorption."),
                AbsorptionRule(medicationId = "levothyroxine", requiredGapMinutes = 60, reason = "Take Levothyroxine 60 minutes before other meds or food for optimal absorption.")
            )
            absorptionRules.forEach { interactionDao.insertAbsorptionRule(it) }

            // Seed Side Effect Rules
            val sideEffectRules = listOf(
                SideEffectRule(medicationId = "lisinopril", sideEffect = "Dizziness/Cough", advice = "Monitor for a persistent dry cough or dizziness when standing up."),
                SideEffectRule(medicationId = "metoprolol", sideEffect = "Low Heart Rate", advice = "Watch for extreme fatigue or very low resting heart rate.")
            )
            sideEffectRules.forEach { interactionDao.insertSideEffectRule(it) }

            // Seed Interaction Rules
            val interactionRules = listOf(
                InteractionRule(sourceId = "grapefruit", targetId = "statin", gapMillis = 24 * 3600000L, reason = "Grapefruit inhibits metabolism of Statins, increasing toxicity risk.", severity = InteractionSeverity.CRITICAL)
            )
            interactionRules.forEach { interactionDao.insertRule(it) }
        }
    }

    private val dismissedIds = dismissedInsightDao.getAllDismissedIds()
    val medications: StateFlow<List<MedicationRecord>> = medicationDao.getAllActiveMedicationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val phosState: StateFlow<PhosState> = dataLayerRepository.phosStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PhosState.getDefaultInstance())

    val healthInsights: StateFlow<List<String>> = combine(medications, collisionResolverFlow, dismissedIds) { meds, resolver, dismissed ->
        resolver.findAbsorptionSpacingSuggestions(meds).filter { insight ->
            !dismissed.contains("absorption_${insight.hashCode()}")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sideEffectAlerts: StateFlow<List<SideEffectRule>> = combine(medications, collisionResolverFlow, dismissedIds) { meds, resolver, dismissed ->
        resolver.getSideEffectAlerts(meds).filter { alert ->
            !dismissed.contains("side_effect_${alert.medicationId}_${alert.sideEffect}")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val temporalAnchorFlow = temporalAnchorDao.getLatestAnchorFlow()
    
    val napOverlaps: StateFlow<List<NapOverlap>> = combine(medications, temporalAnchorFlow) { meds, anchor ->
        if (anchor == null) emptyList()
        else napManager.checkNapOverlaps(meds, anchor)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val postureRecommendation: StateFlow<PosturalRecommendation?> = flow {
        while (true) {
            val recentFood = interactionDao.getRecentFoodLogs(System.currentTimeMillis() - 2 * 3600000L)
            emit(postureIntelligence.checkPostPrandialPosture(recentFood))
            kotlinx.coroutines.delay(60000) // Refresh every minute
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addMedication(name: String, dosage: String, offsetMillis: Long, frequency: Int = 1) {
        viewModelScope.launch {
            for (i in 0 until frequency) {
                // Stagger doses by 4 hours if multiple
                val actualOffset = offsetMillis + (i * 4 * 3600000L)
                medicationDao.insert(
                    MedicationRecord(
                        medicationId = "${name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}_$i",
                        name = name,
                        dosage = dosage,
                        frequencyOffset = actualOffset,
                        validFrom = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun dismissInsight(id: String) {
        viewModelScope.launch {
            dismissedInsightDao.dismiss(DismissedInsight(id))
        }
    }

    fun updateMedication(record: MedicationRecord) {
        viewModelScope.launch {
            medicationDao.deletePermanently(record.id)
            medicationDao.insert(record.copy(id = 0))
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            medicationDao.deletePermanently(id)
        }
    }

    fun duplicateMedication(record: MedicationRecord) {
        viewModelScope.launch {
            medicationDao.insert(
                record.copy(
                    id = 0,
                    medicationId = "${record.name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}"
                )
            )
        }
    }

    fun updateWakeTime(epochMillis: Long) {
        viewModelScope.launch {
            dataLayerRepository.updateTWake(epochMillis)
        }
    }

    fun toggleTimeFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            getApplication<Application>().phosDataStore.updateData { state ->
                state.toBuilder().setIs24Hour(is24Hour).build()
            }
        }
    }
}
