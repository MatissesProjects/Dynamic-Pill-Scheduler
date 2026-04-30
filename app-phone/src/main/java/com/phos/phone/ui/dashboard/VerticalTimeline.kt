package com.phos.phone.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.phos.core.data.engine.*
import com.phos.core.data.model.*
import com.phos.core.data.proto.PhosState
import com.phos.core.intelligence.*
import com.phos.phone.ui.scanner.PillScanResult
import com.phos.phone.ui.scanner.PillScannerScreen
import com.phos.phone.ui.scanner.FoodScanResult
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    medications: List<MedicationRecord>,
    prnMedications: List<PRNMedication>,
    phosState: PhosState,
    healthInsights: List<String>,
    sideEffectAlerts: List<SideEffectRule>,
    betaBlockerInsights: List<BetaBlockerInsight> = emptyList(),
    napOverlaps: List<NapOverlap>,
    postureRecommendation: PosturalRecommendation?,
    prnAdvisory: PRNAdvisory?,
    travelProposal: TravelProposal?,
    eatingWindows: List<OptimalEatingWindow>,
    nutrientAdvisory: NutrientAdvisory?,
    medicationDepletions: List<String>,
    nutrientReferences: List<NutrientReference>,
    healthGoals: List<HealthGoal>,
    optimizationSuggestions: List<OptimizationSuggestion>,
    hormonalHarmony: HormonalHarmonyReport?,
    sleepRestorationAudit: SleepRestorationAudit?,
    dailyReadiness: DailyReadiness?,
    cardioMismatch: CardioMismatchInsight?,
    hrrAudit: HRRAudit?,
    sleepCalibrationInsight: SleepCalibrationInsight?,
    sleepSubjectiveLogs: List<SleepSubjectiveLog>,
    neuroInsight: NeuroCognitiveInsight?,
    thermalInsight: ThermalInsight?,
    bioVelocityLogs: List<BioVelocityLog> = emptyList(),
    bioVelocityInsight: String? = null,
    safetyStatus: SafetyStatus = SafetyStatus.GREEN,
    safetyProof: ZkpPayload? = null,
    voiceState: VoiceState,
    voiceExtractedEntities: ExtractedEntities?,
    onAddMedication: (String, String, Long, Int, String) -> Unit,
    onUpdateMedication: (MedicationRecord) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onDuplicateMedication: (MedicationRecord) -> Unit,
    onUpdateWakeTime: (Long) -> Unit,
    onToggleTimeFormat: (Boolean) -> Unit,
    onDismissInsight: (String) -> Unit,
    onConfirmHeavyLegs: (CardioMismatchInsight) -> Unit,
    onRequestPRNAdvisory: (PRNMedication) -> Unit,
    onLogPRNDose: (PRNMedication) -> Unit,
    onClearPRNAdvisory: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onProcessVoiceCommand: (String, List<SpeechSegment>) -> Unit,
    onClearVoiceResults: () -> Unit,
    onAcceptTravelProposal: (TravelProposal) -> Unit,
    onDismissTravelProposal: () -> Unit,
    onDetectTravel: () -> Unit,
    onLogAppetite: (Int, Int) -> Unit,
    onLogFood: (String, String, NutrientFacts?) -> Unit,
    onRequestNutrientAdvisory: (String, NutrientFacts) -> Unit,
    onClearNutrientAdvisory: () -> Unit,
    onAddHealthGoal: (String, String, Long?) -> Unit,
    onUpdateMealPreferences: (Long, Long, Long, Long, Long, Long) -> Unit,
    onLogSleepSubjective: (Int, Int, String) -> Unit,
    onGenerateSafetyProof: () -> Unit = {},
    aiTextParser: (suspend (String) -> NutrientFacts?)? = null,
    aiVisionParser: (suspend (Bitmap) -> FoodScanResult?)? = null,
    aiPillVisionParser: (suspend (Bitmap) -> PillScanResult?)? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAppetiteDialog by remember { mutableStateOf(false) }
    var prefilledName by remember { mutableStateOf("") }
    var prefilledDosage by remember { mutableStateOf("") }
    var prefilledFrequency by remember { mutableStateOf(1) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var lastScannedFoodResult by remember { mutableStateOf<FoodScanResult?>(null) }

    val context = LocalContext.current
    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    var showSleepCheckIn by remember { mutableStateOf(false) }
    LaunchedEffect(sleepSubjectiveLogs) {
        val today = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(Instant.now())
        if (sleepSubjectiveLogs.none { it.date == today }) showSleepCheckIn = true
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Timeline, null) }, label = { Text("Timeline") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.MedicalServices, null) }, label = { Text("PRN") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.CameraAlt, null) }, label = { Text("Scanner") })
                NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Default.Restaurant, null) }, label = { Text("Meals") })
                NavigationBarItem(selected = selectedTab == 4, onClick = { selectedTab = 4 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(onClick = { showAppetiteDialog = true }, containerColor = MaterialTheme.colorScheme.tertiaryContainer) { Icon(Icons.Default.Fastfood, null) }
                    FloatingActionButton(onClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) { if (voiceState is VoiceState.Listening) onStopVoiceListening() else onStartVoiceListening() } else audioLauncher.launch(Manifest.permission.RECORD_AUDIO) }, containerColor = if (voiceState is VoiceState.Listening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer) { Icon(if (voiceState is VoiceState.Listening) Icons.Default.MicOff else Icons.Default.Mic, null) }
                    FloatingActionButton(onClick = { prefilledName = ""; prefilledDosage = ""; prefilledFrequency = 1; showAddDialog = true }) { Icon(Icons.Default.Add, null) }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().statusBarsPadding()) {
            when (selectedTab) {
                0 -> VerticalTimeline(
                    medications = medications,
                    tWakeEpoch = phosState.tWakeEpoch,
                    wasInterrupted = phosState.wasInterrupted,
                    lastAiInsight = phosState.lastAiInsight,
                    is24Hour = phosState.is24Hour,
                    healthInsights = healthInsights,
                    sideEffectAlerts = sideEffectAlerts,
                    betaBlockerInsights = betaBlockerInsights,
                    napOverlaps = napOverlaps,
                    postureRecommendation = postureRecommendation,
                    travelProposal = travelProposal,
                    optimizationSuggestions = optimizationSuggestions,
                    hormonalHarmony = hormonalHarmony,
                    sleepRestorationAudit = sleepRestorationAudit,
                    dailyReadiness = dailyReadiness,
                    cardioMismatch = cardioMismatch,
                    hrrAudit = hrrAudit,
                    sleepCalibrationInsight = sleepCalibrationInsight,
                    neuroInsight = neuroInsight,
                    thermalInsight = thermalInsight,
                    bioVelocityLogs = bioVelocityLogs,
                    bioVelocityInsight = bioVelocityInsight,
                    safetyStatus = safetyStatus,
                    safetyProof = safetyProof,
                    onUpdateMedication = onUpdateMedication,
                    onDeleteMedication = onDeleteMedication,
                    onDuplicateMedication = onDuplicateMedication,
                    onUpdateWakeTime = onUpdateWakeTime,
                    onDismissInsight = onDismissInsight,
                    onAcceptTravelProposal = onAcceptTravelProposal,
                    onDismissTravelProposal = onDismissTravelProposal,
                    onConfirmHeavyLegs = onConfirmHeavyLegs,
                    onGenerateSafetyProof = onGenerateSafetyProof
                )
                1 -> PRNList(prnMedications, onRequestPRNAdvisory)
                2 -> PillScannerScreen(onPillScanned = { result -> prefilledName = result.detectedName ?: "Pill"; prefilledDosage = result.detectedDosage ?: ""; prefilledFrequency = result.frequencyDosesPerDay; showAddDialog = true; selectedTab = 0 }, onFoodScanned = { result -> lastScannedFoodResult = result; if (result.nutrients != null) onRequestNutrientAdvisory(result.detectedName ?: "Food", result.nutrients!!) else { onLogFood(result.detectedName ?: "Food", result.category ?: "General", null); selectedTab = 3 } }, aiTextParser = aiTextParser, aiVisionParser = aiVisionParser, aiPillVisionParser = aiPillVisionParser)
                3 -> MealSyncDashboard(eatingWindows, phosState.is24Hour, medicationDepletions, nutrientReferences)
                4 -> SettingsScreen(phosState, onToggleTimeFormat, onDetectTravel, healthGoals, { showGoalDialog = true }, onUpdateMealPreferences)
            }
            AnimatedVisibility(visible = voiceState !is VoiceState.Idle, enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
                VoiceOverlay(state = voiceState, extractedEntities = voiceExtractedEntities, neuroInsight = neuroInsight, onProcess = onProcessVoiceCommand, onDismiss = onClearVoiceResults)
            }
        }
    }
    if (showSleepCheckIn) SleepCheckInDialog({ showSleepCheckIn = false }, { q, r, m -> onLogSleepSubjective(q, r, m); showSleepCheckIn = false })
    if (showAppetiteDialog) AppetiteLogDialog({ showAppetiteDialog = false }, onLogAppetite)
    if (showGoalDialog) AddGoalDialog({ showGoalDialog = false }, onAddHealthGoal)
    prnAdvisory?.let { adv -> PRNAdvisoryDialog(adv, onClearPRNAdvisory) { prnMedications.find { m -> adv.reason.contains(m.name) }?.let { onLogPRNDose(it) } ?: onClearPRNAdvisory() } }
    nutrientAdvisory?.let { adv -> NutrientAdvisoryDialog(adv, onClearNutrientAdvisory) { lastScannedFoodResult?.let { onLogFood(it.detectedName ?: "Food", it.category ?: "General", it.nutrients) }; onClearNutrientAdvisory(); selectedTab = 3 } }
    if (showAddDialog) AddMedicationDialog(prefilledName, prefilledDosage, "1", prefilledFrequency, "NONE", { showAddDialog = false }, onAddMedication)
}

