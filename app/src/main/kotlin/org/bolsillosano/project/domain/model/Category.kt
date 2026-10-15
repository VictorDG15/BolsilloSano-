package org.bolsillosano.project.domain.model

data class Category(
    val id: Long,
    val name: String,
    val type: TransactionType,
    val iconKey: String,
)
