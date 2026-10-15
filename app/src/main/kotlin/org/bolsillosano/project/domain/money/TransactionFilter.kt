package org.bolsillosano.project.domain.money

import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import java.time.LocalDate

data class TransactionQuery(
    val text: String = "",
    val type: TransactionType? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
)

fun List<Transaction>.applyQuery(query: TransactionQuery): List<Transaction> {
    val needle = query.text.trim()
    return filter { transaction ->
        val matchesText = needle.isEmpty() || transaction.description.contains(needle, ignoreCase = true)
        val matchesType = query.type == null || transaction.type == query.type
        val matchesFrom = query.from == null || transaction.date >= query.from
        val matchesTo = query.to == null || transaction.date <= query.to
        matchesText && matchesType && matchesFrom && matchesTo
    }.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })
}
