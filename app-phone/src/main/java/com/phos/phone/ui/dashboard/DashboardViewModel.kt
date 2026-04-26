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

    private val healthSyncManager = HealthSyncManager(application)
    private val gaitManager = GaitManager(gaitDao, healthSyncManager)
    private val chronotypeClassifier = ChronotypeClassifier()
    private val metabolicEngine = MetabolicEngine()
    private val napManager = NapManager(healthSyncManager)
    private val postureIntelligence = PostureIntelligence()
    private val jetLagManager = JetLagManager()
    private val mealScheduler = MealScheduler()
    private val goalOptimizationEngine = GoalOptimizationEngine()
    private val sleepCalibrationEngine = SleepCalibrationEngine()
    
    private val nanoEngine = injectedNanoEngine ?: GeminiNanoEngine(application)
    
    private val voiceParser = GeminiVoiceParser(nanoEngine)
    private val voiceLogCoordinator = VoiceLogCoordinator(
        doseLogDao, interactionDao, medicationDao, intelligenceDao, voiceParser
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
                val log = metabolicEngine.calculateMetabolicLoad(session, hr)
                metabolicDao.insertLog(log)
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

    val healthGoals: StateFlow<List<HealthGoal>> = goalDao.getActiveGoalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nocturiaLogs: StateFlow<List<NocturiaLog>> = nocturiaDao.getNocturiaLogsSince(Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val optimizationSuggestions: StateFlow<List<OptimizationSuggestion>> = combine(healthGoals, medications, phosState, temporalAnchorFlow, nocturiaLogs, chronotype, metabolicLogs) { goals, meds, state, anchor, nocturia, chrono, metabolic ->
        if (anchor == null) emptyList()
        else goalOptimizationEngine.evaluateGoals(
            goals = goals, 
            medications = meds, 
            mealPreferences = if(state.hasMealPreferences()) state.mealPreferences else MealPreferences.getDefaultInstance(), 
            tWakeEpoch = anchor.wakeTime, 
            nocturiaCount = nocturia.size,
            chronotype = chrono?.type ?: Chronotype.NEUTRAL,
            metabolicLogs = metabolic
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepSubjectiveLogs: StateFlow<List<SleepSubjectiveLog>> = sleepSubjectiveDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val metabolicLogs: StateFlow<List<MetabolicLoadLog>> = metabolicDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            _voiceExtractedEntities.value = voiceLogCoordinator.processVoiceCommand(text)
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

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
