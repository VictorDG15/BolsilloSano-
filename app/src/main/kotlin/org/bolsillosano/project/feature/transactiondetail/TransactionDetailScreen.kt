package org.bolsillosano.project.feature.transactiondetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.bolsillosano.project.core.designsystem.component.BolsilloTopBar
import org.bolsillosano.project.core.designsystem.component.CategoryIcon
import org.bolsillosano.project.core.designsystem.component.ConfirmationDialog
import org.bolsillosano.project.core.designsystem.component.EmptyState
import org.bolsillosano.project.core.designsystem.component.LoadingState
import org.bolsillosano.project.core.designsystem.component.PrimaryButton
import org.bolsillosano.project.core.designsystem.component.SecondaryButton
import org.bolsillosano.project.core.designsystem.theme.LocalBolsilloColors

@Composable
fun TransactionDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirm by rememberSaveable { mutableStateOf(false) }
    val colors = LocalBolsilloColors.current
    Scaffold(topBar = { BolsilloTopBar("Detalle", onBack = onBack) }) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.missing -> EmptyState(
                title = "Este movimiento ya no existe",
                body = "Vuelve a la lista para ver los movimientos actuales.",
                modifier = Modifier.padding(padding),
                action = { SecondaryButton(text = "Volver a la lista", onClick = onBack) },
            )
            else -> Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CategoryIcon(state.iconKey, state.category)
                Text(state.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    state.amount,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (state.income) colors.income else colors.expense,
                )
                Text(state.category)
                Text(state.date, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (state.note.isNotBlank()) Text(state.note)
                if (state.demo) Text("Dato de demostración", color = colors.warning)
                Spacer(Modifier.weight(1f))
                PrimaryButton(text = "Editar", onClick = { onEdit(state.id) })
                SecondaryButton(text = "Eliminar", onClick = { confirm = true })
            }
        }
    }
    if (confirm) {
        ConfirmationDialog(
            title = "Eliminar movimiento",
            body = "Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            onConfirm = {
                confirm = false
                viewModel.delete(onDeleted)
            },
            onDismiss = { confirm = false },
        )
    }
}
