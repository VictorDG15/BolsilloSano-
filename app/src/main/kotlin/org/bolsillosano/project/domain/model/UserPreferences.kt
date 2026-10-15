package org.bolsillosano.project.domain.model

enum class ThemeMode {
    System,
    Light,
    Dark,
}

enum class CurrencyCode(val code: String) {
    Pen("PEN"),
    Usd("USD"),
    Eur("EUR"),
    ;

    companion object {
        fun fromCode(code: String): CurrencyCode =
            entries.firstOrNull { it.code == code } ?: Pen
    }
}

data class UserPreferences(
    val displayName: String = "",
    val currency: CurrencyCode = CurrencyCode.Pen,
    val themeMode: ThemeMode = ThemeMode.System,
    val showCents: Boolean = true,
)
