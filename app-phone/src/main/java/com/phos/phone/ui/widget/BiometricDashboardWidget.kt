package com.phos.phone.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.material3.ColorProviders
import com.phos.core.data.datastore.phosDataStore
import com.phos.core.data.proto.PhosState
import kotlinx.coroutines.flow.first

class BiometricDashboardWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = context.phosDataStore.data.first()
        
        provideContent {
            GlanceTheme {
                WidgetContent(state)
            }
        }
    }

    @Composable
    private fun WidgetContent(state: PhosState) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(GlanceTheme.colors.surface),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "PHOS Dashboard",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))

            // The "Biometric Wave" - Minimalist representation
            Row(
                modifier = GlanceModifier.fillMaxWidth().height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.medicationsList.take(5).forEachIndexed { index, med ->
                    StatusDot(isCompleted = med.status == "TAKEN")
                    if (index < state.medicationsCount - 1 && index < 4) {
                        DotConnector()
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = getNextMedicationText(state),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }

    companion object {
        fun getNextMedicationText(state: PhosState): String {
            val nextMed = state.medicationsList.firstOrNull { it.status == "PENDING" }
            return if (nextMed != null) "Next: ${nextMed.name}" else "All caught up!"
        }
    }

    @Composable
    private fun StatusDot(isCompleted: Boolean) {
        val color = if (isCompleted) GlanceTheme.colors.secondary else GlanceTheme.colors.primary
        Box(
            modifier = GlanceModifier
                .size(12.dp)
                .background(color),
            contentAlignment = Alignment.Center
        ) {}
    }

    @Composable
    private fun DotConnector() {
        Box(
            modifier = GlanceModifier
                .width(24.dp)
                .height(2.dp)
                .background(GlanceTheme.colors.outline)
        ) {}
    }
}

class BiometricDashboardReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BiometricDashboardWidget()
}
