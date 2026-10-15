package org.bolsillosano.project.domain.money

import org.bolsillosano.project.domain.model.Budget
import org.bolsillosano.project.domain.model.Money
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode

enum class BudgetLevel {
    Ok,
    Near,
    Over,
}

data class BudgetStatus(
    val budget: Budget,
    val spent: Money,
    val remaining: Money,
    val consumedPercent: BigDecimal,
    val level: BudgetLevel,
)

object BudgetCalculator {
    const val NEAR_LIMIT_RATIO = "0.80"

    fun assess(budget: Budget, transactions: List<Transaction>): BudgetStatus {
        val spent = transactions
            .filter { it.type == TransactionType.Expense }
            .filter { it.categoryId == budget.categoryId }
            .filter { java.time.YearMonth.from(it.date) == budget.period }
            .fold(Money.Zero) { acc, transaction -> acc + transaction.amount }
        val remaining = budget.limit - spent
        val percent = if (budget.limit.minorUnits == 0L) {
            BigDecimal.ZERO
        } else {
            BigDecimal.valueOf(spent.minorUnits)
                .multiply(BigDecimal(100))
                .divide(BigDecimal.valueOf(budget.limit.minorUnits), 1, RoundingMode.HALF_UP)
        }
        val ratio = if (budget.limit.minorUnits == 0L) {
            BigDecimal.ZERO
        } else {
            BigDecimal.valueOf(spent.minorUnits)
                .divide(BigDecimal.valueOf(budget.limit.minorUnits), 4, RoundingMode.HALF_UP)
        }
        val level = when {
            ratio >= BigDecimal.ONE -> BudgetLevel.Over
            ratio >= BigDecimal(NEAR_LIMIT_RATIO) -> BudgetLevel.Near
            else -> BudgetLevel.Ok
        }
        return BudgetStatus(budget, spent, remaining, percent, level)
    }

    fun isDuplicate(existing: List<Budget>, candidate: Budget): Boolean =
        existing.any {
            it.id != candidate.id &&
                it.categoryId == candidate.categoryId &&
                it.period == candidate.period
        }
}
