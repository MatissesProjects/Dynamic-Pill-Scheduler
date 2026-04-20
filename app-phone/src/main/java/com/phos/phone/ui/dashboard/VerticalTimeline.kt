package com.phos.phone.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phos.core.data.model.MedicationRecord
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun VerticalTimeline(
    medications: List<MedicationRecord>,
    tWakeEpoch: Long
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(medications) { med ->
            TimelineItem(
                med = med,
                scheduledTime = tWakeEpoch + med.frequencyOffset,
                timeFormatter = timeFormatter
            )
        }
    }
}

@Composable
fun TimelineItem(
    med: MedicationRecord,
    scheduledTime: Long,
    timeFormatter: DateTimeFormatter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Column
        Text(
            text = timeFormatter.format(Instant.ofEpochMilli(scheduledTime)),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(60.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = med.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } 
                        ?: MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Content Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = med.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${med.dosage} • ${med.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
