package org.bolsillosano.project.core.common

import org.bolsillosano.project.domain.model.CurrencyCode
import org.bolsillosano.project.domain.model.Money
import org.bolsillosano.project.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {
    private val locale: Locale = Locale.forLanguageTag("es-PE")

    fun format(money: Money, currency: CurrencyCode, showCents: Boolean): String {
        val amount = BigDecimal.valueOf(money.minorUnits).movePointLeft(2)
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = Currency.getInstance(currency.code)
        format.roundingMode = RoundingMode.HALF_UP
        if (showCents) {
            format.minimumFractionDigits = 2
            format.maximumFractionDigits = 2
        } else {
            format.minimumFractionDigits = 0
            format.maximumFractionDigits = 0
        }
        return format.format(amount)
    }

    fun formatSigned(
        money: Money,
        type: TransactionType,
        currency: CurrencyCode,
        showCents: Boolean,
    ): String {
        val sign = when (type) {
            TransactionType.Income -> "+"
            TransactionType.Expense -> "−"
        }
        return sign + format(money, currency, showCents)
    }
}
