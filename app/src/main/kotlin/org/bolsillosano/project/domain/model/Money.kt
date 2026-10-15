package org.bolsillosano.project.domain.model

/** Céntimos. 100 = 1 sol. El texto del usuario se redondea a 2 decimales con HALF_UP y después solo se suman enteros. */
@JvmInline
value class Money(val minorUnits: Long) {
    operator fun plus(other: Money): Money = Money(minorUnits + other.minorUnits)

    operator fun minus(other: Money): Money = Money(minorUnits - other.minorUnits)

    operator fun unaryMinus(): Money = Money(-minorUnits)

    fun isPositive(): Boolean = minorUnits > 0

    companion object {
        val Zero = Money(0)
    }
}
