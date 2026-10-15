package org.bolsillosano.project.domain.money

import org.bolsillosano.project.domain.model.Budget
import org.bolsillosano.project.domain.model.Category
import org.bolsillosano.project.domain.model.Money
import org.bolsillosano.project.domain.model.PeriodPreset
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class FinanceRulesTest {
    private val today = LocalDate.of(2026, 10, 12)
    private val food = Category(5, "Alimentación", TransactionType.Expense, "food")
    private val salary = Category(1, "Salario", TransactionType.Income, "salary")

    @Test
    fun balanceIsIncomeMinusExpense() {
        val transactions = listOf(
            movement(TransactionType.Income, 1_000, 1, today),
            movement(TransactionType.Expense, 250, 5, today),
        )
        assertEquals(Money(750), FinanceSummary.net(transactions))
        assertEquals(Money(1_000), FinanceSummary.total(transactions, TransactionType.Income))
        assertEquals(Money(250), FinanceSummary.total(transactions, TransactionType.Expense))
    }

    @Test
    fun dashboardUsesAllHistoryForBalanceAndTheMonthForTotals() {
        val transactions = listOf(
            movement(TransactionType.Income, 500, 1, today.minusMonths(1)),
            movement(TransactionType.Income, 200, 1, today),
            movement(TransactionType.Expense, 50, 5, today),
        )
        val summary = FinanceSummary.dashboard(transactions, listOf(salary, food), today, PeriodPreset.ThisMonth)
        assertEquals(Money(650), summary.balance)
        assertEquals(Money(200), summary.income)
        assertEquals(Money(50), summary.expense)
        assertEquals(1, summary.expenseByCategory.size)
    }

    @Test
    fun variationStaysHiddenWithoutAPreviousPeriod() {
        val summary = FinanceSummary.dashboard(
            listOf(movement(TransactionType.Income, 100, 1, today)),
            listOf(salary),
            today,
            PeriodPreset.ThisMonth,
        )
        assertNull(summary.variationPercent)
    }

    @Test
    fun filterCombinesTextTypeAndDates() {
        val items = listOf(
            movement(TransactionType.Expense, 10, 5, today, "Café"),
            movement(TransactionType.Income, 20, 1, today, "Café laboral"),
            movement(TransactionType.Expense, 30, 5, today.minusDays(10), "Café viejo"),
        )
        val result = items.applyQuery(
            TransactionQuery(text = "café", type = TransactionType.Expense, from = today.minusDays(1), to = today),
        )
        assertEquals(listOf("Café"), result.map { it.description })
    }

    @Test
    fun budgetCountsOnlyMatchingExpenseAndWarnsNearTheLimit() {
        val budget = Budget(id = 1, categoryId = 5, period = YearMonth.from(today), limit = Money(10_000))
        val transactions = listOf(
            movement(TransactionType.Expense, 8_000, 5, today),
            movement(TransactionType.Expense, 9_000, 6, today),
            movement(TransactionType.Income, 8_000, 5, today),
            movement(TransactionType.Expense, 8_000, 5, today.minusMonths(1)),
        )
        val status = BudgetCalculator.assess(budget, transactions)
        assertEquals(Money(8_000), status.spent)
        assertEquals(Money(2_000), status.remaining)
        assertEquals(BudgetLevel.Near, status.level)
        assertTrue(
            BudgetCalculator.isDuplicate(
                listOf(budget),
                budget.copy(id = 2),
            ),
        )
    }

    private fun movement(
        type: TransactionType,
        minor: Long,
        categoryId: Long,
        date: LocalDate,
        description: String = "Movimiento",
    ) = Transaction(
        id = minor,
        type = type,
        amount = Money(minor),
        description = description,
        categoryId = categoryId,
        date = date,
    )
}
