package com.phos.phone.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phos.core.data.engine.JetLagManager
import com.phos.core.data.engine.TitrationStep
import java.time.Instant
import java.time.ZoneId

@Composable
fun JetLagSimulator(
    jetLagManager: JetLagManager = JetLagManager()
) {
    var targetZoneId by remember { mutableStateOf("UTC") }
    var titrationSteps by remember { mutableStateOf<List<TitrationStep>>(emptyList()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Jet Lag Simulator",
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
                            targetWake.toEpochMilli()
                        )
                    } catch (e: Exception) {
                        // Handle invalid ZoneId
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Generate Titration Schedule")
            }
            
            if (titrationSteps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Suggested 3-Day Shift:", style = MaterialTheme.typography.labelMedium)
                titrationSteps.take(3).forEach { step ->
                    Text(
                        text = "Day ${step.dayNumber}: Shift T-Wake to ${step.wakeTimeShiftMillis}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
