package org.bolsillosano.project.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.bolsillosano.project.core.designsystem.component.AmountTextField
import org.bolsillosano.project.core.designsystem.component.BolsilloTopBar
import org.bolsillosano.project.core.designsystem.component.DatePickerField
import org.bolsillosano.project.core.designsystem.component.EmptyState
import org.bolsillosano.project.core.designsystem.component.LoadingState
import org.bolsillosano.project.core.designsystem.component.PrimaryButton
import org.bolsillosano.project.domain.model.TransactionType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TransactionFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            BolsilloTopBar(
                title = if (state.editing) "Editar movimiento" else "Nuevo movimiento",
                onBack = onBack,
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.missing -> EmptyState(
                title = "Este movimiento ya no existe",
                body = "Puede que se haya eliminado.",
                modifier = Modifier.padding(padding),
            )
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.type == TransactionType.Expense,
                        onClick = { viewModel.onType(TransactionType.Expense) },
                        label = { Text("Gasto") },
                    )
                    FilterChip(
                        selected = state.type == TransactionType.Income,
                        onClick = { viewModel.onType(TransactionType.Income) },
                        label = { Text("Ingreso") },
                    )
                }
                AmountTextField(state.amountText, viewModel::onAmount, state.amountError)
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescription,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción") },
                    isError = state.descriptionError != null,
                    supportingText = { Text(state.descriptionError ?: "Obligatoria") },
                    singleLine = true,
                )
                Text("Categoría", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.categories.filter { it.type == state.type }.forEach { category ->
                        FilterChip(
                            selected = state.categoryId == category.id,
                            onClick = { viewModel.onCategory(category.id) },
                            label = { Text(category.name) },
                        )
                    }
                }
                state.categoryError?.let { Text(it) }
                DatePickerField(date = state.date, onDateChange = viewModel::onDate)
                OutlinedTextField(
                    value = state.note,
                    onValueChange = viewModel::onNote,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota") },
                    supportingText = { Text("Opcional") },
                )
                PrimaryButton(
                    text = if (state.saving) "Guardando…" else "Guardar",
                    onClick = { viewModel.save(onSaved) },
                    enabled = !state.saving,
                )
            }
        }
    }
}
