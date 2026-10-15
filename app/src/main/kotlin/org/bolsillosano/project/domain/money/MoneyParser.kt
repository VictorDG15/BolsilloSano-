package org.bolsillosano.project.domain.money

import org.bolsillosano.project.domain.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

sealed interface MoneyParseResult {
    data class Valid(val money: Money) : MoneyParseResult

    data class Invalid(val message: String) : MoneyParseResult
}

object MoneyParser {
    fun parse(raw: String): MoneyParseResult {
        val trimmed = raw.trim().replace(" ", "")
        if (trimmed.isEmpty()) {
            return MoneyParseResult.Invalid("Ingresa un importe")
        }
        val normalized = trimmed.replace(',', '.')
        if (normalized.count { it == '.' } > 1) {
            return MoneyParseResult.Invalid("Usa un solo separador decimal")
        }
        val decimal = try {
            BigDecimal(normalized)
        } catch (_: NumberFormatException) {
            return MoneyParseResult.Invalid("El importe no es válido")
        }
        if (decimal.signum() <= 0) {
            return MoneyParseResult.Invalid("El importe debe ser mayor que cero")
        }
        return try {
            val rounded = decimal.setScale(2, RoundingMode.HALF_UP)
            val minor = rounded.movePointRight(2).longValueExact()
            MoneyParseResult.Valid(Money(minor))
        } catch (_: ArithmeticException) {
            MoneyParseResult.Invalid("El importe es demasiado grande")
        }
    }
}
