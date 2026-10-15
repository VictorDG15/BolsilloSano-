package org.bolsillosano.project.data.local

import org.bolsillosano.project.domain.model.TransactionType

object CategorySeed {
    val entities: List<CategoryEntity> = listOf(
        CategoryEntity(1, "Salario", TransactionType.Income.name, "salary"),
        CategoryEntity(2, "Freelance", TransactionType.Income.name, "freelance"),
        CategoryEntity(3, "Inversiones", TransactionType.Income.name, "investment"),
        CategoryEntity(4, "Otros ingresos", TransactionType.Income.name, "other_income"),
        CategoryEntity(5, "Alimentación", TransactionType.Expense.name, "food"),
        CategoryEntity(6, "Transporte", TransactionType.Expense.name, "transport"),
        CategoryEntity(7, "Vivienda", TransactionType.Expense.name, "housing"),
        CategoryEntity(8, "Salud", TransactionType.Expense.name, "health"),
        CategoryEntity(9, "Educación", TransactionType.Expense.name, "education"),
        CategoryEntity(10, "Ocio", TransactionType.Expense.name, "leisure"),
        CategoryEntity(11, "Servicios", TransactionType.Expense.name, "services"),
        CategoryEntity(12, "Otros gastos", TransactionType.Expense.name, "other_expense"),
    )
}
