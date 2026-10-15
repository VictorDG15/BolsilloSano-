package org.bolsillosano.project.domain.model

import java.time.LocalDate
import java.time.YearMonth

enum class PeriodPreset {
    ThisMonth,
    PreviousMonth,
}

fun PeriodPreset.range(today: LocalDate): ClosedRange<LocalDate> {
    val month = when (this) {
        PeriodPreset.ThisMonth -> YearMonth.from(today)
        PeriodPreset.PreviousMonth -> YearMonth.from(today).minusMonths(1)
    }
    return month.atDay(1)..month.atEndOfMonth()
}

fun PeriodPreset.previousRange(today: LocalDate): ClosedRange<LocalDate> {
    val current = range(today)
    val length = current.endInclusive.toEpochDay() - current.start.toEpochDay()
    val previousEnd = current.start.minusDays(1)
    val previousStart = previousEnd.minusDays(length)
    return previousStart..previousEnd
}

fun ClosedRange<LocalDate>.contains(date: LocalDate): Boolean =
    date >= start && date <= endInclusive
