package org.bolsillosano.project

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.bolsillosano.project.core.designsystem.component.EmptyState
import org.bolsillosano.project.core.designsystem.theme.BolsilloSanoTheme
import org.junit.Rule
import org.junit.Test

class EmptyStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun emptyStateShowsItsMessage() {
        rule.setContent {
            BolsilloSanoTheme(darkTheme = false) {
                EmptyState(
                    title = "Sin movimientos",
                    body = "Registra un ingreso o un gasto.",
                )
            }
        }
        rule.onNodeWithText("Sin movimientos").assertIsDisplayed()
        rule.onNodeWithText("Registra un ingreso o un gasto.").assertIsDisplayed()
    }
}
