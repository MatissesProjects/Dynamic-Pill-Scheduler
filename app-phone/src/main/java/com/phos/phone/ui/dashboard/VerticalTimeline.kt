package com.phos.phone.ui.dashboard

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phos.core.data.model.MedicationRecord
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MainDashboard(
    medications: List<MedicationRecord>,
    tWakeEpoch: Long,
    lastAiInsight: String,
    is24Hour: Boolean,
    onAddMedication: (String, String, Long) -> Unit,
    onUpdateMedication: (MedicationRecord) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onDuplicateMedication: (MedicationRecord) -> Unit,
    onUpdateWakeTime: (Long) -> Unit,
    onToggleTimeFormat: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

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
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Medication")
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
                    onUpdateMedication = onUpdateMedication,
                    onDeleteMedication = onDeleteMedication,
                    onDuplicateMedication = onDuplicateMedication,
                    onUpdateWakeTime = onUpdateWakeTime
                )
                1 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "System Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    JetLagSimulator()
                }
            }
        }
    }

    if (showAddDialog) {
        AddMedicationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, dosage, offset ->
                onAddMedication(name, dosage, offset)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalTimeline(
    medications: List<MedicationRecord>,
    tWakeEpoch: Long,
    lastAiInsight: String,
    is24Hour: Boolean,
    onUpdateMedication: (MedicationRecord) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onDuplicateMedication: (MedicationRecord) -> Unit,
    onUpdateWakeTime: (Long) -> Unit
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
            item { AiInsightCard(insight = lastAiInsight) }
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
            onDismiss = { editingMedication = null },
            onConfirm = { name, dosage, offset ->
                onUpdateMedication(med.copy(name = name, dosage = dosage, frequencyOffset = offset))
                editingMedication = null
            }
        )
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
            modifier = Modifier.width(70.dp) // Slightly wider for AM/PM
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
                }
                IconButton(onClick = onDuplicate) { Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
fun AddMedicationDialog(
    initialName: String = "",
    initialDosage: String = "",
    initialOffsetHours: String = "1",
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var dosage by remember { mutableStateOf(initialDosage) }
    var offsetHours by remember { mutableStateOf(initialOffsetHours) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "New Medication" else "Edit Medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage") })
                OutlinedTextField(value = offsetHours, onValueChange = { offsetHours = it }, label = { Text("T-Wake Offset (Hours)") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                val offset = ((offsetHours.toDoubleOrNull() ?: 1.0) * 3600000L).toLong()
                onConfirm(name, dosage, offset) 
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

@Composable
fun AiInsightCard(insight: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Insight", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = insight, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
