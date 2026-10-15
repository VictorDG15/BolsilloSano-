package org.bolsillosano.project.data.demo

import org.bolsillosano.project.domain.model.Money
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import java.time.LocalDate

object DemoTransactions {
    fun create(today: LocalDate): List<Transaction> {
        val month = today.withDayOfMonth(1)
        return listOf(
            sample(TransactionType.Income, 350_000, "Salario (ejemplo)", 1, month.plusDays(1)),
            sample(TransactionType.Income, 45_000, "Proyecto freelance (ejemplo)", 2, month.plusDays(10)),
            sample(TransactionType.Expense, 18_500, "Supermercado (ejemplo)", 5, month.plusDays(3)),
            sample(TransactionType.Expense, 6_200, "Pasajes (ejemplo)", 6, month.plusDays(4)),
            sample(TransactionType.Expense, 90_000, "Alquiler (ejemplo)", 7, month.plusDays(2)),
            sample(TransactionType.Expense, 4_800, "Farmacia (ejemplo)", 8, month.plusDays(8)),
            sample(TransactionType.Expense, 7_500, "Cine (ejemplo)", 10, month.plusDays(12)),
            sample(TransactionType.Expense, 12_000, "Luz e internet (ejemplo)", 11, month.plusDays(6)),
        )
    }

    private fun sample(
        type: TransactionType,
        minor: Long,
        description: String,
        categoryId: Long,
        date: LocalDate,
    ): Transaction = Transaction(
        type = type,
        amount = Money(minor),
        description = description,
        categoryId = categoryId,
        date = date,
        note = "Dato de demostración",
        isDemo = true,
    )
}
