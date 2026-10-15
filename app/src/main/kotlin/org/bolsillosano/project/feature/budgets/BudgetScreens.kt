package org.bolsillosano.project.feature.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.bolsillosano.project.core.designsystem.component.AmountTextField
import org.bolsillosano.project.core.designsystem.component.BolsilloTopBar
import org.bolsillosano.project.core.designsystem.component.CategoryIcon
import org.bolsillosano.project.core.designsystem.component.ConfirmationDialog
import org.bolsillosano.project.core.designsystem.component.EmptyState
import org.bolsillosano.project.core.designsystem.component.LoadingState
import org.bolsillosano.project.core.designsystem.component.PrimaryButton
import org.bolsillosano.project.core.designsystem.theme.LocalBolsilloColors
import org.bolsillosano.project.domain.money.BudgetLevel

@Composable
fun BudgetsScreen(
    onCreate: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by rememberSaveable { mutableStateOf<Long?>(null) }
    val colors = LocalBolsilloColors.current
    Scaffold(
        topBar = {
            BolsilloTopBar("Presupuestos", actions = {
                IconButton(onClick = onCreate) {
                    Icon(Icons.Outlined.Add, contentDescription = "Crear presupuesto")
                }
            })
        },
    ) { padding ->
        if (state.loading) {
            LoadingState(Modifier.padding(padding))
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = viewModel::previousMonth) { Text("Anterior") }
                        Text(state.monthLabel, style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = viewModel::nextMonth) { Text("Siguiente") }
                    }
                }
                if (state.empty) {
                    item {
                        EmptyState(
                            title = "Sin presupuestos este mes",
                            body = "Define un límite para una categoría de gasto. El consumo sale de los movimientos de ese mes.",
                        )
                    }
                }
                items(state.rows, key = { it.id }) { row ->
                    val tone = when (row.level) {
                        BudgetLevel.Over -> colors.expense
                        BudgetLevel.Near -> colors.warning
                        BudgetLevel.Ok -> MaterialTheme.colorScheme.onSurface
                    }
                    Surface(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        onClick = { onEdit(row.id) },
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CategoryIcon(row.iconKey, row.category)
                                Column(Modifier.weight(1f)) {
                                    Text(row.category, style = MaterialTheme.typography.titleMedium)
                                    Text("${row.spent} de ${row.limit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(row.percent, color = tone)
                            }
                            Text("Restante: ${row.remaining}", color = tone)
                            val warning = when (row.level) {
                                BudgetLevel.Over -> "Superaste el límite."
                                BudgetLevel.Near -> "Estás cerca del límite."
                                BudgetLevel.Ok -> null
                            }
                            warning?.let { Text(it, color = tone) }
                            TextButton(onClick = { pendingDelete = row.id }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
    pendingDelete?.let { id ->
        ConfirmationDialog(
            title = "Eliminar presupuesto",
            body = "Los movimientos de la categoría se conservan.",
            confirmText = "Eliminar",
            onConfirm = {
                viewModel.delete(id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: BudgetFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { BolsilloTopBar(if (state.editing) "Editar presupuesto" else "Nuevo presupuesto", onBack = onBack) }) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.missing -> EmptyState(
                title = "Este presupuesto ya no existe",
                body = "Vuelve a la lista.",
                modifier = Modifier.padding(padding),
            )
            else -> Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    state.month.atDay(1).format(
                        java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("es-PE")),
                    ).replaceFirstChar { it.titlecase(java.util.Locale.forLanguageTag("es-PE")) },
                )
                Text("Categoría de gasto", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.categories.forEach { category ->
                        FilterChip(
                            selected = state.categoryId == category.id,
                            onClick = { viewModel.onCategory(category.id) },
                            label = { Text(category.name) },
                        )
                    }
                }
                state.categoryError?.let { Text(it) }
                AmountTextField(state.amountText, viewModel::onAmount, state.amountError)
                state.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                PrimaryButton(
                    text = if (state.saving) "Guardando…" else "Guardar",
                    onClick = { viewModel.save(onSaved) },
                    enabled = !state.saving,
                )
            }
        }
    }
}
