package org.bolsillosano.project.data.mapper

import org.bolsillosano.project.data.local.BudgetEntity
import org.bolsillosano.project.data.local.CategoryEntity
import org.bolsillosano.project.data.local.TransactionEntity
import org.bolsillosano.project.domain.model.Budget
import org.bolsillosano.project.domain.model.Category
import org.bolsillosano.project.domain.model.Money
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    type = TransactionType.valueOf(type),
    iconKey = iconKey,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = TransactionType.valueOf(type),
    amount = Money(amountMinor),
    description = description,
    categoryId = categoryId,
    date = LocalDate.ofEpochDay(epochDay),
    note = note,
    isDemo = isDemo,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type.name,
    amountMinor = amount.minorUnits,
    description = description.trim(),
    categoryId = categoryId,
    epochDay = date.toEpochDay(),
    note = note.trim(),
    isDemo = isDemo,
)

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    categoryId = categoryId,
    period = YearMonth.of(year, month),
    limit = Money(limitMinor),
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    categoryId = categoryId,
    year = period.year,
    month = period.monthValue,
    limitMinor = limit.minorUnits,
)
