@file:OptIn(ExperimentalMaterial3Api::class)
package com.phos.phone.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
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
import com.phos.core.data.engine.NapOverlap
import com.phos.core.data.engine.OptimalEatingWindow
import com.phos.core.data.engine.PRNAdvisory
import com.phos.core.data.engine.NutrientAdvisory
import com.phos.core.data.model.*
import com.phos.core.intelligence.ExtractedEntities
import com.phos.core.intelligence.PosturalRecommendation
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
    tWakeEpoch: Long,
    lastAiInsight: String,
    is24Hour: Boolean,
    healthInsights: List<String>,
    sideEffectAlerts: List<SideEffectRule>,
    napOverlaps: List<NapOverlap>,
    postureRecommendation: PosturalRecommendation?,
    prnAdvisory: PRNAdvisory?,
    travelProposal: TravelProposal?,
    eatingWindows: List<OptimalEatingWindow>,
    nutrientAdvisory: NutrientAdvisory?,
    voiceState: VoiceState,
    voiceExtractedEntities: ExtractedEntities?,
    onAddMedication: (String, String, Long, Int, String) -> Unit,
    onUpdateMedication: (MedicationRecord) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onDuplicateMedication: (MedicationRecord) -> Unit,
    onUpdateWakeTime: (Long) -> Unit,
    onToggleTimeFormat: (Boolean) -> Unit,
    onDismissInsight: (String) -> Unit,
    onRequestPRNAdvisory: (PRNMedication) -> Unit,
    onLogPRNDose: (PRNMedication) -> Unit,
    onClearPRNAdvisory: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onProcessVoiceCommand: (String) -> Unit,
    onClearVoiceResults: () -> Unit,
    onAcceptTravelProposal: (TravelProposal) -> Unit,
    onDismissTravelProposal: () -> Unit,
    onDetectTravel: () -> Unit,
    onLogAppetite: (Int, Int) -> Unit,
    onLogFood: (String, String, NutrientFacts?) -> Unit,
    onRequestNutrientAdvisory: (String, NutrientFacts) -> Unit,
    onClearNutrientAdvisory: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAppetiteDialog by remember { mutableStateOf(false) }
    var prefilledName by remember { mutableStateOf("") }
    var prefilledDosage by remember { mutableStateOf("") }
    var prefilledFrequency by remember { mutableStateOf(1) }
    
    var lastScannedFoodResult by remember { mutableStateOf<FoodScanResult?>(null) }

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Timeline") },
                    label = { Text("Timeline") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "As-Needed") },
                    label = { Text("PRN") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Nutrition") },
                    label = { Text("Meals") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = { showAppetiteDialog = true },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Icon(Icons.Default.Fastfood, contentDescription = "Log Appetite")
                    }

                    FloatingActionButton(
                        onClick = { 
                            if (hasAudioPermission) {
                                if (voiceState is VoiceState.Listening) onStopVoiceListening()
                                else onStartVoiceListening()
                            } else {
                                audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        containerColor = if (voiceState is VoiceState.Listening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(if (voiceState is VoiceState.Listening) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Voice Log")
                    }

                    FloatingActionButton(onClick = { 
                        prefilledName = ""
                        prefilledDosage = ""
                        prefilledFrequency = 1
                        showAddDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Medication")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .statusBarsPadding()
        ) {
            when (selectedTab) {
                0 -> VerticalTimeline(
                    medications = medications,
                    tWakeEpoch = tWakeEpoch,
                    lastAiInsight = lastAiInsight,
                    is24Hour = is24Hour,
                    healthInsights = healthInsights,
                    sideEffectAlerts = sideEffectAlerts,
                    napOverlaps = napOverlaps,
                    postureRecommendation = postureRecommendation,
                    travelProposal = travelProposal,
                    onUpdateMedication = onUpdateMedication,
                    onDeleteMedication = onDeleteMedication,
                    onDuplicateMedication = onDuplicateMedication,
                    onUpdateWakeTime = onUpdateWakeTime,
                    onDismissInsight = onDismissInsight,
                    onAcceptTravelProposal = onAcceptTravelProposal,
                    onDismissTravelProposal = onDismissTravelProposal
                )
                1 -> PRNList(
                    prnMedications = prnMedications,
                    onRequestAdvisory = onRequestPRNAdvisory
                )
                2 -> {
                    if (hasCameraPermission) {
                        PillScannerScreen(
                            onPillScanned = { result ->
                                prefilledName = result.detectedName ?: "${result.detectedColor} ${result.detectedShape} Pill"
                                prefilledDosage = result.detectedDosage ?: ""
                                prefilledFrequency = result.frequencyDosesPerDay
                                showAddDialog = true
                                selectedTab = 0 
                            },
                            onFoodScanned = { result ->
                                lastScannedFoodResult = result
                                if (result.nutrients != null) {
                                    onRequestNutrientAdvisory(result.detectedName ?: "Food Item", result.nutrients!!)
                                } else {
                                    onLogFood(result.detectedName ?: "Unknown", result.category ?: "General", null)
                                    selectedTab = 2
                                }
                            }
                        )
                    } else {
                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Camera permission required for scanner")
                            Button(onClick = { cameraLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
                3 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "System Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    ListItem(
                        headlineContent = { Text("24-Hour Time Format") },
                        trailingContent = {
                            Switch(
                                checked = is24Hour,
                                onCheckedChange = onToggleTimeFormat
                            )
                        }
                    )
                    
                    Divider()
                    JetLagSimulator()

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onDetectTravel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Flight, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Simulate Travel Detection")
                    }
                }
            }

            // Voice Feedback Overlay
            AnimatedVisibility(
                visible = voiceState !is VoiceState.Idle,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                VoiceOverlay(
                    state = voiceState,
                    extractedEntities = voiceExtractedEntities,
                    onProcess = onProcessVoiceCommand,
                    onDismiss = onClearVoiceResults
                )
            }
        }
    }

    if (showAppetiteDialog) {
        AppetiteLogDialog(
            onDismiss = { showAppetiteDialog = false },
            onConfirm = { hunger, difficulty ->
                onLogAppetite(hunger, difficulty)
                showAppetiteDialog = false
            }
        )
    }

    prnAdvisory?.let { advisory ->
        PRNAdvisoryDialog(
            advisory = advisory,
            onDismiss = onClearPRNAdvisory,
            onConfirm = {
                // Find the med that triggered this
                prnMedications.find { advisory.reason.contains(it.name) || it.name.contains(advisory.reason.split(" ").last().replace(".","")) }?.let {
                    onLogPRNDose(it)
                } ?: onClearPRNAdvisory()
            }
        )
    }

    nutrientAdvisory?.let { advisory ->
        NutrientAdvisoryDialog(
            advisory = advisory,
            onDismiss = onClearNutrientAdvisory,
            onConfirm = {
                val result = lastScannedFoodResult
                if (result != null) {
                    onLogFood(result.detectedName ?: "Food Item", result.category ?: "General", result.nutrients)
                }
                onClearNutrientAdvisory()
                selectedTab = 2
            }
        )
    }

    if (showAddDialog) {
        AddMedicationDialog(
            initialName = prefilledName,
            initialDosage = prefilledDosage,
            initialFrequency = prefilledFrequency,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, dosage, offset, frequency, foodReq ->
                onAddMedication(name, dosage, offset, frequency, foodReq)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun NutrientAdvisoryDialog(
    advisory: NutrientAdvisory,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (advisory.isGoodIdea) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null, tint = if (advisory.isGoodIdea) Color.Green else MaterialTheme.colorScheme.error) },
        title = { Text(if (advisory.isGoodIdea) "Nutrient Check Passed" else "Dietary Warning") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                item { Text(advisory.summary, fontWeight = FontWeight.Bold) }
                items(advisory.warnings) { warning ->
                    Text("• $warning", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                items(advisory.suggestions) { suggestion ->
                    Text("💡 $suggestion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Log Anyway") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AppetiteLogDialog(onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var hunger by remember { mutableStateOf(5f) }
    var difficulty by remember { mutableStateOf(1f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Appetite & Hunger Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("How hungry do you feel? (1-10)")
                Slider(value = hunger, onValueChange = { hunger = it }, valueRange = 1f..10f, steps = 8)
                Text("Difficulty eating right now? (1-10)")
                Slider(value = difficulty, onValueChange = { difficulty = it }, valueRange = 1f..10f, steps = 8)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(hunger.toInt(), difficulty.toInt()) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MealSyncDashboard(eatingWindows: List<OptimalEatingWindow>, is24Hour: Boolean) {
    val timeFormatter = DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a").withZone(ZoneId.systemDefault())
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Adaptive Nutrition Orchestration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Dynamically calculated windows based on your schedule and hunger logs.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
        
        items(eatingWindows) { window ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (window.isSacred) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                ),
                border = if (window.isSacred) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (window.isSacred) Icons.Default.HealthAndSafety else Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = if (window.isSacred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${timeFormatter.format(Instant.ofEpochMilli(window.startTime))} - ${timeFormatter.format(Instant.ofEpochMilli(window.endTime))}",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (window.isSacred) {
                            Spacer(Modifier.weight(1f))
                            Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("SACRED WINDOW", color = Color.White) }
                        }
                    }
                    Text(window.reason, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    
                    LinearProgressIndicator(
                        progress = window.score / 10f,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(4.dp),
                        color = if (window.isSacred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        
        if (eatingWindows.isEmpty()) {
            item { 
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No clear windows detected. Try adjusting medication offsets.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center) 
                }
            }
        }
    }
}

@Composable
fun VoiceOverlay(
    state: VoiceState,
    extractedEntities: ExtractedEntities?,
    onProcess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(enabled = false) {},
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                when (state) {
                    is VoiceState.Listening -> {
                        Text("Listening...", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    is VoiceState.Success -> {
                        Text("Extracting...", style = MaterialTheme.typography.bodyLarge)
                        LaunchedEffect(state.text) { onProcess(state.text) }
                    }
                    is VoiceState.Error -> {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Text(state.message, textAlign = TextAlign.Center)
                        Button(onClick = onDismiss, Modifier.padding(top = 16.dp)) { Text("Dismiss") }
                    }
                    else -> {}
                }

                extractedEntities?.let { entities ->
                    Text("Understood:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    
                    if (entities.medications.isEmpty() && entities.symptoms.isEmpty() && entities.foods.isEmpty()) {
                        Text("Nothing recognized. Try saying 'Took my lisinopril' or 'Feeling a headache'.")
                    } else {
                        entities.medications.forEach { Text("✅ Logged Dose: ${it.name}", color = Color.Green) }
                        entities.symptoms.forEach { Text("✅ Logged Symptom: ${it.name}", color = Color.Cyan) }
                        entities.foods.forEach { Text("✅ Logged Food: ${it.name}", color = Color.Yellow) }
                    }
                    
                    Button(onClick = onDismiss, Modifier.padding(top = 16.dp)) { Text("Done") }
                }
            }
        }
    }
}

@Composable
fun PRNList(
    prnMedications: List<PRNMedication>,
    onRequestAdvisory: (PRNMedication) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("As-Needed Medications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Select a medication to request a safety check before taking.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
        }
        
        items(prnMedications) { med ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onRequestAdvisory(med) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(med.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(med.dosage, style = MaterialTheme.typography.bodySmall)
                        med.reasonForUse?.let { Text("Used for: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun PRNAdvisoryDialog(
    advisory: PRNAdvisory,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (advisory.isApproved) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null, tint = if (advisory.isApproved) Color.Green else MaterialTheme.colorScheme.error) },
        title = { Text(if (advisory.isApproved) "Safety Check Passed" else "Safety Warning") },
        text = {
            Column {
                Text(advisory.reason)
                advisory.alternativeAdvice?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("Recommendation: $it", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            if (advisory.isApproved) {
                Button(onClick = onConfirm) { Text("Log Dose") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (advisory.isApproved) "Cancel" else "Dismiss") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalTimeline(
    medications: List<MedicationRecord>,
    tWakeEpoch: Long,
    lastAiInsight: String,
    is24Hour: Boolean,
    healthInsights: List<String>,
    sideEffectAlerts: List<SideEffectRule>,
    napOverlaps: List<NapOverlap>,
    postureRecommendation: PosturalRecommendation?,
    travelProposal: TravelProposal?,
    onUpdateMedication: (MedicationRecord) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onDuplicateMedication: (MedicationRecord) -> Unit,
    onUpdateWakeTime: (Long) -> Unit,
    onDismissInsight: (String) -> Unit,
    onAcceptTravelProposal: (TravelProposal) -> Unit,
    onDismissTravelProposal: () -> Unit
) {
    val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
    val timeFormatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault())
    
    var showTimePicker by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<MedicationRecord?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            WakeTimeHeader(tWakeEpoch, timeFormatter) { showTimePicker = true }
        }

        if (lastAiInsight.isNotEmpty()) {
            item { InsightCard(title = "AI Baseline Insight", insight = lastAiInsight, icon = Icons.Default.AutoAwesome, onDismiss = { onDismissInsight("baseline") }) }
        }
        
        travelProposal?.let { proposal ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Travel Detected: ${proposal.destination}", fontWeight = FontWeight.Bold)
                        }
                        Text(proposal.explanation ?: "", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = onDismissTravelProposal) { Text("Dismiss") }
                            Button(onClick = { onAcceptTravelProposal(proposal) }) { Text("Accept Plan") }
                        }
                    }
                }
            }
        }
        
        postureRecommendation?.let { rec ->
            item { InsightCard(title = rec.title, insight = rec.recommendation, icon = Icons.Default.VerticalAlignTop, color = MaterialTheme.colorScheme.primaryContainer, onDismiss = { onDismissInsight("posture_${rec.hashCode()}") }) }
        }

        napOverlaps.forEach { overlap ->
            item { InsightCard(title = "Nap Detected: ${overlap.medicationName} Shift", insight = "Your ${overlap.overlapDurationMinutes}-minute nap overlapped with this dose. Suggesting a shift of ${(overlap.suggestedShiftMillis / 60000)} minutes.", icon = Icons.Default.Bedtime, color = MaterialTheme.colorScheme.secondaryContainer, onDismiss = { onDismissInsight("nap_${overlap.medicationId}") }) }
        }
        
        healthInsights.forEach { insight ->
            item { InsightCard(title = "Absorption Spacing", insight = insight, icon = Icons.Default.Info, color = MaterialTheme.colorScheme.secondaryContainer, onDismiss = { onDismissInsight("absorption_${insight.hashCode()}") }) }
        }

        sideEffectAlerts.forEach { alert ->
            item { InsightCard(title = "Side Effect Watch: ${alert.sideEffect}", insight = alert.advice, icon = Icons.Default.Warning, color = MaterialTheme.colorScheme.errorContainer, onDismiss = { onDismissInsight("side_effect_${alert.medicationId}_${alert.sideEffect}") }) }
        }

        if (medications.isEmpty()) {
            item { EmptyTimelineMessage() }
        }

        items(medications.sortedBy { it.frequencyOffset }) { med ->
            TimelineItem(
                med = med,
                tWakeEpoch = tWakeEpoch,
                timeFormatter = timeFormatter,
                onEdit = { editingMedication = med },
                onDuplicate = { onDuplicateMedication(med) },
                onDelete = { onDeleteMedication(med.id) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showTimePicker) {
        WakeTimePickerDialog(
            initialEpoch = tWakeEpoch,
            is24Hour = is24Hour,
            onDismiss = { showTimePicker = false },
            onConfirm = { newEpoch ->
                onUpdateWakeTime(newEpoch)
                showTimePicker = false
            }
        )
    }

    editingMedication?.let { med ->
        AddMedicationDialog(
            initialName = med.name,
            initialDosage = med.dosage,
            initialOffsetHours = (med.frequencyOffset / 3600000.0).toString(),
            initialFoodRequirement = med.foodRequirement,
            onDismiss = { editingMedication = null },
            onConfirm = { name, dosage, offset, frequency, foodReq ->
                onUpdateMedication(med.copy(name = name, dosage = dosage, frequencyOffset = offset, foodRequirement = foodReq))
                editingMedication = null
            }
        )
    }
}

@Composable
fun InsightCard(title: String, insight: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.tertiaryContainer, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = insight, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun WakeTimeHeader(tWakeEpoch: Long, timeFormatter: DateTimeFormatter, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "T-Wake Anchor", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = timeFormatter.format(Instant.ofEpochMilli(tWakeEpoch)),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
    }
}

@Composable
fun EmptyTimelineMessage() {
    Text(
        text = "No medications scheduled. Tap + to add one.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 32.dp).fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
fun TimelineItem(
    med: MedicationRecord,
    tWakeEpoch: Long,
    timeFormatter: DateTimeFormatter,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val scheduledTime = Instant.ofEpochMilli(tWakeEpoch).plusMillis(med.frequencyOffset)

        Text(
            text = timeFormatter.format(scheduledTime),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(70.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(modifier = Modifier
            .size(8.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape))

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = med.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = med.dosage, style = MaterialTheme.typography.bodySmall)
                    if (med.foodRequirement != "NONE") {
                        Text(text = "Food: ${med.foodRequirement}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onDuplicate) { Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    initialName: String = "",
    initialDosage: String = "",
    initialOffsetHours: String = "1",
    initialFrequency: Int = 1,
    initialFoodRequirement: String = "NONE",
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var dosage by remember { mutableStateOf(initialDosage) }
    var offsetHours by remember { mutableStateOf(initialOffsetHours) }
    var frequency by remember { mutableStateOf(initialFrequency.toString()) }
    var foodReq by remember { mutableStateOf(initialFoodRequirement) }
    
    LaunchedEffect(initialName, initialDosage, initialFrequency, initialFoodRequirement) {
        name = initialName
        dosage = initialDosage
        frequency = initialFrequency.toString()
        foodReq = initialFoodRequirement
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty() && initialDosage.isEmpty()) "New Medication" else "Review Medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage") })
                OutlinedTextField(value = offsetHours, onValueChange = { offsetHours = it }, label = { Text("First T-Wake Offset (Hours)") })
                OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("Frequency (times/day)") })
                
                Text("Food Requirement:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = foodReq == "NONE", onClick = { foodReq = "NONE" }, label = { Text("None") })
                    FilterChip(selected = foodReq == "WITH_FOOD", onClick = { foodReq = "WITH_FOOD" }, label = { Text("With Food") })
                    FilterChip(selected = foodReq == "EMPTY_STOMACH", onClick = { foodReq = "EMPTY_STOMACH" }, label = { Text("Empty Stomach") })
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                val offset = ((offsetHours.toDoubleOrNull() ?: 1.0) * 3600000L).toLong()
                val freq = frequency.toIntOrNull() ?: 1
                onConfirm(name, dosage, offset, freq, foodReq) 
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakeTimePickerDialog(
    initialEpoch: Long,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val initialTime = Instant.ofEpochMilli(initialEpoch).atZone(ZoneId.systemDefault()).toLocalTime()
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Wake Time") },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(onClick = {
                val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                val newEpoch = Instant.now().atZone(ZoneId.systemDefault())
                    .with(newTime)
                    .toInstant()
                    .toEpochMilli()
                onConfirm(newEpoch)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
