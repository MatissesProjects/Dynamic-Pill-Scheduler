package com.phos.phone.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phos.core.data.engine.JetLagManager
import com.phos.core.data.model.TitrationStep
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun JetLagSimulator(
    jetLagManager: JetLagManager = JetLagManager()
) {
    var targetZoneId by remember { mutableStateOf("UTC") }
    var titrationSteps by remember { mutableStateOf<List<TitrationStep>>(emptyList()) }

    Column {
        Text(
            text = "Jet Lag Titration",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = targetZoneId,
            onValueChange = { targetZoneId = it },
            label = { Text("Target Time Zone (e.g., Europe/London)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = {
                try {
                    val zone = ZoneId.of(targetZoneId)
                    val now = Instant.now()
                    val targetWake = jetLagManager.getTargetWakeInTimeZone(now, zone)
                    titrationSteps = jetLagManager.calculateTitrationSchedule(
                        now.toEpochMilli(),
                        targetWake.toEpochMilli(),
                        ZonedDateTime.now()
                    )
                } catch (e: Exception) {
                    // Handle invalid ZoneId
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Generate Schedule")
        }
        
        if (titrationSteps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Titration Plan:", style = MaterialTheme.typography.labelMedium)
            titrationSteps.forEach { step ->
                val timeStr = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(step.targetWakeTime))
                Text(
                    text = "${step.date}: Shift T-Wake to $timeStr",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
