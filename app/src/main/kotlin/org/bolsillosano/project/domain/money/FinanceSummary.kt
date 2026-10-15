package org.bolsillosano.project.domain.money

import org.bolsillosano.project.domain.model.Category
import org.bolsillosano.project.domain.model.Money
import org.bolsillosano.project.domain.model.PeriodPreset
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import org.bolsillosano.project.domain.model.contains
import org.bolsillosano.project.domain.model.previousRange
import org.bolsillosano.project.domain.model.range
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class CategoryTotal(
    val category: Category,
    val amount: Money,
)

data class DashboardSummary(
    val balance: Money,
    val income: Money,
    val expense: Money,
    val variationPercent: BigDecimal?,
    val expenseByCategory: List<CategoryTotal>,
    val recent: List<Transaction>,
    val hasAnyTransaction: Boolean,
    val hasTransactionsInPeriod: Boolean,
)

data class WeekTotal(
    val label: String,
    val income: Money,
    val expense: Money,
)

object FinanceSummary {
    fun dashboard(
        transactions: List<Transaction>,
        categories: List<Category>,
        today: LocalDate,
        period: PeriodPreset,
    ): DashboardSummary {
        val current = transactions.filter { period.range(today).contains(it.date) }
        val previous = transactions.filter { period.previousRange(today).contains(it.date) }
        val categoriesById = categories.associateBy { it.id }
        return DashboardSummary(
            balance = net(transactions),
            income = total(current, TransactionType.Income),
            expense = total(current, TransactionType.Expense),
            variationPercent = variation(net(current), net(previous), previous.isNotEmpty()),
            expenseByCategory = totalsByCategory(current, categoriesById, TransactionType.Expense),
            recent = transactions.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id }).take(5),
            hasAnyTransaction = transactions.isNotEmpty(),
            hasTransactionsInPeriod = current.isNotEmpty(),
        )
    }

    fun net(transactions: List<Transaction>): Money =
        transactions.fold(Money.Zero) { acc, transaction ->
            when (transaction.type) {
                TransactionType.Income -> acc + transaction.amount
                TransactionType.Expense -> acc - transaction.amount
            }
        }

    fun total(transactions: List<Transaction>, type: TransactionType): Money =
        transactions.filter { it.type == type }.fold(Money.Zero) { acc, transaction -> acc + transaction.amount }

    fun totalsByCategory(
        transactions: List<Transaction>,
        categoriesById: Map<Long, Category>,
        type: TransactionType,
    ): List<CategoryTotal> =
        transactions
            .filter { it.type == type }
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, items) ->
                val category = categoriesById[categoryId] ?: return@mapNotNull null
                CategoryTotal(category, items.fold(Money.Zero) { acc, transaction -> acc + transaction.amount })
            }
            .sortedByDescending { it.amount.minorUnits }

    fun weekly(transactions: List<Transaction>, today: LocalDate, period: PeriodPreset): List<WeekTotal> {
        val range = period.range(today)
        val inPeriod = transactions.filter { range.contains(it.date) }
        if (inPeriod.isEmpty()) return emptyList()
        return inPeriod
            .groupBy { it.date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)) }
            .toSortedMap()
            .map { (weekStart, items) ->
                WeekTotal(
                    label = weekStart.dayOfMonth.toString(),
                    income = total(items, TransactionType.Income),
                    expense = total(items, TransactionType.Expense),
                )
            }
    }

    private fun variation(current: Money, previous: Money, previousHasData: Boolean): BigDecimal? {
        if (!previousHasData || previous.minorUnits == 0L) return null
        return BigDecimal.valueOf(current.minorUnits - previous.minorUnits)
            .multiply(BigDecimal(100))
            .divide(BigDecimal.valueOf(kotlin.math.abs(previous.minorUnits)), 1, RoundingMode.HALF_UP)
    }
}
