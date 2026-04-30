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
import com.phos.core.data.engine.*
import com.phos.core.data.sync.HealthSyncManager
import com.phos.core.intelligence.*
import com.phos.core.data.proto.MealPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import kotlin.math.abs

class DashboardViewModel(
    application: Application,
    private val db: PhosDatabase = Room.databaseBuilder(
        application,
        PhosDatabase::class.java, "phos-db"
    ).fallbackToDestructiveMigration().build(),
    private val dataLayerRepository: DataLayerRepository = DataLayerRepository(application, application.phosDataStore),
    injectedVoiceManager: VoiceManager? = null,
    injectedNanoEngine: GeminiNanoEngine? = null
) : AndroidViewModel(application) {

    private val medicationDao = db.medicationDao()
    private val temporalAnchorDao = db.temporalAnchorDao()
    private val interactionDao = db.interactionDao()
    private val dismissedInsightDao = db.dismissedInsightDao()
    private val prnDao = db.prnDao()
    private val doseLogDao = db.doseLogDao()
    private val biometricDao = db.biometricDao()
    private val intelligenceDao = db.intelligenceDao()
    private val appetiteDao = db.appetiteDao()
    private val allergenDao = db.allergenDao()
    private val nutrientDao = db.nutrientDao()
    private val goalDao = db.goalDao()
    private val nocturiaDao = db.nocturiaDao()
    private val sleepSubjectiveDao = db.sleepSubjectiveDao()
    private val gaitDao = db.gaitDao()
    private val chronotypeDao = db.chronotypeDao()
    private val metabolicDao = db.metabolicDao()
    private val sentimentDao = db.sentimentDao()
    private val caffeineDao = db.caffeineDao()
    private val dreamDao = db.dreamDao()
    private val hrrDao = db.hrrDao()
    private val alcoholDao = db.alcoholDao()
    private val userProfileDao = db.userProfileDao()
    private val postureDao = db.postureDao()
    private val environmentalDao = db.environmentalDao()
    private val bioVelocityDao = db.bioVelocityDao()

    private val healthSyncManager = HealthSyncManager(application)
    private val gaitManager = GaitManager(gaitDao, healthSyncManager)
    private val chronotypeClassifier = ChronotypeClassifier()
    private val metabolicEngine = MetabolicEngine()
    private val metabolicClearanceEngine = MetabolicClearanceEngine()
    private val ebikeNormalizer = EbikeEffortNormalizer()
    private val stressSynthesisEngine = StressSynthesisEngine()
    private val giProtectionEngine = GIProtectionEngine()
    private val adenosineEngine = AdenosineEngine()
    private val napManager = NapManager(healthSyncManager)
    private val postureIntelligence = PostureIntelligence()
    private val jetLagManager = JetLagManager()
    private val mealScheduler = MealScheduler()
    private val goalOptimizationEngine = GoalOptimizationEngine()
    private val sleepCalibrationEngine = SleepCalibrationEngine()
    private val alertnessOrchestrator = AlertnessOrchestrator()
    private val betaBlockerSafetyEngine = BetaBlockerSafetyEngine()
    private val remSafetyEngine = REMSafetyEngine()
    private val cardioMismatchEngine = CardioMismatchEngine()
    private val hrrOrchestrator = HRROrchestrator()
    private val hfDecompensationEngine = HFDecompensationEngine()
    private val pulsePowerEngine = PulsePowerEngine()
    private val nocturnalRespiratoryEngine = NocturnalRespiratoryEngine()
    private val hormonalSyncEngine = HormonalSyncEngine(mealScheduler)
    private val thermalShieldEngine = ThermalShieldEngine()
    private val environmentalCorrelationEngine = EnvironmentalCorrelationEngine()
    private val bioVelocityEngine = BioVelocityEngine(injectedNanoEngine ?: GeminiNanoEngine(application))
    
    private val nanoEngine = injectedNanoEngine ?: GeminiNanoEngine(application)
    private val neuroLoadEngine = NeuroLoadEngine(nanoEngine)
    private val voiceParser = GeminiVoiceParser(nanoEngine)
    private val voiceLogCoordinator = VoiceLogCoordinator(
        doseLogDao, interactionDao, medicationDao, intelligenceDao, dreamDao, voiceParser
    )
    
    val voiceManager: VoiceManager = injectedVoiceManager ?: VoiceManager(application)

    // State Declarations
    private val dismissedIds = dismissedInsightDao.getAllDismissedIds()
    
    val phosState: StateFlow<PhosState> = dataLayerRepository.phosStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PhosState.getDefaultInstance())

    private val temporalAnchorFlow = temporalAnchorDao.getLatestAnchorFlow()

    val medications: StateFlow<List<MedicationRecord>> = medicationDao.getAllActiveMedicationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caffeineLogs: StateFlow<List<CaffeineLog>> = caffeineDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appetiteLogs: StateFlow<List<AppetiteLog>> = appetiteDao.getAppetiteLogsSince(Instant.now().minus(24, ChronoUnit.HOURS))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepSubjectiveLogs: StateFlow<List<SleepSubjectiveLog>> = sleepSubjectiveDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val metabolicLogs: StateFlow<List<MetabolicLoadLog>> = metabolicDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nocturiaLogs: StateFlow<List<NocturiaLog>> = nocturiaDao.getNocturiaLogsSince(Instant.now().minus(24, ChronoUnit.HOURS))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chronotype: StateFlow<ChronotypeRecord?> = chronotypeDao.getChronotypeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val healthGoals: StateFlow<List<HealthGoal>> = goalDao.getActiveGoalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptomLogs: StateFlow<List<SymptomLog>> = intelligenceDao.getSymptomsSince(Instant.now().minus(24, ChronoUnit.HOURS))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val giIrritantIds: StateFlow<List<String>> = medicationDao.getGIIrritantIdsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prnMedications: StateFlow<List<PRNMedication>> = prnDao.getAllActivePRNMedicationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = userProfileDao.getUserProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val bioVelocityLogs: StateFlow<List<BioVelocityLog>> = bioVelocityDao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _bioVelocityInsight = MutableStateFlow<String?>(null)
    val bioVelocityInsight: StateFlow<String?> = _bioVelocityInsight.asStateFlow()

    val alcoholLogs: StateFlow<List<AlcoholLog>> = alcoholDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allergenProfile: StateFlow<List<AllergenProfile>> = allergenDao.getAllergensFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val collisionResolverFlow = combine(
        interactionDao.getAllRules(),
        interactionDao.getAllAbsorptionRules(),
        interactionDao.getAllSideEffectRules()
    ) { interaction, absorption, sideEffects ->
        CollisionResolver(interaction, absorption, sideEffects)
    }

    private val prnAdvisorFlow = collisionResolverFlow.map { 
        PRNAdvisor(doseLogDao, biometricDao, it)
    }

    private val nutrientAdvisoryEngineFlow = collisionResolverFlow.map {
        NutrientAdvisoryEngine(it)
    }

    // Derived Flows
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

    val hormonalHarmony: StateFlow<HormonalHarmonyReport?> = combine(
        medications, temporalAnchorFlow, appetiteLogs
    ) { meds, anchor, appetite ->
        if (anchor == null) null
        else hormonalSyncEngine.evaluateHormonalHarmony(meds, anchor, appetite)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adenosineState: StateFlow<SleepPressureState?> = combine(temporalAnchorFlow, caffeineLogs) { anchor, caffeine ->
        if (anchor == null) null
        else adenosineEngine.calculateCurrentState(anchor.wakeTime, caffeine)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val napOverlaps: StateFlow<List<NapOverlap>> = combine(medications, temporalAnchorFlow) { meds, anchor ->
        if (anchor == null) emptyList()
        else napManager.checkNapOverlaps(meds, anchor)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eatingWindows: StateFlow<List<OptimalEatingWindow>> = combine(medications, temporalAnchorFlow, appetiteLogs, phosState) { meds, anchor, appetite, state ->
        if (anchor == null) emptyList()
        else mealScheduler.findOptimalEatingWindows(meds, anchor, appetite, if(state.hasMealPreferences()) state.mealPreferences else null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val optimizationSuggestions: StateFlow<List<OptimizationSuggestion>> = combine(
        healthGoals, medications, phosState, temporalAnchorFlow, nocturiaLogs, chronotype, metabolicLogs, adenosineState
    ) { array ->
        val goals = array[0] as List<HealthGoal>
        val meds = array[1] as List<MedicationRecord>
        val state = array[2] as PhosState
        val anchor = array[3] as TemporalAnchor?
        val nocturia = array[4] as List<NocturiaLog>
        val chrono = array[5] as ChronotypeRecord?
        val metabolic = array[6] as List<MetabolicLoadLog>
        val adenosine = array[7] as SleepPressureState?

        if (anchor == null) emptyList()
        else {
            val betaBlockerNames = listOf("metoprolol", "propranolol")
            val hasBetaBlocker = meds.any { med -> betaBlockerNames.any { med.name.lowercase().contains(it) } }
            
            val suggestions = goalOptimizationEngine.evaluateGoals(
                goals = goals, 
                medications = meds, 
                mealPreferences = if(state.hasMealPreferences()) state.mealPreferences else MealPreferences.getDefaultInstance(), 
                tWakeEpoch = anchor.wakeTime, 
                nocturiaCount = nocturia.size,
                chronotype = chrono?.type ?: Chronotype.NEUTRAL,
                metabolicLogs = metabolic,
                isOnBetaBlocker = hasBetaBlocker,
                giIrritantIds = giIrritantIds.value
            ).toMutableList()

            if (adenosine != null && adenosine.napPropensityScore > 80) {
                suggestions.add(OptimizationSuggestion(
                    goalId = -20,
                    id = "high_sleep_pressure",
                    title = "Critical Sleep Pressure",
                    description = "AI models show extreme homeostatic sleep pressure.",
                    suggestedMedicationShifts = emptyMap()
                ))
            }
            suggestions
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepCalibrationInsight: StateFlow<SleepCalibrationInsight?> = combine(sleepSubjectiveLogs, temporalAnchorFlow) { logs, anchor ->
        if (anchor == null || logs.isEmpty()) null
        else sleepCalibrationEngine.calibrate(8 * 3600000L, logs.first())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val medicationDepletions: StateFlow<List<String>> = combine(medications, nutrientDao.getAllDepletions()) { meds, rules ->
        NutrientAdvisoryEngine(CollisionResolver()).findDepletionWarnings(meds, rules)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nutrientReferences: StateFlow<List<NutrientReference>> = nutrientDao.getAllReferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mutable Insight Flows
    private val _travelProposal = MutableStateFlow<TravelProposal?>(null)
    val travelProposal: StateFlow<TravelProposal?> = _travelProposal.asStateFlow()

    private val _nutrientAdvisory = MutableStateFlow<NutrientAdvisory?>(null)
    val nutrientAdvisory: StateFlow<NutrientAdvisory?> = _nutrientAdvisory.asStateFlow()

    private val _betaBlockerInsights = MutableStateFlow<List<BetaBlockerInsight>>(emptyList())
    val betaBlockerInsights: StateFlow<List<BetaBlockerInsight>> = _betaBlockerInsights.asStateFlow()

    private val _sleepRestorationAudit = MutableStateFlow<SleepRestorationAudit?>(null)
    val sleepRestorationAudit: StateFlow<SleepRestorationAudit?> = _sleepRestorationAudit.asStateFlow()

    private val _dailyReadiness = MutableStateFlow<DailyReadiness?>(null)
    val dailyReadiness: StateFlow<DailyReadiness?> = _dailyReadiness.asStateFlow()

    private val _cardioMismatch = MutableStateFlow<CardioMismatchInsight?>(null)
    val cardioMismatch: StateFlow<CardioMismatchInsight?> = _cardioMismatch.asStateFlow()

    private val _hrrAudit = MutableStateFlow<HRRAudit?>(null)
    val hrrAudit: StateFlow<HRRAudit?> = _hrrAudit.asStateFlow()

    private val _neuroInsight = MutableStateFlow<NeuroCognitiveInsight?>(null)
    val neuroInsight: StateFlow<NeuroCognitiveInsight?> = _neuroInsight.asStateFlow()

    private val _thermalInsight = MutableStateFlow<ThermalInsight?>(null)
    val thermalInsight: StateFlow<ThermalInsight?> = _thermalInsight.asStateFlow()

    private val _voiceExtractedEntities = MutableStateFlow<ExtractedEntities?>(null)
    val voiceExtractedEntities: StateFlow<ExtractedEntities?> = _voiceExtractedEntities.asStateFlow()

    private val _prnAdvisory = MutableStateFlow<PRNAdvisory?>(null)
    val prnAdvisory: StateFlow<PRNAdvisory?> = _prnAdvisory.asStateFlow()

    private val _orthostaticInsight = MutableStateFlow<OrthostaticInsight?>(null)
    val orthostaticInsight: StateFlow<OrthostaticInsight?> = _orthostaticInsight.asStateFlow()

    private val _environmentalInsight = MutableStateFlow<EnvironmentalInsight?>(null)
    val environmentalInsight: StateFlow<EnvironmentalInsight?> = _environmentalInsight.asStateFlow()

    private val _hfInsight = MutableStateFlow<HFDecompensationInsight?>(null)
    val hfInsight: StateFlow<HFDecompensationInsight?> = _hfInsight.asStateFlow()

    private val _ebac = MutableStateFlow<Double>(0.0)
    val ebac: StateFlow<Double> = _ebac.asStateFlow()

    val postureRecommendation: StateFlow<PosturalRecommendation?> = flow {
        while (true) {
            val recentFood = interactionDao.getRecentFoodLogs(System.currentTimeMillis() - 2 * 3600000L)
            emit(postureIntelligence.checkPostPrandialPosture(recentFood))
            kotlinx.coroutines.delay(60000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            nanoEngine.initialize()
            seedKnowledgeBase()
            syncGait()
            syncChronotype()
            syncMetabolicLoad()
            syncBetaBlockerSafety()
            syncSleepRestorationAudit()
            syncCardioReadiness()
            syncHrrAudit()
            syncHFDecompensation()
            syncMetabolicClearance()
            syncPosture()
            syncEnvironmentalStrain()
            syncBioVelocity()
            
            launch {
                while (true) {
                    kotlinx.coroutines.delay(3600000)
                    syncSleepRestorationAudit()
                    syncCardioReadiness()
                    syncHrrAudit()
                    syncHFDecompensation()
                    syncMetabolicClearance()
                    syncPosture()
                    syncEnvironmentalStrain()
                    syncBioVelocity()
                }
            }
        }
    }

    private fun syncBioVelocity() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val birthYear = profile.birthYear ?: 1980
            
            val now = Instant.now()
            val rhrSamples = healthSyncManager.fetchRestingHeartRate(now.minus(7, ChronoUnit.DAYS), now) ?: emptyList()
            val hrvSamples = healthSyncManager.fetchHrv(now.minus(7, ChronoUnit.DAYS), now) ?: emptyList()
            
            if (rhrSamples.isEmpty() || hrvSamples.isEmpty()) return@launch
            
            val currentRhr = rhrSamples.map { it.beatsPerMinute }.average()
            val currentHrv = hrvSamples.map { it.value }.average()
            
            var baseline = bioVelocityDao.getBaseline()
            if (baseline == null) {
                baseline = BioBaseline(baselineHrv = currentHrv, baselineRhr = currentRhr, baselineSleepConsistency = 0.8)
                bioVelocityDao.upsertBaseline(baseline)
            }
            
            val doses = doseLogDao.getDosesInWindow(now.minus(7, ChronoUnit.DAYS).toEpochMilli(), now.toEpochMilli())
            val adherence = if (doses.isNotEmpty()) doses.count { it.status == "TAKEN" }.toDouble() / doses.size else 1.0
            
            val log = bioVelocityEngine.calculateBioVelocity(birthYear, currentHrv, currentRhr, baseline, adherence)
            bioVelocityDao.insertLog(log)
            _bioVelocityInsight.value = bioVelocityEngine.generateVelocityInsight(log)
        }
    }

    private fun syncEnvironmentalStrain() {
        viewModelScope.launch {
            val now = Instant.now()
            val envLogs = environmentalDao.getLogsSince(now.minus(24, ChronoUnit.HOURS))
            val rr = healthSyncManager.fetchRespiratoryRate(now.minus(12, ChronoUnit.HOURS), now) ?: emptyList()
            val spo2 = healthSyncManager.fetchOxygenSaturation(now.minus(12, ChronoUnit.HOURS), now) ?: emptyList()
            
            val metrics = rr.map { r ->
                NocturnalRespiratoryMetric(
                    timestamp = r.time,
                    respiratoryRate = r.rate,
                    sleepPosition = SleepPosition.FLAT,
                    oxygenSaturation = spo2.minByOrNull { abs(it.time.toEpochMilli() - r.time.toEpochMilli()) }?.percentage?.value ?: 95.0
                )
            }
            _environmentalInsight.value = environmentalCorrelationEngine.correlateRespiratoryStrain(metrics, envLogs)
        }
    }

    private fun syncPosture() {
        viewModelScope.launch {
            val now = Instant.now()
            val recentLogs = postureDao.getLogsSince(now.minus(1, ChronoUnit.HOURS))
            val pressureSamples = recentLogs.filter { it.type == PostureLogType.BAROMETRIC_PRESSURE }.sortedBy { it.timestamp }.map { it.value }
            val hasBetaBlocker = medications.value.any { it.name.lowercase().contains("metoprolol") || it.name.lowercase().contains("propranolol") }
            _orthostaticInsight.value = postureIntelligence.detectOrthostaticTransition(pressureSamples, hasBetaBlocker)
        }
    }

    private fun syncSleepRestorationAudit() {
        viewModelScope.launch {
            val hasBetaBlocker = medications.value.any { it.name.lowercase().contains("metoprolol") || it.name.lowercase().contains("propranolol") }
            if (!hasBetaBlocker) { _sleepRestorationAudit.value = null; return@launch }
            
            val sleepSamples = healthSyncManager.fetchSleepStages(Instant.now().minus(24, ChronoUnit.HOURS), Instant.now()) ?: emptyList()
            if (sleepSamples.isEmpty()) return@launch
            
            val mappedSamples = sleepSamples.map { 
                SleepStageSample(it.startTime, it.endTime, when(it.stage) {
                    androidx.health.connect.client.records.SleepSessionRecord.STAGE_TYPE_AWAKE -> SleepStage.AWAKE
                    androidx.health.connect.client.records.SleepSessionRecord.STAGE_TYPE_REM -> SleepStage.REM
                    androidx.health.connect.client.records.SleepSessionRecord.STAGE_TYPE_DEEP -> SleepStage.DEEP
                    else -> SleepStage.LIGHT
                })
            }
            val fragmentation = remSafetyEngine.calculateFragmentationIndex(mappedSamples)
            val avgIntensity = dreamDao.getLogsForDate(java.time.LocalDate.now().toString()).map { it.intensity }.average().toInt()
            _sleepRestorationAudit.value = remSafetyEngine.buildRestorationAudit(fragmentation, if(avgIntensity == 0) null else avgIntensity)
        }
    }

    private fun syncCardioReadiness() {
        viewModelScope.launch {
            val hasBetaBlocker = medications.value.any { it.name.lowercase().contains("metoprolol") || it.name.lowercase().contains("propranolol") }
            if (!hasBetaBlocker) { _dailyReadiness.value = null; return@launch }
            
            val rhrSamples = healthSyncManager.fetchRestingHeartRate(Instant.now().minus(24, ChronoUnit.HOURS), Instant.now()) ?: emptyList()
            val currentRhr = if (rhrSamples.isNotEmpty()) rhrSamples.map { it.beatsPerMinute }.average() else 60.0
            val sleepQuality = sleepSubjectiveLogs.value.firstOrNull()?.reportedQuality ?: 7
            
            _dailyReadiness.value = cardioMismatchEngine.calculateReadiness(45.0, 50.0, currentRhr, 62.0, sleepQuality)
            detectHeavyLegs(currentRhr)
        }
    }

    private fun detectHeavyLegs(rhr: Double) {
        viewModelScope.launch {
            val now = Instant.now()
            val steps = healthSyncManager.fetchStepsForSession(now.minus(1, ChronoUnit.HOURS), now)
            val hrSamples = healthSyncManager.fetchHeartRateForSession(now.minus(1, ChronoUnit.HOURS), now) ?: emptyList()
            
            if (steps > 1000 && hrSamples.isNotEmpty()) {
                val insight = cardioMismatchEngine.detectMismatch(now.toEpochMilli(), steps.toDouble() / 60.0, hrSamples.map { it.beatsPerMinute }.average(), rhr)
                _cardioMismatch.value = if (insight.isSignificant) insight else null
            }
        }
    }

    private fun syncHrrAudit() {
        viewModelScope.launch {
            val exercises = healthSyncManager.fetchRecentExercises() ?: return@launch
            val currentMedVersion = medications.value.maxByOrNull { it.validFrom }?.id ?: 0L
            
            exercises.forEach { session ->
                val hrSamples = healthSyncManager.fetchHeartRateForSession(session.endTime, session.endTime.plus(2, ChronoUnit.MINUTES)) ?: emptyList()
                if (hrSamples.isNotEmpty()) {
                    val peakHr = healthSyncManager.fetchHeartRateForSession(session.endTime.minus(30, ChronoUnit.SECONDS), session.endTime)?.maxOfOrNull { it.beatsPerMinute.toDouble() } ?: 120.0
                    val record = hrrOrchestrator.calculateHRR(session.endTime, hrSamples.map { BiometricLog(type = BiometricType.HEART_RATE, value = it.beatsPerMinute.toDouble(), timestamp = it.time) }, peakHr, currentMedVersion)
                    hrrDao.insertRecord(record)
                }
            }
            val latestRecord = hrrDao.getRecordsSince(java.time.LocalDate.now().toString()).firstOrNull()
            if (latestRecord != null) {
                val audit = hrrOrchestrator.buildHRRAudit(latestRecord, hrrDao.getRecordsSince(java.time.LocalDate.now().minusDays(7).toString()))
                _hrrAudit.value = audit
                if (audit?.isStrained == true) dataLayerRepository.updateAutonomicStrain(true)
            }
        }
    }

    private fun syncBetaBlockerSafety() {
        viewModelScope.launch {
            val betaBlockers = medications.value.filter { it.name.lowercase().contains("metoprolol") || it.name.lowercase().contains("propranolol") }
            if (betaBlockers.isEmpty()) { _betaBlockerInsights.value = emptyList(); return@launch }
            
            val anchor = temporalAnchorDao.getLatestAnchor() ?: return@launch
            val wakeTime = Instant.ofEpochMilli(anchor.wakeTime)
            val morningHr = healthSyncManager.fetchHeartRateForSession(wakeTime, wakeTime.plus(30, ChronoUnit.MINUTES)) ?: emptyList()
            val bradycardiaInsight = betaBlockerSafetyEngine.detectBradycardia(anchor, morningHr.map { it.beatsPerMinute })
            
            val bbDose = doseLogDao.getDosesInWindow(Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli(), Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS).toEpochMilli()).find { d -> betaBlockers.any { it.medicationId == d.medicationId } && d.status == "TAKEN" }
            val slumpInsight = bbDose?.actualTime?.let { actualTime ->
                val slumpHr = healthSyncManager.fetchHeartRateForSession(Instant.ofEpochMilli(actualTime).plus(5, ChronoUnit.HOURS), Instant.ofEpochMilli(actualTime).plus(7, ChronoUnit.HOURS)) ?: emptyList()
                val dailyHr = healthSyncManager.fetchHeartRateForSession(wakeTime, Instant.now()) ?: emptyList()
                betaBlockerSafetyEngine.detectFatigueSlump(actualTime, dailyHr.map { it.beatsPerMinute }.average(), slumpHr.map { it.beatsPerMinute })
            }
            _betaBlockerInsights.value = listOfNotNull(bradycardiaInsight, slumpInsight, betaBlockerSafetyEngine.suggestOxygenationBout(slumpInsight))
        }
    }

    private fun syncGait() { viewModelScope.launch { gaitManager.syncGaitMetrics() } }
    private fun syncChronotype() { viewModelScope.launch { val history = healthSyncManager.fetchSleepHistory(14); if (history != null) chronotypeDao.updateChronotype(chronotypeClassifier.classify(history)) } }
    
    private fun syncMetabolicLoad() {
        viewModelScope.launch {
            val exercises = healthSyncManager.fetchRecentExercises() ?: return@launch
            exercises.forEach { session ->
                val hr = healthSyncManager.fetchHeartRateForSession(session.startTime, session.endTime) ?: emptyList()
                if (session.exerciseType == 8) { // CYCLING
                    val effort = ebikeNormalizer.normalizeEffort(session, healthSyncManager.fetchPowerForSession(session.startTime, session.endTime) ?: emptyList(), hr, healthSyncManager.fetchCadenceForSession(session.startTime, session.endTime) ?: emptyList())
                    metabolicDao.insertLog(MetabolicLoadLog(exerciseSessionId = session.metadata.id, trimpScore = effort.normalizedCardioLoad, avgHeartRate = hr.map { it.beatsPerMinute }.average(), durationMinutes = java.time.Duration.between(session.startTime, session.endTime).toMinutes(), timestamp = session.endTime, isHyperMetabolic = effort.wasHighIntensity))
                } else metabolicDao.insertLog(metabolicEngine.calculateMetabolicLoad(session, hr))
            }
        }
    }

    private fun syncMetabolicClearance() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val recentLogs = alcoholDao.getLogsSince(Instant.now().minus(24, ChronoUnit.HOURS))
            _ebac.value = metabolicClearanceEngine.calculateEBAC(recentLogs, profile)
        }
    }

    private fun syncHFDecompensation() {
        viewModelScope.launch {
            val insight = hfDecompensationEngine.calculateFluidProxy(HFTrendData(18.5, 94.0, 72.0, 38.0), HFTrendData(16.0, 98.0, 65.0, 45.0))
            _hfInsight.value = insight
            if (insight.riskLevel == HFRiskLevel.ELEVATED || insight.riskLevel == HFRiskLevel.CRITICAL) dataLayerRepository.updateSafetyTightening(hfDecompensationEngine.getSafetyTighteningMillis(insight.riskLevel))
        }
    }

    fun processVoiceCommand(text: String, segments: List<SpeechSegment> = emptyList()) {
        viewModelScope.launch {
            val entities = voiceLogCoordinator.processVoiceCommand(text)
            _voiceExtractedEntities.value = entities
            if (segments.isNotEmpty()) _neuroInsight.value = neuroLoadEngine.correlateWithMeds(neuroLoadEngine.analyzeSpeech(segments, text), medications.value, doseLogDao.getDosesInWindow(System.currentTimeMillis() - 4 * 3600000L, System.currentTimeMillis()))
            if (entities.dreams.isNotEmpty()) syncSleepRestorationAudit()
        }
    }

    fun onConfirmHeavyLegs(insight: CardioMismatchInsight) {
        viewModelScope.launch {
            intelligenceDao.insertSymptom(SymptomLog(symptomName = "Heavy Legs", severity = (insight.mismatchIntensity * 10).toInt(), timestamp = Instant.ofEpochMilli(insight.timestamp)))
            _cardioMismatch.value = null
        }
    }

    fun addMedication(name: String, dosage: String, firstOffsetMillis: Long, frequency: Int = 1, foodRequirement: String = "NONE") {
        viewModelScope.launch {
            for (i in 0 until frequency) {
                val actualOffset = if (frequency == 1) firstOffsetMillis else firstOffsetMillis + (i * (15 * 3600000L / maxOf(1, frequency - 1)))
                medicationDao.insert(MedicationRecord(medicationId = "${name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}_$i", name = name, dosage = dosage, frequencyOffset = actualOffset, foodRequirement = foodRequirement, validFrom = System.currentTimeMillis()))
            }
        }
    }

    fun updateMedication(record: MedicationRecord) { viewModelScope.launch { medicationDao.deletePermanently(record.id); medicationDao.insert(record.copy(id = 0)) } }
    fun deleteMedication(id: Long) { viewModelScope.launch { medicationDao.deletePermanently(id) } }
    fun duplicateMedication(record: MedicationRecord) { viewModelScope.launch { medicationDao.insert(record.copy(id = 0, medicationId = "${record.name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}")) } }
    fun updateWakeTime(epochMillis: Long) { viewModelScope.launch { dataLayerRepository.updateTWake(epochMillis) } }
    fun toggleTimeFormat(is24Hour: Boolean) { viewModelScope.launch { getApplication<Application>().phosDataStore.updateData { it.toBuilder().setIs24Hour(is24Hour).build() } } }
    fun updateMealPreferences(bS: Long, bE: Long, lS: Long, lE: Long, dS: Long, dE: Long) { viewModelScope.launch { dataLayerRepository.updateMealPreferences(bS, bE, lS, lE, dS, dE) } }
    fun addHealthGoal(desc: String, symp: String, off: Long?) { viewModelScope.launch { goalDao.insertGoal(HealthGoal(description = desc, targetSymptom = symp, targetTimeOffset = off, targetTimeOfDay = null)) } }
    fun logSleepSubjective(q: Int, r: Int, m: String) { viewModelScope.launch { sleepSubjectiveDao.insertSubjectiveLog(SleepSubjectiveLog(reportedQuality = q, restfulnessRating = r, morningMood = m, date = java.time.LocalDate.now().toString())) } }
    fun requestPRNAdvisory(med: PRNMedication) { viewModelScope.launch { _prnAdvisory.value = prnAdvisorFlow.first().evaluateRequest(med, medications.value, interactionDao.getRecentFoodLogs(System.currentTimeMillis() - 2 * 3600000L)) } }
    fun clearPRNAdvisory() { _prnAdvisory.value = null }
    fun logPRNDose(med: PRNMedication) { viewModelScope.launch { doseLogDao.insertLog(DoseLog(medicationId = med.medicationId, scheduledTime = System.currentTimeMillis(), actualTime = System.currentTimeMillis(), status = "TAKEN", notes = "PRN Dose")); clearPRNAdvisory() } }
    fun logAppetite(h: Int, d: Int) { viewModelScope.launch { appetiteDao.insertAppetiteLog(AppetiteLog(hungerLevel = h, difficultyLevel = d)) } }
    fun logCaffeine(mg: Int, src: String) { viewModelScope.launch { caffeineDao.insertLog(CaffeineLog(mg = mg, source = src)) } }
    fun logFood(n: String, c: String, nut: NutrientFacts? = null) { viewModelScope.launch { interactionDao.insertFoodLog(FoodLog(foodId = n.lowercase().replace(" ", "_"), name = n, timestamp = System.currentTimeMillis(), nutrients = nut)) } }
    fun dismissInsight(id: String) { viewModelScope.launch { dismissedInsightDao.dismiss(DismissedInsight(id)) } }
    fun clearVoiceResults() { _voiceExtractedEntities.value = null; voiceManager.reset() }
    fun detectUpcomingTravel() { viewModelScope.launch { _travelProposal.value = jetLagManager.proposeAdvanceTitration("Tokyo", "Asia/Tokyo", Instant.now().plus(5, ChronoUnit.DAYS)) } }
    fun acceptTravelProposal(p: TravelProposal) { viewModelScope.launch { p.titrationSteps.firstOrNull()?.let { dataLayerRepository.updateTWake(it.targetWakeTime) }; _travelProposal.value = null } }
    fun dismissTravelProposal() { _travelProposal.value = null }
    fun requestNutrientAdvisory(n: String, nut: NutrientFacts) { viewModelScope.launch { _nutrientAdvisory.value = nutrientAdvisoryEngineFlow.first().evaluateFood(n, nut, allergenProfile.value, medications.value, temporalAnchorFlow.first()?.wakeTime ?: 0L) } }
    fun clearNutrientAdvisory() { _nutrientAdvisory.value = null }
    suspend fun parseNutritionTextWithNano(t: String): NutrientFacts? { val j = nanoEngine.parseNutritionText(t) ?: return null; return try { NutrientFacts(calories = Regex("\"calories\":\\s*(\\d+)").find(j)?.groupValues?.get(1)?.toInt() ?: 200, proteinG = Regex("\"proteinG\":\\s*([\\d.]+)").find(j)?.groupValues?.get(1)?.toDouble() ?: 10.0, calciumMg = Regex("\"calciumMg\":\\s*([\\d.]+)").find(j)?.groupValues?.get(1)?.toDouble() ?: 50.0, ingredients = if (j.contains("ingredients")) Regex("\"ingredients\":\\s*\\[(.*?)\\]").find(j)?.groupValues?.get(1)?.split(",")?.map { it.replace("\"", "").trim() } ?: emptyList() else emptyList()) } catch (e: Exception) { null } }
    suspend fun analyzeMealWithNano(b: android.graphics.Bitmap): com.phos.phone.ui.scanner.FoodScanResult? { val j = nanoEngine.analyzeMealImage(b) ?: return null; return try { com.phos.phone.ui.scanner.FoodScanResult(detectedName = Regex("\"detectedName\":\\s*\"(.*?)\"").find(j)?.groupValues?.get(1) ?: "Unknown Meal", category = Regex("\"category\":\\s*\"(.*?)\"").find(j)?.groupValues?.get(1) ?: "General", nutrients = NutrientFacts(calories = Regex("\"calories\":\\s*(\\d+)").find(j)?.groupValues?.get(1)?.toInt() ?: 400, proteinG = Regex("\"proteinG\":\\s*([\\d.]+)").find(j)?.groupValues?.get(1)?.toDouble() ?: 25.0, calciumMg = Regex("\"calciumMg\":\\s*([\\d.]+)").find(j)?.groupValues?.get(1)?.toDouble() ?: 50.0, ingredients = if (j.contains("ingredients")) Regex("\"ingredients\":\\s*\\[(.*?)\\]").find(j)?.groupValues?.get(1)?.split(",")?.map { it.replace("\"", "").trim() } ?: emptyList() else emptyList()), confidence = 1.0f) } catch (e: Exception) { null } }
    suspend fun analyzePillWithNano(b: android.graphics.Bitmap): com.phos.phone.ui.scanner.PillScanResult? { val j = nanoEngine.analyzePillImage(b) ?: return null; return try { com.phos.phone.ui.scanner.PillScanResult(detectedName = Regex("\"detectedName\":\\s*\"(.*?)\"").find(j)?.groupValues?.get(1), detectedDosage = Regex("\"detectedDosage\":\\s*\"(.*?)\"").find(j)?.groupValues?.get(1), detectedColor = Regex("\"detectedColor\":\\s*\"(.*?)\"").find(j)?.groupValues?.get(1), detectedShape = Regex("\"detectedShape\":\\s*\"(.*?)\"").find(j)?.groupValues?.get(1), frequencyDosesPerDay = Regex("\"frequencyDosesPerDay\":\\s*(\\d+)").find(j)?.groupValues?.get(1)?.toIntOrNull() ?: 1, confidence = 1.0f) } catch (e: Exception) { null } }
    
    private fun seedKnowledgeBase() {
        viewModelScope.launch {
            listOf(AbsorptionRule(medicationId = "sucralfate", requiredGapMinutes = 120, reason = "Take Sucralfate on an empty stomach."), AbsorptionRule(medicationId = "levothyroxine", requiredGapMinutes = 60, reason = "Take Levothyroxine 60 mins before other meds/food.")).forEach { interactionDao.insertAbsorptionRule(it) }
            listOf(SideEffectRule(medicationId = "lisinopril", sideEffect = "Dizziness/Cough", advice = "Monitor for persistent cough."), SideEffectRule(medicationId = "metoprolol", sideEffect = "Low Heart Rate", advice = "Watch for extreme fatigue.")).forEach { interactionDao.insertSideEffectRule(it) }
            interactionDao.insertRule(InteractionRule(sourceId = "grapefruit", targetId = "statin", gapMillis = 24 * 3600000L, reason = "Grapefruit inhibits metabolism of Statins.", severity = InteractionSeverity.CRITICAL))
            listOf(PRNMedication(medicationId = "ibuprofen_prn", name = "Ibuprofen", dosage = "400mg", maxDosesPer24h = 4, minGapMinutes = 240, reasonForUse = "Pain", validFrom = System.currentTimeMillis()), PRNMedication(medicationId = "albuterol_prn", name = "Albuterol", dosage = "2 puffs", maxDosesPer24h = 8, minGapMinutes = 15, reasonForUse = "Breath", validFrom = System.currentTimeMillis())).forEach { prnDao.insert(it) }
            listOf(AllergenProfile(allergenId = "dairy", displayName = "Dairy", severity = "MODERATE"), AllergenProfile(allergenId = "gluten", displayName = "Gluten", severity = "MODERATE")).forEach { allergenDao.insertAllergen(it) }
            listOf(NutrientReference(foodId = "yogurt", name = "Greek Yogurt", nutrients = NutrientFacts(calories = 150, proteinG = 15.0, calciumMg = 200.0, ingredients = listOf("Milk")), bestSources = "Dairy Aisle")).forEach { nutrientDao.insertReference(it) }
            listOf(MedicationInducedDepletion(medicationNamePattern = "statin", depletedNutrient = "CoQ10", advice = "Statins inhibit CoQ10 production.", foodSuggestions = listOf("Fish")), MedicationInducedDepletion(medicationNamePattern = "metformin", depletedNutrient = "B12", advice = "Metformin reduces B12 absorption.", foodSuggestions = listOf("Eggs"))).forEach { nutrientDao.insertDepletion(it) }
        }
    }

    override fun onCleared() { super.onCleared(); voiceManager.destroy() }
}
