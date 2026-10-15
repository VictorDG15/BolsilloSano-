package org.bolsillosano.project.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.bolsillosano.project.core.common.DateFormatter
import org.bolsillosano.project.core.designsystem.component.BolsilloTopBar
import org.bolsillosano.project.core.designsystem.component.DatePickerField
import org.bolsillosano.project.core.designsystem.component.EmptyState
import org.bolsillosano.project.core.designsystem.component.ErrorState
import org.bolsillosano.project.core.designsystem.component.LoadingState
import org.bolsillosano.project.core.designsystem.component.SecondaryButton
import org.bolsillosano.project.core.designsystem.component.TransactionItem
import org.bolsillosano.project.core.designsystem.component.TransactionSectionHeader
import org.bolsillosano.project.core.designsystem.theme.LocalBolsilloColors
import org.bolsillosano.project.domain.model.TransactionType
import java.time.LocalDate

@Composable
fun TransactionsScreen(
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalBolsilloColors.current
    Scaffold(
        topBar = {
            BolsilloTopBar(title = "Movimientos", actions = {
                IconButton(onClick = onCreate) {
                    Icon(Icons.Outlined.Add, contentDescription = "Añadir movimiento")
                }
            })
        },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(state.error.orEmpty(), { viewModel.onQueryChange(state.query) }, Modifier.padding(padding))
            else -> TransactionsContent(state, colors.income, colors.expense, viewModel, onOpen, padding)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionsContent(
    state: TransactionsUiState,
    incomeColor: androidx.compose.ui.graphics.Color,
    expenseColor: androidx.compose.ui.graphics.Color,
    viewModel: TransactionsViewModel,
    onOpen: (Long) -> Unit,
    padding: PaddingValues,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar por descripción") },
                    singleLine = true,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(state.type == null, { viewModel.onTypeChange(null) }, { Text("Todos") })
                    FilterChip(state.type == TransactionType.Income, { viewModel.onTypeChange(TransactionType.Income) }, { Text("Ingresos") })
                    FilterChip(state.type == TransactionType.Expense, { viewModel.onTypeChange(TransactionType.Expense) }, { Text("Gastos") })
                }
                DatePickerField(
                    date = state.from ?: LocalDate.now(),
                    onDateChange = viewModel::onFromChange,
                    label = "Desde",
                    text = state.from?.let(DateFormatter::short) ?: "Cualquier fecha",
                )
                DatePickerField(
                    date = state.to ?: LocalDate.now(),
                    onDateChange = viewModel::onToChange,
                    label = "Hasta",
                    text = state.to?.let(DateFormatter::short) ?: "Cualquier fecha",
                )
                if (state.from != null || state.to != null) {
                    SecondaryButton(text = "Quitar rango de fechas", onClick = viewModel::clearDates)
                }
            }
        }
        if (state.empty) {
            item {
                EmptyState(
                    title = "No hay movimientos",
                    body = "Prueba con otra búsqueda o registra un ingreso o un gasto.",
                )
            }
        } else {
            state.sections.forEach { section ->
                item { TransactionSectionHeader(section.title) }
                items(section.rows, key = { it.id }) { row ->
                    TransactionItem(
                        title = row.title,
                        categoryName = row.category,
                        iconKey = row.iconKey,
                        amount = row.amount,
                        amountColor = if (row.income) incomeColor else expenseColor,
                        demoLabel = row.isDemo,
                        onClick = { onOpen(row.id) },
                    )
                }
            }
        }
    }
}
