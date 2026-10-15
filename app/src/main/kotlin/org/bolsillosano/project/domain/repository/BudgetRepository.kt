package org.bolsillosano.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.bolsillosano.project.domain.model.Budget

sealed interface BudgetSaveResult {
    data object Saved : BudgetSaveResult

    data object DuplicatePeriod : BudgetSaveResult
}

interface BudgetRepository {
    fun observeAll(): Flow<List<Budget>>

    fun observeById(id: Long): Flow<Budget?>

    suspend fun save(budget: Budget): BudgetSaveResult

    suspend fun delete(id: Long)
}
