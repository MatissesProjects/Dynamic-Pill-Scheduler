package com.phos.phone.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phos.core.data.proto.Medication
import com.phos.core.data.proto.PhosState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun VerticalTimeline(
    state: PhosState
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (state.lastAiInsight.isNotEmpty()) {
            item {
                AiInsightCard(insight = state.lastAiInsight)
            }
        }

        item {
            JetLagSimulator()
        }

        items(state.medicationsList) { med ->
            TimelineItem(
                med = med,
                timeFormatter = timeFormatter
            )
        }
    }
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
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "AI Insight",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TimelineItem(
    med: Medication,
    timeFormatter: DateTimeFormatter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Column
        Text(
            text = timeFormatter.format(Instant.ofEpochMilli(med.scheduledTime)),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(60.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (med.status == "TAKEN") Color.Gray else MaterialTheme.colorScheme.primary,
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
                    text = med.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
