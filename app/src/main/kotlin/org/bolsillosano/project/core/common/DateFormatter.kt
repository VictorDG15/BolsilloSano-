package org.bolsillosano.project.core.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private val locale: Locale = Locale.forLanguageTag("es-PE")
    private val full: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale)
    private val short: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    private val month: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

    fun section(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> full.format(date).replaceFirstChar { it.titlecase(locale) }
    }

    fun short(date: LocalDate): String = short.format(date)

    fun month(date: LocalDate): String = month.format(date).replaceFirstChar { it.titlecase(locale) }
}
