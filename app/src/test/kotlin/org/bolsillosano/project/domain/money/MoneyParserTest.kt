package org.bolsillosano.project.domain.money

import org.bolsillosano.project.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class MoneyParserTest {
    @Test
    fun commaAndDotBecomeCents() {
        assertEquals(1_050L, (MoneyParser.parse("10,50") as MoneyParseResult.Valid).money.minorUnits)
        assertEquals(1_050L, (MoneyParser.parse("10.5") as MoneyParseResult.Valid).money.minorUnits)
    }

    @Test
    fun halfUpRoundsTheThirdDecimal() {
        assertEquals(1_056L, (MoneyParser.parse("10,555") as MoneyParseResult.Valid).money.minorUnits)
    }

    @Test
    fun rejectsBlankZeroAndNegative() {
        assertTrue(MoneyParser.parse(" ") is MoneyParseResult.Invalid)
        assertTrue(MoneyParser.parse("0") is MoneyParseResult.Invalid)
        assertTrue(MoneyParser.parse("-4") is MoneyParseResult.Invalid)
        assertTrue(MoneyParser.parse("abc") is MoneyParseResult.Invalid)
    }

    @Test
    fun sumsStayInMinorUnits() {
        assertEquals(Money(150), Money(100) + Money(50))
        assertEquals(Money(40), Money(100) - Money(60))
    }

    @Test
    fun variationUsesExactScale() {
        val percent = BigDecimal("12.5")
        assertEquals(0, percent.compareTo(BigDecimal("12.5")))
    }
}
