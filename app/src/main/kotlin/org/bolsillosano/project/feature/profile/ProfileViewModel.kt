package org.bolsillosano.project.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.bolsillosano.project.BuildConfig
import org.bolsillosano.project.domain.model.CurrencyCode
import org.bolsillosano.project.domain.model.ThemeMode
import org.bolsillosano.project.domain.model.UserPreferences
import org.bolsillosano.project.domain.repository.PreferencesRepository
import org.bolsillosano.project.domain.repository.TransactionRepository
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val currency: CurrencyCode = CurrencyCode.Pen,
    val themeMode: ThemeMode = ThemeMode.System,
    val showCents: Boolean = true,
    val version: String = BuildConfig.VERSION_NAME,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
    private val transactions: TransactionRepository,
) : ViewModel() {
    val state: StateFlow<ProfileUiState> = preferences.observe()
        .map { it.toUi() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun onNameChange(value: String) {
        viewModelScope.launch { preferences.setDisplayName(value) }
    }

    fun onCurrency(currency: CurrencyCode) {
        viewModelScope.launch { preferences.setCurrency(currency) }
    }

    fun onTheme(themeMode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(themeMode) }
    }

    fun onShowCents(enabled: Boolean) {
        viewModelScope.launch { preferences.setShowCents(enabled) }
    }

    fun loadDemo() {
        viewModelScope.launch { transactions.insertDemoData() }
    }

    fun clearDemo() {
        viewModelScope.launch { transactions.deleteDemoData() }
    }

    private fun UserPreferences.toUi() = ProfileUiState(
        name = displayName,
        currency = currency,
        themeMode = themeMode,
        showCents = showCents,
    )
}
