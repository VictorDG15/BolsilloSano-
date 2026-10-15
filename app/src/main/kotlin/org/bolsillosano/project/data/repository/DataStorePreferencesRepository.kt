package org.bolsillosano.project.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bolsillosano.project.domain.model.CurrencyCode
import org.bolsillosano.project.domain.model.ThemeMode
import org.bolsillosano.project.domain.model.UserPreferences
import org.bolsillosano.project.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

private val Context.preferencesDataStore by preferencesDataStore(name = "bolsillosano_preferences")

@Singleton
class DataStorePreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) : PreferencesRepository {
    private val dataStore = context.preferencesDataStore

    override fun observe(): Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            displayName = preferences[Keys.Name].orEmpty(),
            currency = CurrencyCode.fromCode(preferences[Keys.Currency] ?: CurrencyCode.Pen.code),
            themeMode = preferences[Keys.Theme]?.let { value ->
                runCatching { ThemeMode.valueOf(value) }.getOrDefault(ThemeMode.System)
            } ?: ThemeMode.System,
            showCents = preferences[Keys.ShowCents] ?: true,
        )
    }

    override suspend fun setDisplayName(name: String) {
        dataStore.edit { it[Keys.Name] = name.trim() }
    }

    override suspend fun setCurrency(currency: CurrencyCode) {
        dataStore.edit { it[Keys.Currency] = currency.code }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.Theme] = themeMode.name }
    }

    override suspend fun setShowCents(showCents: Boolean) {
        dataStore.edit { it[Keys.ShowCents] = showCents }
    }

    private object Keys {
        val Name = stringPreferencesKey("display_name")
        val Currency = stringPreferencesKey("currency")
        val Theme = stringPreferencesKey("theme_mode")
        val ShowCents = booleanPreferencesKey("show_cents")
    }
}
