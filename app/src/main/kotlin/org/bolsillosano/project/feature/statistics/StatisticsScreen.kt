package org.bolsillosano.project.feature.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.bolsillosano.project.core.designsystem.component.BolsilloTopBar
import org.bolsillosano.project.core.designsystem.component.EmptyState
import org.bolsillosano.project.core.designsystem.component.ErrorState
import org.bolsillosano.project.core.designsystem.component.LoadingState
import org.bolsillosano.project.core.designsystem.component.ShareBars
import org.bolsillosano.project.core.designsystem.theme.LocalBolsilloColors
import org.bolsillosano.project.domain.model.PeriodPreset

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalBolsilloColors.current
    Scaffold(topBar = { BolsilloTopBar("Estadísticas") }) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(state.error.orEmpty(), { viewModel.selectPeriod(state.period) }, Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(state.period == PeriodPreset.ThisMonth, { viewModel.selectPeriod(PeriodPreset.ThisMonth) }, { Text("Este mes") })
                    FilterChip(state.period == PeriodPreset.PreviousMonth, { viewModel.selectPeriod(PeriodPreset.PreviousMonth) }, { Text("Mes anterior") })
                }
                if (state.empty) {
                    EmptyState(
                        title = "Sin movimientos en este periodo",
                        body = "Los gráficos aparecen cuando hay ingresos o gastos guardados en el mes elegido.",
                    )
                } else {
                    Text("Ingresos y gastos", style = MaterialTheme.typography.titleMedium)
                    Meter("Ingresos ${state.income}", state.incomeFraction, colors.income)
                    Meter("Gastos ${state.expense}", state.expenseFraction, colors.expense)
                    Text("Gastos por categoría", style = MaterialTheme.typography.titleMedium)
                    if (state.shares.isEmpty()) {
                        Text("No hay gastos en este periodo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        ShareBars(state.shares)
                    }
                    Text("Totales por semana", style = MaterialTheme.typography.titleMedium)
                    state.weeks.forEach { week ->
                        Text(week.label, style = MaterialTheme.typography.labelLarge)
                        Meter("Ingresos ${week.income}", week.incomeFraction, colors.income)
                        Meter("Gastos ${week.expense}", week.expenseFraction, colors.expense)
                    }
                }
            }
        }
    }
}

@Composable
private fun Meter(label: String, fraction: Float, color: androidx.compose.ui.graphics.Color) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Column(Modifier.semantics { contentDescription = label }) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Canvas(Modifier.fillMaxWidth().height(10.dp).padding(top = 4.dp)) {
            drawRoundRect(track, cornerRadius = CornerRadius(8.dp.toPx()))
            drawRoundRect(
                color = color,
                size = Size(size.width * fraction.coerceIn(0f, 1f), size.height),
                cornerRadius = CornerRadius(8.dp.toPx()),
            )
        }
    }
}
