package org.bolsillosano.project.domain.model

import java.time.YearMonth

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val period: YearMonth,
    val limit: Money,
)
