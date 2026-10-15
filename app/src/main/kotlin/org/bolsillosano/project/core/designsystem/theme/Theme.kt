package org.bolsillosano.project.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class BolsilloColors(
    val income: Color,
    val expense: Color,
    val warning: Color,
    val border: Color,
)

private val LightBolsilloColors = BolsilloColors(
    income = IncomeLight,
    expense = ExpenseLight,
    warning = WarningLight,
    border = BorderLight,
)

private val DarkBolsilloColors = BolsilloColors(
    income = IncomeDark,
    expense = ExpenseDark,
    warning = WarningDark,
    border = BorderDark,
)

val LocalBolsilloColors = staticCompositionLocalOf { LightBolsilloColors }

private val LightScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE7FF),
    onPrimaryContainer = BlueDark,
    secondary = BlueDark,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = ExpenseLight,
)

private val DarkScheme = darkColorScheme(
    primary = BlueOnDark,
    onPrimary = Color(0xFF0B1220),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBE7FF),
    secondary = BlueOnDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = ExpenseDark,
)

@Composable
fun BolsilloSanoTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val extra = if (darkTheme) DarkBolsilloColors else LightBolsilloColors
    CompositionLocalProvider(LocalBolsilloColors provides extra) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = BolsilloTypography,
            shapes = BolsilloShapes,
            content = content,
        )
    }
}
