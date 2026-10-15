package org.bolsillosano.project.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.bolsillosano.project.core.designsystem.component.BolsilloTopBar
import org.bolsillosano.project.core.designsystem.component.PrimaryButton
import org.bolsillosano.project.core.designsystem.component.SecondaryButton
import org.bolsillosano.project.domain.model.CurrencyCode
import org.bolsillosano.project.domain.model.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by remember(state.name) { mutableStateOf(state.name) }
    Scaffold(topBar = { BolsilloTopBar("Perfil") }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Los datos se quedan en este dispositivo. No hay cuenta ni sincronización.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.onNameChange(name) }),
            )
            SecondaryButton(text = "Guardar nombre", onClick = { viewModel.onNameChange(name) })
            Text("Moneda", style = MaterialTheme.typography.titleMedium)
            Text(
                "Cambiar la moneda solo cambia el formato. No convierte los importes ya guardados.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrencyCode.entries.forEach { currency ->
                    FilterChip(
                        selected = state.currency == currency,
                        onClick = { viewModel.onCurrency(currency) },
                        label = { Text(currency.code) },
                    )
                }
            }
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val label = when (mode) {
                        ThemeMode.System -> "Sistema"
                        ThemeMode.Light -> "Claro"
                        ThemeMode.Dark -> "Oscuro"
                    }
                    FilterChip(selected = state.themeMode == mode, onClick = { viewModel.onTheme(mode) }, label = { Text(label) })
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Mostrar céntimos", style = MaterialTheme.typography.titleMedium)
                Switch(checked = state.showCents, onCheckedChange = viewModel::onShowCents)
            }
            Text("Datos de demostración", style = MaterialTheme.typography.titleMedium)
            Text("El ejemplo queda marcado en cada movimiento y se puede quitar sin borrar tus registros.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryButton(text = "Cargar datos de demostración", onClick = viewModel::loadDemo)
            SecondaryButton(text = "Quitar datos de demostración", onClick = viewModel::clearDemo)
            Text("BolsilloSano", style = MaterialTheme.typography.titleMedium)
            Text("Versión ${state.version}")
            Text("Finanzas personales en modo local.")
        }
    }
}
