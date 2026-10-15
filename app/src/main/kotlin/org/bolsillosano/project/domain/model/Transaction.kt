package org.bolsillosano.project.domain.model

import java.time.LocalDate

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Money,
    val description: String,
    val categoryId: Long,
    val date: LocalDate,
    val note: String = "",
    val isDemo: Boolean = false,
)