@Composable
fun VerticalTimeline(
    medications: List<MedicationRecord>,
    tWakeEpoch: Long,
    wasInterrupted: Boolean,
    lastAiInsight: String,
    is24Hour: Boolean,
    healthInsights: List<String>,
    sideEffectAlerts: List<SideEffectRule>,
    betaBlockerInsights: List<BetaBlockerInsight>,
    napOverlaps: List<NapOverlap>,
    postureRecommendation: PosturalRecommendation?,
    travelProposal: TravelProposal?,
    optimizationSuggestions: List<OptimizationSuggestion>,
    hormonalHarmony: HormonalHarmonyReport?,
    sleepRestorationAudit: SleepRestorationAudit?,
    dailyReadiness: DailyReadiness?,
    cardioMismatch: CardioMismatchInsight?,
    hrrAudit: HRRAudit?,
    sleepCalibrationInsight: SleepCalibrationInsight?,
    neuroInsight: NeuroCognitiveInsight?,
    thermalInsight: ThermalInsight?,
    bioVelocityLogs: List<BioVelocityLog> = emptyList(),
    bioVelocityInsight: String? = null,
    safetyStatus: SafetyStatus = SafetyStatus.GREEN,
    safetyProof: ZkpPayload? = null,
    onUpdateMedication: (MedicationRecord) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onDuplicateMedication: (MedicationRecord) -> Unit,
    onUpdateWakeTime: (Long) -> Unit,
    onDismissInsight: (String) -> Unit,
    onAcceptTravelProposal: (TravelProposal) -> Unit,
    onDismissTravelProposal: () -> Unit,
    onConfirmHeavyLegs: (CardioMismatchInsight) -> Unit,
    onGenerateSafetyProof: () -> Unit = {}
) {
    val timeFormatter = DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a").withZone(ZoneId.systemDefault())
    var showTimePicker by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<MedicationRecord?>(null) }
    var showProofDialog by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Spacer(Modifier.height(8.dp)); WakeTimeHeader(tWakeEpoch, timeFormatter) { showTimePicker = true } }
        item { 
            SafetyStatusCard(safetyStatus) { 
                onGenerateSafetyProof()
                showProofDialog = true
            } 
        }
        if (wasInterrupted) item { InsightCard("Sleep Session Bridged", "Detected and bridged short wake gaps.", Icons.Default.History) { } }
        sleepCalibrationInsight?.let { item { InsightCard(it.title, it.description, Icons.Default.Bedtime, MaterialTheme.colorScheme.tertiaryContainer) { } } }
        optimizationSuggestions.forEach { item { InsightCard("Goal Optimization", it.description, Icons.Default.ModelTraining, MaterialTheme.colorScheme.tertiaryContainer) { } } }
        if (lastAiInsight.isNotEmpty()) item { InsightCard("AI Baseline Insight", lastAiInsight, Icons.Default.AutoAwesome) { onDismissInsight("baseline") } }
        hormonalHarmony?.let { report -> item { InsightCard("Hormonal Harmony: ${report.alignmentScore}/100", if (report.alerts.isEmpty()) "Perfectly aligned." else report.alerts.joinToString("\n") { "• ${it.message}" }, Icons.Default.SelfImprovement, when { report.alignmentScore > 80 -> MaterialTheme.colorScheme.tertiaryContainer; report.alignmentScore > 50 -> MaterialTheme.colorScheme.secondaryContainer; else -> MaterialTheme.colorScheme.errorContainer }) { } } }
        sleepRestorationAudit?.let { item { InsightCard("Sleep Restoration Audit: ${it.remStabilityScore}/100", it.restorationMessage, Icons.Default.Bedtime) { onDismissInsight("sleep_audit_${it.date}") } } }
        dailyReadiness?.let { item { InsightCard("Daily Readiness: ${it.score}/100", it.recommendation, Icons.Default.Bolt) { onDismissInsight("readiness_${it.date}") } } }
        hrrAudit?.let { audit -> item { InsightCard("Heart Rate Recovery Audit", audit.advice, if (audit.isStrained) Icons.Default.Warning else Icons.Default.History, if (audit.isStrained) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer) { onDismissInsight("hrr_audit_${audit.date}") } } }
        cardioMismatch?.let { mismatch -> item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.HeartBroken, null, tint = Color.Red); Spacer(Modifier.width(8.dp)); Text("Muscle-Heart Mismatch", fontWeight = FontWeight.Bold) }; Text("Step Rate: ${mismatch.stepRate} HR: ${mismatch.heartRate}", style = MaterialTheme.typography.bodySmall); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = { onDismissInsight("mismatch") }) { Text("Dismiss") }; Button(onClick = { onConfirmHeavyLegs(mismatch) }) { Text("Confirm Heavy Legs") } } } } } }
        travelProposal?.let { p -> item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(16.dp)) { Text("Travel Detected: ${p.destination}", fontWeight = FontWeight.Bold); Text(p.explanation ?: "", style = MaterialTheme.typography.bodySmall); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismissTravelProposal) { Text("Dismiss") }; Button(onClick = { onAcceptTravelProposal(p) }) { Text("Accept") } } } } } }
        postureRecommendation?.let { item { InsightCard(it.title, it.recommendation, Icons.Default.VerticalAlignTop) { onDismissInsight("posture") } } }
        thermalInsight?.let { item { if(it.riskLevel != ThermalRiskLevel.LOW) InsightCard("Thermal Strain: ${it.riskLevel}", it.advice ?: "", Icons.Default.Thermostat, if(it.riskLevel == ThermalRiskLevel.CRITICAL) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) { onDismissInsight("thermal") } } }
        bioVelocityLogs.firstOrNull()?.let { log -> item { InsightCard("Biological Age: ${"%.1f".format(log.biologicalAge)}", bioVelocityInsight ?: "Aging at ${"%.2f".format(log.paceOfAging)}x speed. Adherence impact: ${"%.1f".format(log.adherenceImpact)}y saved.", Icons.Default.Timeline, when { log.paceOfAging < 0.9 -> MaterialTheme.colorScheme.tertiaryContainer; log.paceOfAging > 1.1 -> MaterialTheme.colorScheme.errorContainer; else -> MaterialTheme.colorScheme.secondaryContainer }) { onDismissInsight("bio_velocity") } } }
        napOverlaps.forEach { item { InsightCard("Nap Detected: ${it.medicationName} Shift", "Suggested shift: ${it.suggestedShiftMillis/60000} mins.", Icons.Default.Bedtime) { onDismissInsight("nap_${it.medicationId}") } } }
        healthInsights.forEach { item { InsightCard("Absorption Spacing", it, Icons.Default.Info) { onDismissInsight("absorption") } } }
        sideEffectAlerts.forEach { item { InsightCard("Side Effect Watch: ${it.sideEffect}", it.advice, Icons.Default.Warning, MaterialTheme.colorScheme.errorContainer) { onDismissInsight("side_effect") } } }
        betaBlockerInsights.forEach { item { InsightCard(it.title, it.description, when(it.type) { BetaBlockerInsightType.BRADYCARDIA -> Icons.Default.Warning; BetaBlockerInsightType.FATIGUE_SLUMP -> Icons.Default.BatteryAlert; else -> Icons.Default.DirectionsWalk }, if(it.isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) { onDismissInsight("bb_${it.type}") } } }
        
        if (medications.isEmpty()) item { Text("No medications scheduled.", Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center) }
        items(medications.sortedBy { it.frequencyOffset }) { TimelineItem(it, tWakeEpoch, timeFormatter, { editingMedication = it }, { onDuplicateMedication(it) }, { onDeleteMedication(it.id) }) }
        item { Spacer(Modifier.height(80.dp)) }
    }
    if (showTimePicker) WakeTimePickerDialog(tWakeEpoch, is24Hour, { showTimePicker = false }, { onUpdateWakeTime(it); showTimePicker = false })
    editingMedication?.let { med -> AddMedicationDialog(med.name, med.dosage, (med.frequencyOffset/3600000.0).toString(), 1, med.foodRequirement, { editingMedication = null }, { n, d, o, f, fr -> onUpdateMedication(med.copy(name = n, dosage = d, frequencyOffset = o, foodRequirement = fr)); editingMedication = null }) }
    if (showProofDialog && safetyProof != null) {
        AlertDialog(
            onDismissRequest = { showProofDialog = false },
            title = { Text("Care Mesh Proof") },
            text = { 
                Column {
                    Text("This proof mathematically verifies your ${safetyProof.status} status without revealing your raw health data.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Commitment: ${safetyProof.commitment.take(16)}...", fontWeight = FontWeight.Bold)
                    Text("Proof Z: ${safetyProof.proofZ.take(16)}...", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = { Button({ showProofDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
fun SafetyStatusCard(status: SafetyStatus, onShare: () -> Unit) {
    val color = when (status) {
        SafetyStatus.GREEN -> Color(0xFF4CAF50)
        SafetyStatus.YELLOW -> Color(0xFFFFC107)
        SafetyStatus.RED -> Color(0xFFF44336)
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).background(color, CircleShape))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Safety Status: $status", fontWeight = FontWeight.Bold)
                Text("Privacy-Preserving Care Mesh Active", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onShare) { Icon(Icons.Default.Share, null) }
        }
    }
}

@Composable
fun SettingsScreen(state: PhosState, onToggleTimeFormat: (Boolean) -> Unit, onDetectTravel: () -> Unit, healthGoals: List<HealthGoal>, onAddGoal: () -> Unit, onUpdateMeals: (Long, Long, Long, Long, Long, Long) -> Unit, onGenerateSafetyProof: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ListItem(headlineContent = { Text("24-Hour Format") }, trailingContent = { Switch(state.is24Hour, onToggleTimeFormat) })
        Divider()
        Button(onClick = onDetectTravel, Modifier.fillMaxWidth()) { Icon(Icons.Default.Flight, null); Spacer(Modifier.width(8.dp)); Text("Detect Travel") }
        Divider()
        Text("Care Mesh", fontWeight = FontWeight.Bold)
        Button(onClick = onGenerateSafetyProof, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { 
            Icon(Icons.Default.Security, null)
            Spacer(Modifier.width(8.dp))
            Text("Generate Privacy-Preserving Proof") 
        }
        Divider()
        Text("Goals", fontWeight = FontWeight.Bold); healthGoals.forEach { Text("• ${it.description}", style = MaterialTheme.typography.bodySmall) }
        Button(onClick = onAddGoal, Modifier.fillMaxWidth()) { Icon(Icons.Default.AddTask, null); Text("Add Goal") }
        Divider()
        Text("Meal Prefs (ms offset)", fontWeight = FontWeight.Bold)
        var bS by remember { mutableStateOf((state.mealPreferences?.breakfastStartOffset ?: 0L).toString()) }
        var bE by remember { mutableStateOf((state.mealPreferences?.breakfastEndOffset ?: 3600000L).toString()) }
        Row { OutlinedTextField(bS, {bS=it}, label={Text("BStart")}, modifier=Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); OutlinedTextField(bE, {bE=it}, label={Text("BEnd")}, modifier=Modifier.weight(1f)) }
        Button(onClick = { onUpdateMeals(bS.toLong(), bE.toLong(), 0, 0, 0, 0) }, Modifier.fillMaxWidth()) { Text("Save") }
    }
}

@Composable
fun VoiceOverlay(state: VoiceState, extractedEntities: ExtractedEntities?, neuroInsight: NeuroCognitiveInsight?, onProcess: (String, List<com.phos.core.intelligence.SpeechSegment>) -> Unit, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable { onDismiss() }, contentAlignment = Alignment.BottomCenter) {
        Card(Modifier.fillMaxWidth().padding(16.dp).clickable(false) {}, shape = MaterialTheme.shapes.extraLarge) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                when (state) {
                    is VoiceState.Listening -> { Text("Listening...", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(16.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) }
                    is VoiceState.Success -> { Text("Processing...", style = MaterialTheme.typography.bodyLarge); LaunchedEffect(state.text) { onProcess(state.text, state.segments) } }
                    is VoiceState.Error -> { Icon(Icons.Default.Error, null, tint = Color.Red); Text(state.message); Button(onDismiss) { Text("Dismiss") } }
                    else -> {}
                }
                neuroInsight?.let { Card(colors = CardDefaults.cardColors(containerColor = if(it.isSignificant) Color.Red else Color.Gray)) { Text("Neuro Audit: Fog ${it.brainFogIndex}", Modifier.padding(8.dp)) } }
                extractedEntities?.let { entities ->
                    entities.medications.forEach { Text("✅ ${it.name}", color = Color.Green) }
                    Button(onDismiss) { Text("Done") }
                }
            }
        }
    }
}

@Composable
fun PRNList(prnMedications: List<PRNMedication>, onRequestAdvisory: (PRNMedication) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("PRN Medications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Spacer(Modifier.height(16.dp)) }
        items(prnMedications) { med -> Card(Modifier.fillMaxWidth().clickable { onRequestAdvisory(med) }) { Row(Modifier.padding(16.dp)) { Column(Modifier.weight(1f)) { Text(med.name, fontWeight = FontWeight.Bold); Text(med.dosage) }; Icon(Icons.Default.ChevronRight, null) } } }
    }
}

@Composable
fun MealSyncDashboard(windows: List<OptimalEatingWindow>, is24h: Boolean, depletions: List<String>, refs: List<NutrientReference>) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Adaptive Nutrition", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        if (depletions.isNotEmpty()) item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) { Column(Modifier.padding(16.dp)) { depletions.forEach { Text("• $it") } } } }
        items(windows) { w -> Card { Column(Modifier.padding(16.dp)) { Text("${w.startTime} - ${w.endTime}", fontWeight = FontWeight.Bold); Text(w.reason) } } }
    }
}

@Composable
fun SleepCheckInDialog(onDismiss: () -> Unit, onConfirm: (Int, Int, String) -> Unit) {
    var q by remember { mutableStateOf(5f) }; var r by remember { mutableStateOf(5f) }; var m by remember { mutableStateOf("Neutral") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Sleep Check-in") }, text = { Column { Text("Quality: ${q.toInt()}"); Slider(q, {q=it}, valueRange=1f..10f); Text("Restfulness: ${r.toInt()}"); Slider(r, {r=it}, valueRange=1f..10f) } }, confirmButton = { Button({ onConfirm(q.toInt(), r.toInt(), m) }) { Text("Log") } })
}

@Composable
fun AppetiteLogDialog(onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var h by remember { mutableStateOf(5f) }; var d by remember { mutableStateOf(1f) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Appetite") }, text = { Column { Text("Hunger: ${h.toInt()}"); Slider(h, {h=it}, valueRange=1f..10f); Text("Difficulty: ${d.toInt()}"); Slider(d, {d=it}, valueRange=1f..10f) } }, confirmButton = { Button({ onConfirm(h.toInt(), d.toInt()) }) { Text("Save") } })
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (String, String, Long?) -> Unit) {
    var d by remember { mutableStateOf("") }; var s by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New Goal") }, text = { Column { OutlinedTextField(d, {d=it}, label={Text("Goal")}); OutlinedTextField(s, {s=it}, label={Text("Symptom")}) } }, confirmButton = { Button({ onConfirm(d, s, null) }) { Text("Save") } })
}

@Composable
fun NutrientAdvisoryDialog(adv: NutrientAdvisory, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Nutrient Check") }, text = { Column { Text(adv.summary, fontWeight = FontWeight.Bold); adv.warnings.forEach { Text("• $it", color = Color.Red) } } }, confirmButton = { Button(onConfirm) { Text("Log Anyway") } })
}

@Composable
fun PRNAdvisoryDialog(adv: PRNAdvisory, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Safety Check") }, text = { Column { Text(adv.reason); if(!adv.isApproved) Text("Alternative: ${adv.alternativeAdvice}", fontWeight = FontWeight.Bold) } }, confirmButton = { if(adv.isApproved) Button(onConfirm) { Text("Log") } })
}

@Composable
fun InsightCard(title: String, insight: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.tertiaryContainer, onDismiss: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); IconButton(onDismiss, Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) } }
            Text(insight, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun WakeTimeHeader(t: Long, f: DateTimeFormatter, onEdit: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onEdit() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("T-Wake Anchor", style = MaterialTheme.typography.labelMedium); Text(f.format(Instant.ofEpochMilli(t)), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
            Icon(Icons.Default.Edit, null)
        }
    }
}

@Composable
fun TimelineItem(med: MedicationRecord, t: Long, f: DateTimeFormatter, onEdit: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(f.format(Instant.ofEpochMilli(t).plusMillis(med.frequencyOffset)), style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(70.dp))
        Spacer(Modifier.width(8.dp)); Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape)); Spacer(Modifier.width(8.dp))
        Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(med.name, fontWeight = FontWeight.Bold); Text(med.dosage, style = MaterialTheme.typography.bodySmall) }
                IconButton(onDuplicate) { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)) }
                IconButton(onEdit) { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)) }
                IconButton(onDelete) { Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = Color.Red) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(iN: String, iD: String, iO: String, iF: Int, iFR: String, onDismiss: () -> Unit, onConfirm: (String, String, Long, Int, String) -> Unit) {
    var name by remember { mutableStateOf(iN) }; var dosage by remember { mutableStateOf(iD) }; var offset by remember { mutableStateOf(iO) }; var freq by remember { mutableStateOf(iF.toString()) }; var food by remember { mutableStateOf(iFR) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Medication") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(name, {name=it}, label={Text("Name")}); OutlinedTextField(dosage, {dosage=it}, label={Text("Dosage")}); OutlinedTextField(offset, {offset=it}, label={Text("Offset (h)")}); OutlinedTextField(freq, {freq=it}, label={Text("Freq")}); Row { FilterChip(food=="NONE", {food="NONE"}, label={Text("None")}); Spacer(Modifier.width(4.dp)); FilterChip(food=="WITH_FOOD", {food="WITH_FOOD"}, label={Text("Food")}) } } }, confirmButton = { Button(onClick = { onConfirm(name, dosage, ((offset.toDoubleOrNull() ?: 1.0)*3600000L).toLong(), freq.toIntOrNull() ?: 1, food) }) { Text("Save") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakeTimePickerDialog(iE: Long, is24: Boolean, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    val t = Instant.ofEpochMilli(iE).atZone(ZoneId.systemDefault()).toLocalTime()
    val s = rememberTimePickerState(t.hour, t.minute, is24)
    AlertDialog(onDismissRequest = onDismiss, text = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(s) } }, confirmButton = { Button(onClick = { onConfirm(Instant.now().atZone(ZoneId.systemDefault()).with(LocalTime.of(s.hour, s.minute)).toInstant().toEpochMilli()) }) { Text("OK") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}
