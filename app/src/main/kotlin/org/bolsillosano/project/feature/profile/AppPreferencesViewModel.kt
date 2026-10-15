package org.bolsillosano.project.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.bolsillosano.project.domain.model.UserPreferences
import org.bolsillosano.project.domain.repository.PreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AppPreferencesViewModel @Inject constructor(
    preferences: PreferencesRepository,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = preferences.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())
}
