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
    
    private val nanoEngine = injectedNanoEngine ?: GeminiNanoEngine(application)
    
    private val voiceParser = GeminiVoiceParser(nanoEngine)
    private val voiceLogCoordinator = VoiceLogCoordinator(
        doseLogDao, interactionDao, medicationDao, intelligenceDao, dreamDao, voiceParser
    )
    
    val voiceManager: VoiceManager = injectedVoiceManager ?: VoiceManager(application)

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
            syncPulsePowerEfficiency()
            syncNocturnalRespiratoryStrain()
            syncMetabolicClearance()
            syncPosture()
            
            // Periodic sync loop
            launch {
                while (true) {
                    kotlinx.coroutines.delay(3600000) // Hourly
                    syncSleepRestorationAudit()
                    syncCardioReadiness()
                    syncHrrAudit()
                    syncHFDecompensation()
                    syncPulsePowerEfficiency()
                    syncNocturnalRespiratoryStrain()
                    syncMetabolicClearance()
                    syncPosture()
                }
            }
        }
    }

    private fun syncPosture() {
        viewModelScope.launch {
            val now = Instant.now()
            val recentLogs = postureDao.getLogsSince(now.minus(1, java.time.temporal.ChronoUnit.HOURS))
            
            val pressureSamples = recentLogs
                .filter { it.type == PostureLogType.BAROMETRIC_PRESSURE }
                .sortedBy { it.timestamp }
                .map { it.value }
            
            val meds = medications.value
            val betaBlockerNames = listOf("metoprolol", "atenolol", "bisoprolol", "carvedilol", "propranolol")
            val hasBetaBlocker = meds.any { med -> betaBlockerNames.any { med.name.lowercase().contains(it) } }
            
            _orthostaticInsight.value = postureIntelligence.detectOrthostaticTransition(pressureSamples, hasBetaBlocker)
        }
    }

    fun logAlcohol(beverageType: BeverageType, abv: Double, volumeMl: Double) {
        viewModelScope.launch {
            alcoholDao.insertLog(AlcoholLog(
                timestamp = Instant.now(),
                beverageType = beverageType,
                abv = abv,
                volumeMl = volumeMl
            ))
            syncMetabolicClearance()
        }
    }

    fun updateUserProfile(weightKg: Double, gender: Gender) {
        viewModelScope.launch {
            userProfileDao.updateProfile(UserProfile(weightKg = weightKg, gender = gender))
            syncMetabolicClearance()
        }
    }

    private fun syncSleepRestorationAudit() {
        viewModelScope.launch {
            val meds = medications.value
            val betaBlockerNames = listOf("metoprolol", "atenolol", "bisoprolol", "carvedilol", "propranolol")
            val hasBetaBlocker = meds.any { med -> 
                betaBlockerNames.any { med.name.lowercase().contains(it) } 
            }
            
            if (!hasBetaBlocker) {
                _sleepRestorationAudit.value = null
                return@launch
            }
            
            val today = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE.withZone(java.time.ZoneId.systemDefault()).format(Instant.now())
            val sleepSamples = healthSyncManager.fetchSleepStages(Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS), Instant.now()) ?: emptyList()
            
            if (sleepSamples.isEmpty()) return@launch
            
            val mappedSamples = sleepSamples.map { 
                SleepStageSample(it.startTime, it.endTime, when(it.stage) {
                    androidx.health.connect.client.records.SleepStageRecord.STAGE_TYPE_AWAKE -> SleepStage.AWAKE
                    androidx.health.connect.client.records.SleepStageRecord.STAGE_TYPE_REM -> SleepStage.REM
                    androidx.health.connect.client.records.SleepStageRecord.STAGE_TYPE_DEEP -> SleepStage.DEEP
                    else -> SleepStage.LIGHT
                })
            }
            
            val fragmentation = remSafetyEngine.calculateFragmentationIndex(mappedSamples)
            val dreamLogs = dreamDao.getLogsForDate(today)
            val avgIntensity = if (dreamLogs.isNotEmpty()) dreamLogs.map { it.intensity }.average().toInt() else null
            
            _sleepRestorationAudit.value = remSafetyEngine.buildRestorationAudit(fragmentation, avgIntensity)
        }
    }

    fun logDreamJournal(text: String) {
        viewModelScope.launch {
            val json = nanoEngine.synthesizeDreamIntensity(text)
            val intensity = if (json != null) Regex("\"intensity\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toInt() ?: 5 else 5
            val vividness = if (json != null) Regex("\"vividness\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toInt() ?: 5 else 5
            
            val today = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE.withZone(java.time.ZoneId.systemDefault()).format(Instant.now())
            dreamDao.insertLog(DreamLog(date = today, rawText = text, intensity = intensity, vividness = vividness))
            
            // Refresh audit after logging a dream
            syncSleepRestorationAudit()
        }
    }

    private fun syncCardioReadiness() {
        viewModelScope.launch {
            val meds = medications.value
            val betaBlockerNames = listOf("metoprolol", "atenolol", "bisoprolol", "carvedilol", "propranolol")
            val hasBetaBlocker = meds.any { med -> 
                betaBlockerNames.any { med.name.lowercase().contains(it) } 
            }
            
            if (!hasBetaBlocker) {
                _dailyReadiness.value = null
                return@launch
            }
            
            val now = Instant.now()
            val startTime = now.minus(24, java.time.temporal.ChronoUnit.HOURS)
            
            val hrSamples = healthSyncManager.fetchHeartRateForSession(startTime, now) ?: emptyList()
            val rhrSamples = healthSyncManager.fetchRestingHeartRate(startTime, now) ?: emptyList()
            // Simulating HRV since HealthSyncManager might not have a direct fetcher yet
            val hrv = 45.0 // Default or simulated
            
            val currentRhr = if (rhrSamples.isNotEmpty()) rhrSamples.map { it.beatsPerMinute }.average() else 60.0
            val sleepLogs = sleepSubjectiveLogs.value
            val sleepQuality = if (sleepLogs.isNotEmpty()) sleepLogs.first().reportedQuality else 7
            
            val readiness = cardioMismatchEngine.calculateReadiness(
                hrv = hrv,
                avgHrv = 50.0,
                rhr = currentRhr,
                avgRhr = 62.0,
                sleepQuality = sleepQuality
            )
            
            _dailyReadiness.value = readiness
            detectHeavyLegs(currentRhr)
        }
    }

    private fun detectHeavyLegs(rhr: Double) {
        viewModelScope.launch {
            val now = Instant.now()
            val oneHourAgo = now.minus(1, java.time.temporal.ChronoUnit.HOURS)
            
            val steps = healthSyncManager.fetchStepsForSession(oneHourAgo, now)
            val hrSamples = healthSyncManager.fetchHeartRateForSession(oneHourAgo, now) ?: emptyList()
            
            if (steps > 1000 && hrSamples.isNotEmpty()) {
                val stepRate = steps.toDouble() / 60.0 // Rough average for the hour
                val avgHr = hrSamples.map { it.beatsPerMinute }.average()
                
                val insight = cardioMismatchEngine.detectMismatch(
                    timestamp = now.toEpochMilli(),
                    stepRate = stepRate,
                    heartRate = avgHr,
                    rhr = rhr
                )
                
                if (insight.isSignificant) {
                    _cardioMismatch.value = insight
                } else {
                    _cardioMismatch.value = null
                }
            }
        }
    }

    private fun syncHrrAudit() {
        viewModelScope.launch {
            val exercises = healthSyncManager.fetchRecentExercises() ?: return@launch
            val medications = medicationDao.getAllActiveMedications()
            
            // For simplicity, we correlate with the most recent medication version id
            val currentMedVersion = medications.maxByOrNull { it.validFrom }?.id ?: 0L
            
            exercises.forEach { session ->
                // Check if we already have a record for this workout
                // (In a real app, we'd check metadata.id in hrrDao)
                
                val hrSamples = healthSyncManager.fetchHeartRateForSession(
                    session.endTime,
                    session.endTime.plus(2, java.time.temporal.ChronoUnit.MINUTES)
                ) ?: emptyList()
                
                if (hrSamples.isNotEmpty()) {
                    val peakHrSamples = healthSyncManager.fetchHeartRateForSession(
                        session.endTime.minus(30, java.time.temporal.ChronoUnit.SECONDS),
                        session.endTime
                    ) ?: emptyList()
                    val peakHr = if (peakHrSamples.isNotEmpty()) peakHrSamples.maxOf { it.beatsPerMinute } else 120.0
                    
                    val record = hrrOrchestrator.calculateHRR(
                        endTime = session.endTime,
                        hrSamples = hrSamples.map { BiometricLog(type = BiometricType.HEART_RATE, value = it.beatsPerMinute, timestamp = it.time) },
                        peakHr = peakHr,
                        medicationVersion = currentMedVersion
                    )
                    hrrDao.insertRecord(record)
                }
            }
            
            val latestRecord = hrrDao.getRecordsSince(java.time.LocalDate.now().toString()).firstOrNull()
            if (latestRecord != null) {
                val history = hrrDao.getRecordsSince(java.time.LocalDate.now().minusDays(7).toString())
                val audit = hrrOrchestrator.buildHRRAudit(latestRecord, history)
                _hrrAudit.value = audit
                
                // Trigger Watch alert if strained (T38 M3)
                if (audit?.isStrained == true) {
                    dataLayerRepository.updateAutonomicStrain(true)
                }
            }
        }
    }

    private fun syncBetaBlockerSafety() {
        viewModelScope.launch {
            val meds = medications.value
            val betaBlockerNames = listOf("metoprolol", "atenolol", "bisoprolol", "carvedilol", "propranolol")
            val betaBlockers = meds.filter { med -> 
                betaBlockerNames.any { med.name.lowercase().contains(it) } 
            }
            
            if (betaBlockers.isEmpty()) {
                _betaBlockerInsights.value = emptyList()
                return@launch
            }
            
            val anchor = temporalAnchorDao.getLatestAnchor() ?: return@launch
            val wakeTime = Instant.ofEpochMilli(anchor.wakeTime)
            
            // M1: Idle Speed
            val morningHr = healthSyncManager.fetchHeartRateForSession(
                wakeTime, 
                wakeTime.plus(30, java.time.temporal.ChronoUnit.MINUTES)
            ) ?: emptyList()
            val bradycardiaInsight = betaBlockerSafetyEngine.detectBradycardia(anchor, morningHr.map { it.beatsPerMinute })
            
            // M2: 6-hour Slump
            val startOfDay = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS)
            val endOfDay = startOfDay.plus(1, java.time.temporal.ChronoUnit.DAYS)
            val todayDoses = doseLogDao.getDosesInWindow(startOfDay.toEpochMilli(), endOfDay.toEpochMilli())
            
            val bbDose = todayDoses.find { dose -> 
                betaBlockers.any { it.medicationId == dose.medicationId } && dose.status == "TAKEN" 
            }
            
            val slumpInsight = bbDose?.actualTime?.let { actualTime ->
                val doseInstant = Instant.ofEpochMilli(actualTime)
                val slumpStart = doseInstant.plus(5, java.time.temporal.ChronoUnit.HOURS).plus(30, java.time.temporal.ChronoUnit.MINUTES)
                val slumpEnd = doseInstant.plus(6, java.time.temporal.ChronoUnit.HOURS).plus(30, java.time.temporal.ChronoUnit.MINUTES)
                val slumpHr = healthSyncManager.fetchHeartRateForSession(slumpStart, slumpEnd) ?: emptyList()
                
                val dailyHr = healthSyncManager.fetchHeartRateForSession(wakeTime, Instant.now()) ?: emptyList()
                val dailyAvg = if (dailyHr.isNotEmpty()) dailyHr.map { it.beatsPerMinute }.average() else 0.0
                
                betaBlockerSafetyEngine.detectFatigueSlump(actualTime, dailyAvg, slumpHr.map { it.beatsPerMinute })
            }
            
            // M3: Oxygenation Reminder
            val reminder = betaBlockerSafetyEngine.suggestOxygenationBout(slumpInsight)
            
            _betaBlockerInsights.value = listOfNotNull(bradycardiaInsight, slumpInsight, reminder)
        }
    }

    private fun syncGait() {
        viewModelScope.launch {
            gaitManager.syncGaitMetrics()
        }
    }

    private fun syncChronotype() {
        viewModelScope.launch {
            val history = healthSyncManager.fetchSleepHistory(14)
            if (history != null) {
                val record = chronotypeClassifier.classify(history)
                chronotypeDao.updateChronotype(record)
            }
        }
    }

    private fun syncMetabolicLoad() {
        viewModelScope.launch {
            val exercises = healthSyncManager.fetchRecentExercises() ?: return@launch
            exercises.forEach { session ->
                val hr = healthSyncManager.fetchHeartRateForSession(session.startTime, session.endTime) ?: emptyList()
                
                if (session.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_CYCLING) {
                    val power = healthSyncManager.fetchPowerForSession(session.startTime, session.endTime) ?: emptyList()
                    val cadence = healthSyncManager.fetchCadenceForSession(session.startTime, session.endTime) ?: emptyList()
                    val effort = ebikeNormalizer.normalizeEffort(session, power, hr, cadence)
                    
                    // We store normalized load as TRIMP for consistent tracking
                    val log = MetabolicLoadLog(
                        exerciseSessionId = session.metadata.id,
                        trimpScore = effort.normalizedCardioLoad,
                        avgHeartRate = if (hr.isNotEmpty()) hr.map { it.beatsPerMinute }.average() else 0.0,
                        durationMinutes = java.time.Duration.between(session.startTime, session.endTime).toMinutes(),
                        timestamp = session.endTime,
                        isHyperMetabolic = effort.wasHighIntensity
                    )
                    metabolicDao.insertLog(log)
                } else {
                    val log = metabolicEngine.calculateMetabolicLoad(session, hr)
                    metabolicDao.insertLog(log)
                }
            }
        }
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

            // Seed PRN Medications
            val prnMedications = listOf(
                PRNMedication(medicationId = "ibuprofen_prn", name = "Ibuprofen", dosage = "400mg", maxDosesPer24h = 4, minGapMinutes = 240, reasonForUse = "Pain/Inflammation", validFrom = System.currentTimeMillis()),
                PRNMedication(medicationId = "albuterol_prn", name = "Albuterol Inhaler", dosage = "2 puffs", maxDosesPer24h = 8, minGapMinutes = 15, reasonForUse = "Shortness of Breath", validFrom = System.currentTimeMillis())
            )
            prnMedications.forEach { prnDao.insert(it) }
            
            // Seed Allergens
            val initialAllergens = listOf(
                AllergenProfile("dairy", "Dairy", "MODERATE"),
                AllergenProfile("gluten", "Gluten", "MODERATE")
            )
            initialAllergens.forEach { allergenDao.insertAllergen(it) }

            // Seed Nutrient References
            val foodRefs = listOf(
                NutrientReference("yogurt", "Greek Yogurt", NutrientFacts(calories = 150, proteinG = 15.0, calciumMg = 200.0, ingredients = listOf("Milk", "Live Cultures")), "Found in: Dairy Aisle"),
                NutrientReference("spinach", "Spinach", NutrientFacts(calories = 20, calciumMg = 100.0, ironMg = 2.0, ingredients = listOf("Spinach")), "Found in: Produce")
            )
            foodRefs.forEach { nutrientDao.insertReference(it) }

            // Seed Depletions
            val depletions = listOf(
                MedicationInducedDepletion(medicationNamePattern = "statin", depletedNutrient = "CoQ10", advice = "Statins inhibit the natural production of Coenzyme Q10.", foodSuggestions = listOf("Fatty Fish", "Organ Meats")),
                MedicationInducedDepletion(medicationNamePattern = "metformin", depletedNutrient = "Vitamin B12", advice = "Long-term Metformin use can reduce B12 absorption.", foodSuggestions = listOf("Eggs", "Dairy", "Fortified Cereals"))
            )
            depletions.forEach { nutrientDao.insertDepletion(it) }
        }
    }

    private val dismissedIds = dismissedInsightDao.getAllDismissedIds()
    val medications: StateFlow<List<MedicationRecord>> = medicationDao.getAllActiveMedicationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prnMedications: StateFlow<List<PRNMedication>> = prnDao.getAllActivePRNMedicationsFlow()
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

    val gaitInsights: StateFlow<List<String>> = flow {
        while (true) {
            val deviation = gaitManager.detectGaitDeviation()
            if (deviation != null && deviation.isSignificant) {
                val msg = "⚠️ Critical Gait Warning: Stride length has dropped by ${"%.1f".format(deviation.dropPercentage)}%. This may indicate dizziness or neuromotor side effects from recent medication changes. Please consult your doctor if you feel unsteady."
                emit(listOf(msg))
            } else {
                emit(emptyList())
            }
            kotlinx.coroutines.delay(3600000) // Check hourly
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptomLogs: StateFlow<List<SymptomLog>> = intelligenceDao.getSymptomsSince(Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caffeineLogs: StateFlow<List<CaffeineLog>> = caffeineDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adenosineState: StateFlow<SleepPressureState?> = combine(temporalAnchorFlow, caffeineLogs) { anchor, caffeine ->
        if (anchor == null) null
        else adenosineEngine.calculateCurrentState(anchor.wakeTime, caffeine)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val giProtectionInsights: StateFlow<List<String>> = combine(medications, giIrritantIds, symptomLogs) { meds, irritants, symptoms ->
        val culprits = giProtectionEngine.correlateStomachPain(meds, irritants, symptoms)
        if (culprits.isNotEmpty()) {
            val names = meds.filter { culprits.contains(it.medicationId) }.map { it.name }.distinct()
            listOf("Gastric Irritation Detected: Your recent stomach discomfort correlates with ${names.joinToString()}. We recommend ensuring these are taken with a full meal or a thick liquid (like yogurt) to buffer the lining.")
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertnessIntervention: StateFlow<AlertnessIntervention?> = combine(
        temporalAnchorFlow,
        sleepSubjectiveLogs,
        metabolicLogs
    ) { anchor, sleepLogs, metabolic ->
        if (anchor == null) null
        else {
            val history = healthSyncManager.fetchSleepHistory(7) ?: emptyList()
            val prompt = alertnessOrchestrator.buildPredictionPrompt(anchor.wakeTime, history, sleepLogs, metabolic)
            val json = nanoEngine.generateResponse(prompt) ?: return@combine null
            
            try {
                val isVulnerable = json.contains("\"isVulnerable\": true")
                val mins = Regex("\"predictedMinutesUntilDip\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toLong() ?: 0L
                val reason = Regex("\"reason\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1) ?: ""
                
                if (isVulnerable) {
                    AlertnessIntervention(
                        title = "AI Wakefulness Prediction",
                        description = reason,
                        suggestedActivity = "15-min E-Bike ride or Brisk Walk",
                        timeToDipMinutes = mins,
                        isHighRisk = sleepLogs.firstOrNull()?.let { it.reportedQuality <= 4 } ?: false
                    )
                } else null
            } catch (e: Exception) { null }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    val appetiteLogs: StateFlow<List<AppetiteLog>> = appetiteDao.getAppetiteLogsSince(Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eatingWindows: StateFlow<List<OptimalEatingWindow>> = combine(medications, temporalAnchorFlow, appetiteLogs, phosState) { meds, anchor, appetite, state ->
        if (anchor == null) emptyList()
        else mealScheduler.findOptimalEatingWindows(meds, anchor, appetite, if(state.hasMealPreferences()) state.mealPreferences else null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allergenProfile: StateFlow<List<AllergenProfile>> = allergenDao.getAllergensFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chronotype: StateFlow<ChronotypeRecord?> = chronotypeDao.getChronotypeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val healthGoals: StateFlow<List<HealthGoal>> = goalDao.getActiveGoalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nocturiaLogs: StateFlow<List<NocturiaLog>> = nocturiaDao.getNocturiaLogsSince(Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val optimizationSuggestions: StateFlow<List<OptimizationSuggestion>> = combine(
        healthGoals, medications, phosState, temporalAnchorFlow, nocturiaLogs, chronotype, metabolicLogs, adenosineState
    ) { goals, meds, state, anchor, nocturia, chrono, metabolic, adenosine ->
        if (anchor == null) emptyList()
        else {
            val betaBlockerNames = listOf("metoprolol", "atenolol", "bisoprolol", "carvedilol", "propranolol")
            val hasBetaBlocker = meds.any { med -> betaBlockerNames.any { med.name.lowercase().contains(it) } }
            
            val suggestions = goalOptimizationEngine.evaluateGoals(
                goals = goals, 
                medications = meds, 
                mealPreferences = if(state.hasMealPreferences()) state.mealPreferences else MealPreferences.getDefaultInstance(), 
                tWakeEpoch = anchor.wakeTime, 
                nocturiaCount = nocturia.size,
                chronotype = chrono?.type ?: Chronotype.NEUTRAL,
                metabolicLogs = metabolic,
                isOnBetaBlocker = hasBetaBlocker
            ).toMutableList()

            // 7. Adenosine / Nap Propensity Safety
            if (adenosine != null && adenosine.napPropensityScore > 80) {
                suggestions.add(OptimizationSuggestion(
                    goalId = -20,
                    id = "high_sleep_pressure",
                    title = "Critical Sleep Pressure",
                    description = "AI models show extreme homeostatic sleep pressure (Propensity: ${adenosine.napPropensityScore}%). To stay awake and safe, we recommend a 15-min E-Bike alertness micro-bout now.",
                    suggestedMedicationShifts = emptyMap()
                ))
            }
            
            suggestions
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepSubjectiveLogs: StateFlow<List<SleepSubjectiveLog>> = sleepSubjectiveDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val metabolicLogs: StateFlow<List<MetabolicLoadLog>> = metabolicDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sentimentLogs: StateFlow<List<SentimentLog>> = sentimentDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val burnoutRisk: StateFlow<BurnoutRisk> = combine(sentimentLogs, biometricDao.getLogsByTypeFlow(BiometricType.HRV)) { sentiment, biometrics ->
        stressSynthesisEngine.detectBurnoutRisk(sentiment, biometrics)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BurnoutRisk(0f, false, null))

    val sleepCalibrationInsight: StateFlow<SleepCalibrationInsight?> = combine(sleepSubjectiveLogs, temporalAnchorFlow) { logs, anchor ->
        if (anchor == null || logs.isEmpty()) null
        else {
            val latestSubjective = logs.first()
            // Simulating objective duration since we don't store it in Anchor yet (it's in HealthConnect)
            // In a real app we'd query HealthSyncManager for the matching night
            sleepCalibrationEngine.calibrate(8 * 3600000L, latestSubjective)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val medicationDepletions: StateFlow<List<String>> = combine(medications, nutrientDao.getAllDepletions()) { meds, rules ->
        val engine = NutrientAdvisoryEngine(CollisionResolver()) // Transient for utility
        engine.findDepletionWarnings(meds, rules)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nutrientReferences: StateFlow<List<NutrientReference>> = nutrientDao.getAllReferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val _hfInsight = MutableStateFlow<HFDecompensationInsight?>(null)
    val hfInsight: StateFlow<HFDecompensationInsight?> = _hfInsight.asStateFlow()

    private val _efficiencyInsight = MutableStateFlow<EfficiencyInsight?>(null)
    val efficiencyInsight: StateFlow<EfficiencyInsight?> = _efficiencyInsight.asStateFlow()

    private val _congestionInsight = MutableStateFlow<CongestionInsight?>(null)
    val congestionInsight: StateFlow<CongestionInsight?> = _congestionInsight.asStateFlow()

    private val _ebac = MutableStateFlow<Double>(0.0)
    val ebac: StateFlow<Double> = _ebac.asStateFlow()

    private val _orthostaticInsight = MutableStateFlow<OrthostaticInsight?>(null)
    val orthostaticInsight: StateFlow<OrthostaticInsight?> = _orthostaticInsight.asStateFlow()

    val alcoholLogs: StateFlow<List<AlcoholLog>> = alcoholDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = userProfileDao.getUserProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun syncMetabolicClearance() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val recentLogs = alcoholDao.getLogsSince(Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS))
            val currentBAC = metabolicClearanceEngine.calculateEBAC(recentLogs, profile)
            _ebac.value = currentBAC
            
            if (currentBAC > 0.05) {
                // High BAC Alert: Correlate with medications
                val activeMeds = medications.value.filter { it.metabolicPathway != null }
                if (activeMeds.isNotEmpty()) {
                    val names = activeMeds.map { it.name }.distinct()
                    // Propose delay for next doses if liver is monopolized (T42 M2)
                    // (Implementation detail: This would trigger a notification or timeline warning)
                }
            }
        }
    }

    private fun syncHFDecompensation() {
        viewModelScope.launch {
            val now = Instant.now()
            val weekAgo = now.minus(7, java.time.temporal.ChronoUnit.DAYS)
            
            // In a real app, we'd fetch these from healthSyncManager
            // For now we simulate trend data
            val current = HFTrendData(
                avgRespiratoryRate = 18.5,
                avgOxygenSaturation = 94.0,
                avgRestingHeartRate = 72.0,
                avgHrv = 38.0
            )
            val baseline = HFTrendData(
                avgRespiratoryRate = 16.0,
                avgOxygenSaturation = 98.0,
                avgRestingHeartRate = 65.0,
                avgHrv = 45.0
            )
            
            val insight = hfDecompensationEngine.calculateFluidProxy(current, baseline)
            _hfInsight.value = insight
            
            // Tighten diuretic safe-gaps if needed (T39 M3)
            if (insight.riskLevel == HFRiskLevel.ELEVATED || insight.riskLevel == HFRiskLevel.CRITICAL) {
                val adjustment = hfDecompensationEngine.getSafetyTighteningMillis(insight.riskLevel)
                dataLayerRepository.updateSafetyTightening(adjustment)
            }
        }
    }

    private fun syncPulsePowerEfficiency() {
        viewModelScope.launch {
            val now = Instant.now()
            val exercises = healthSyncManager.fetchRecentExercises() ?: return@launch
            val cyclingSessions = exercises.filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_CYCLING }
            
            if (cyclingSessions.isNotEmpty()) {
                val lastSession = cyclingSessions.first()
                val hr = healthSyncManager.fetchHeartRateForSession(lastSession.startTime, lastSession.endTime) ?: emptyList()
                val power = healthSyncManager.fetchPowerForSession(lastSession.startTime, lastSession.endTime) ?: emptyList()
                
                val metrics = power.mapNotNull { p ->
                    val matchingHr = hr.minByOrNull { Math.abs(it.time.toEpochMilli() - p.time.toEpochMilli()) }
                    matchingHr?.let { PulsePowerMetric(p.time, p.power, it.beatsPerMinute) }
                }
                
                val insight = pulsePowerEngine.calculateEfficiency(metrics, 1.0) // Assume 1.0 as baseline
                _efficiencyInsight.value = insight
            }
        }
    }

    private fun syncNocturnalRespiratoryStrain() {
        viewModelScope.launch {
            val now = Instant.now()
            val lastNight = now.minus(12, java.time.temporal.ChronoUnit.HOURS)
            
            val rr = healthSyncManager.fetchRespiratoryRate(lastNight, now) ?: emptyList()
            val spo2 = healthSyncManager.fetchOxygenSaturation(lastNight, now) ?: emptyList()
            
            // In a real app, we'd cross-reference with sleep position sensor data
            val metrics = rr.map { r ->
                NocturnalRespiratoryMetric(
                    timestamp = r.time,
                    respiratoryRate = r.rate,
                    sleepPosition = if (r.time.toEpochMilli() % 2 == 0L) SleepPosition.FLAT else SleepPosition.PROPPED_UP,
                    oxygenSaturation = spo2.minByOrNull { Math.abs(it.time.toEpochMilli() - r.time.toEpochMilli()) }?.value ?: 95.0
                )
            }
            
            _congestionInsight.value = nocturnalRespiratoryEngine.analyzeCongestion(metrics)
        }
    }

    fun detectUpcomingTravel() {
        viewModelScope.launch {
            _travelProposal.value = jetLagManager.proposeAdvanceTitration(
                destination = "Tokyo",
                targetZoneId = "Asia/Tokyo",
                travelDate = Instant.now().plus(5, java.time.temporal.ChronoUnit.DAYS)
            )
        }
    }

    fun acceptTravelProposal(proposal: TravelProposal) {
        viewModelScope.launch {
            proposal.titrationSteps.firstOrNull()?.let { step ->
                dataLayerRepository.updateTWake(step.targetWakeTime)
            }
            _travelProposal.value = null
        }
    }

    fun dismissTravelProposal() {
        _travelProposal.value = null
    }

    fun requestNutrientAdvisory(foodName: String, nutrients: NutrientFacts) {
        viewModelScope.launch {
            val engine = nutrientAdvisoryEngineFlow.first()
            val meds = medications.first()
            val allergens = allergenProfile.first()
            val anchor = temporalAnchorFlow.first() ?: return@launch
            
            _nutrientAdvisory.value = engine.evaluateFood(foodName, nutrients, allergens, meds, anchor.wakeTime)
        }
    }

    fun clearNutrientAdvisory() {
        _nutrientAdvisory.value = null
    }

    private val _voiceExtractedEntities = MutableStateFlow<ExtractedEntities?>(null)
    val voiceExtractedEntities: StateFlow<ExtractedEntities?> = _voiceExtractedEntities.asStateFlow()

    fun addMedication(name: String, dosage: String, firstOffsetMillis: Long, frequency: Int = 1, foodRequirement: String = "NONE") {
        viewModelScope.launch {
            // Strategy: Spread doses over a 15-hour active day
            val totalActiveDayMillis = 15 * 3600000L
            
            for (i in 0 until frequency) {
                val actualOffset = when (frequency) {
                    1 -> firstOffsetMillis
                    2 -> if (i == 0) firstOffsetMillis else firstOffsetMillis + totalActiveDayMillis
                    else -> firstOffsetMillis + (i * (totalActiveDayMillis / (frequency - 1)))
                }
                
                medicationDao.insert(
                    MedicationRecord(
                        medicationId = "${name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}_$i",
                        name = name,
                        dosage = dosage,
                        frequencyOffset = actualOffset,
                        foodRequirement = foodRequirement,
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
                    medicationId = "${record.name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}",
                    foodRequirement = record.foodRequirement
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

    fun updateMealPreferences(breakfastStart: Long, breakfastEnd: Long, lunchStart: Long, lunchEnd: Long, dinnerStart: Long, dinnerEnd: Long) {
        viewModelScope.launch {
            dataLayerRepository.updateMealPreferences(breakfastStart, breakfastEnd, lunchStart, lunchEnd, dinnerStart, dinnerEnd)
        }
    }

    fun addHealthGoal(description: String, targetSymptom: String, targetTimeOffset: Long?) {
        viewModelScope.launch {
            goalDao.insertGoal(HealthGoal(description = description, targetSymptom = targetSymptom, targetTimeOffset = targetTimeOffset, targetTimeOfDay = null))
        }
    }

    fun logSleepSubjective(quality: Int, restfulness: Int, mood: String) {
        viewModelScope.launch {
            val date = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE.withZone(java.time.ZoneId.systemDefault()).format(Instant.now())
            sleepSubjectiveDao.insertSubjectiveLog(SleepSubjectiveLog(reportedQuality = quality, restfulnessRating = restfulness, morningMood = mood, date = date))
        }
    }

    private val _prnAdvisory = MutableStateFlow<PRNAdvisory?>(null)
    val prnAdvisory: StateFlow<PRNAdvisory?> = _prnAdvisory.asStateFlow()

    fun requestPRNAdvisory(prnMed: PRNMedication) {
        viewModelScope.launch {
            val advisor = prnAdvisorFlow.first()
            val activeMeds = medications.first()
            val recentFood = interactionDao.getRecentFoodLogs(System.currentTimeMillis() - 2 * 3600000L)
            
            _prnAdvisory.value = advisor.evaluateRequest(prnMed, activeMeds, recentFood)
        }
    }

    fun clearPRNAdvisory() {
        _prnAdvisory.value = null
    }

    fun logPRNDose(prnMed: PRNMedication) {
        viewModelScope.launch {
            doseLogDao.insertLog(
                DoseLog(
                    medicationId = prnMed.medicationId,
                    scheduledTime = System.currentTimeMillis(),
                    actualTime = System.currentTimeMillis(),
                    status = "TAKEN",
                    notes = "PRN Dose for ${prnMed.reasonForUse}"
                )
            )
            clearPRNAdvisory()
        }
    }

    fun logAppetite(hunger: Int, difficulty: Int) {
        viewModelScope.launch {
            appetiteDao.insertAppetiteLog(AppetiteLog(hungerLevel = hunger, difficultyLevel = difficulty))
        }
    }

    fun logCaffeine(mg: Int, source: String) {
        viewModelScope.launch {
            caffeineDao.insertLog(CaffeineLog(mg = mg, source = source))
        }
    }

    fun logFood(name: String, category: String, nutrients: NutrientFacts? = null) {
        viewModelScope.launch {
            interactionDao.insertFoodLog(
                FoodLog(
                    foodId = name.lowercase().replace(" ", "_"),
                    name = name,
                    timestamp = System.currentTimeMillis(),
                    nutrients = nutrients
                )
            )
        }
    }

    fun processVoiceCommand(text: String) {
        viewModelScope.launch {
            val entities = voiceLogCoordinator.processVoiceCommand(text)
            _voiceExtractedEntities.value = entities
            processSentiment(text)
            
            // Refresh sleep audit if dreams were detected
            if (entities.dreams.isNotEmpty()) {
                syncSleepRestorationAudit()
            }
        }
    }

    private fun processSentiment(text: String) {
        viewModelScope.launch {
            val json = nanoEngine.calculateSentiment(text) ?: return@launch
            try {
                val score = Regex("\"score\":\\s*([\\d.-]+)").find(json)?.groupValues?.get(1)?.toFloat() ?: 0f
                val emotion = Regex("\"primaryEmotion\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1) ?: "Unknown"
                val intensity = Regex("\"intensity\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toInt() ?: 5
                
                sentimentDao.insertLog(SentimentLog(
                    text = text,
                    score = score,
                    primaryEmotion = emotion,
                    intensity = intensity
                ))
            } catch (e: Exception) {}
        }
    }

    fun clearVoiceResults() {
        _voiceExtractedEntities.value = null
        voiceManager.reset()
    }

    /**
     * Enhanced food parsing using Gemini Nano.
     */
    suspend fun parseNutritionTextWithNano(ocrText: String): NutrientFacts? {
        val json = nanoEngine.parseNutritionText(ocrText) ?: return null
        return try {
            // Simulated JSON parsing for Orchestration
            val calories = Regex("\"calories\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toInt() ?: 200
            val protein = Regex("\"proteinG\":\\s*([\\d.]+)").find(json)?.groupValues?.get(1)?.toDouble() ?: 10.0
            val calcium = Regex("\"calciumMg\":\\s*([\\d.]+)").find(json)?.groupValues?.get(1)?.toDouble() ?: 50.0
            val ingredients = if (json.contains("ingredients")) {
                 Regex("\"ingredients\":\\s*\\[(.*?)\\]").find(json)?.groupValues?.get(1)?.split(",")?.map { it.replace("\"", "").trim() } ?: emptyList()
            } else emptyList()

            NutrientFacts(
                calories = calories,
                proteinG = protein,
                calciumMg = calcium,
                ingredients = ingredients
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Multimodal meal analysis using Gemini Nano Vision.
     */
    suspend fun analyzeMealWithNano(bitmap: android.graphics.Bitmap): com.phos.phone.ui.scanner.FoodScanResult? {
        val json = nanoEngine.analyzeMealImage(bitmap) ?: return null
        return try {
            val name = Regex("\"detectedName\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1) ?: "Unknown Meal"
            val category = Regex("\"category\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1) ?: "General"
            
            // Re-using the logic from parseNutritionTextWithNano to extract nested nutrients
            val calories = Regex("\"calories\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toInt() ?: 400
            val protein = Regex("\"proteinG\":\\s*([\\d.]+)").find(json)?.groupValues?.get(1)?.toDouble() ?: 25.0
            val calcium = Regex("\"calciumMg\":\\s*([\\d.]+)").find(json)?.groupValues?.get(1)?.toDouble() ?: 50.0
            val ingredients = if (json.contains("ingredients")) {
                 Regex("\"ingredients\":\\s*\\[(.*?)\\]").find(json)?.groupValues?.get(1)?.split(",")?.map { it.replace("\"", "").trim() } ?: emptyList()
            } else emptyList()

            com.phos.phone.ui.scanner.FoodScanResult(
                detectedName = name,
                category = category,
                nutrients = NutrientFacts(calories = calories, proteinG = protein, calciumMg = calcium, ingredients = ingredients),
                confidence = 1.0f
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Multimodal pill/bottle analysis using Gemini Nano Vision.
     */
    suspend fun analyzePillWithNano(bitmap: android.graphics.Bitmap): com.phos.phone.ui.scanner.PillScanResult? {
        val json = nanoEngine.analyzePillImage(bitmap) ?: return null
        return try {
            val name = Regex("\"detectedName\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1)
            val dosage = Regex("\"detectedDosage\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1)
            val color = Regex("\"detectedColor\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1)
            val shape = Regex("\"detectedShape\":\\s*\"(.*?)\"").find(json)?.groupValues?.get(1)
            val freqStr = Regex("\"frequencyDosesPerDay\":\\s*(\\d+)").find(json)?.groupValues?.get(1)
            val freq = freqStr?.toIntOrNull() ?: 1

            com.phos.phone.ui.scanner.PillScanResult(
                detectedName = name,
                detectedDosage = dosage,
                detectedColor = color,
                detectedShape = shape,
                frequencyDosesPerDay = freq,
                confidence = 1.0f
            )
        } catch (e: Exception) {
            null
        }
    }

    fun onConfirmHeavyLegs(insight: CardioMismatchInsight) {
        viewModelScope.launch {
            intelligenceDao.insertSymptom(SymptomLog(
                symptomId = "heavy_legs_confirmed",
                name = "Heavy Legs (Physiological Mismatch)",
                severity = (insight.mismatchIntensity * 10).toInt(),
                timestamp = Instant.ofEpochMilli(insight.timestamp)
            ))
            _cardioMismatch.value = null // Clear after confirm
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
