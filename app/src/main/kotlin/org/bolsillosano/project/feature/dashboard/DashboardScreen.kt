package org.bolsillosano.project.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.bolsillosano.project.core.designsystem.component.BalanceCard
import org.bolsillosano.project.core.designsystem.component.BolsilloTopBar
import org.bolsillosano.project.core.designsystem.component.EmptyState
import org.bolsillosano.project.core.designsystem.component.ErrorState
import org.bolsillosano.project.core.designsystem.component.LoadingState
import org.bolsillosano.project.core.designsystem.component.PrimaryButton
import org.bolsillosano.project.core.designsystem.component.SecondaryButton
import org.bolsillosano.project.core.designsystem.component.ShareBarItem
import org.bolsillosano.project.core.designsystem.component.ShareBars
import org.bolsillosano.project.core.designsystem.component.SummaryMetricCard
import org.bolsillosano.project.core.designsystem.component.TransactionItem
import org.bolsillosano.project.core.designsystem.theme.LocalBolsilloColors
import org.bolsillosano.project.domain.model.PeriodPreset

@Composable
fun DashboardScreen(
    onCreate: (String) -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onOpenTransactions: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalBolsilloColors.current
    Scaffold(topBar = { BolsilloTopBar(title = "Inicio") }) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(
                message = state.error.orEmpty(),
                onRetry = { viewModel.selectPeriod(state.period) },
                modifier = Modifier.padding(padding),
            )
            else -> DashboardContent(
                state = state,
                incomeColor = colors.income,
                expenseColor = colors.expense,
                onPeriod = viewModel::selectPeriod,
                onCreate = onCreate,
                onOpenTransaction = onOpenTransaction,
                onOpenTransactions = onOpenTransactions,
                onDemo = viewModel::loadDemo,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    incomeColor: androidx.compose.ui.graphics.Color,
    expenseColor: androidx.compose.ui.graphics.Color,
    onPeriod: (PeriodPreset) -> Unit,
    onCreate: (String) -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onOpenTransactions: () -> Unit,
    onDemo: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(state.greeting, style = MaterialTheme.typography.headlineMedium)
            Text(state.dateLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.period == PeriodPreset.ThisMonth,
                onClick = { onPeriod(PeriodPreset.ThisMonth) },
                label = { Text("Este mes") },
            )
            FilterChip(
                selected = state.period == PeriodPreset.PreviousMonth,
                onClick = { onPeriod(PeriodPreset.PreviousMonth) },
                label = { Text("Mes anterior") },
            )
        }
        BalanceCard(
            label = "Saldo disponible",
            amount = state.balance,
            caption = "Ingresos menos gastos, de todo lo registrado",
        )
        if (state.showWelcome) {
            EmptyState(
                title = "Empieza con tu primer movimiento",
                body = "Registra un ingreso o un gasto. El saldo y los gráficos se calculan solo con lo que guardes.",
                action = {
                    PrimaryButton(text = "Añadir gasto", onClick = { onCreate("expense") })
                    SecondaryButton(text = "Cargar datos de demostración", onClick = onDemo)
                },
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMetricCard("Ingresos", state.income, incomeColor, Modifier.weight(1f))
                SummaryMetricCard("Gastos", state.expense, expenseColor, Modifier.weight(1f))
            }
            state.variation?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text("Gastos por categoría", style = MaterialTheme.typography.titleMedium)
            if (state.periodEmpty || state.shares.isEmpty()) {
                Text(
                    "Este periodo no tiene gastos para graficar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ShareBars(
                    state.shares.map {
                        ShareBarItem(it.name, it.amount, it.percentLabel, it.fraction)
                    },
                )
            }
            Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium)
            state.recent.forEach { movement ->
                TransactionItem(
                    title = movement.title,
                    categoryName = movement.category,
                    iconKey = movement.iconKey,
                    amount = movement.amount,
                    amountColor = if (movement.income) incomeColor else expenseColor,
                    demoLabel = movement.isDemo,
                    onClick = { onOpenTransaction(movement.id) },
                )
            }
            SecondaryButton(text = "Ver todos los movimientos", onClick = onOpenTransactions)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(text = "Gasto", onClick = { onCreate("expense") }, modifier = Modifier.weight(1f))
                SecondaryButton(text = "Ingreso", onClick = { onCreate("income") }, modifier = Modifier.weight(1f))
            }
        }
    }
}
