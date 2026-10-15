package org.bolsillosano.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.bolsillosano.project.domain.model.CurrencyCode
import org.bolsillosano.project.domain.model.ThemeMode
import org.bolsillosano.project.domain.model.UserPreferences

interface PreferencesRepository {
    fun observe(): Flow<UserPreferences>

    suspend fun setDisplayName(name: String)

    suspend fun setCurrency(currency: CurrencyCode)

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setShowCents(showCents: Boolean)
}
