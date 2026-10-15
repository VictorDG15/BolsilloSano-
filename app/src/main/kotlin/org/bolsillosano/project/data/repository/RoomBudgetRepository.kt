package org.bolsillosano.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bolsillosano.project.data.local.BudgetDao
import org.bolsillosano.project.data.mapper.toDomain
import org.bolsillosano.project.data.mapper.toEntity
import org.bolsillosano.project.domain.model.Budget
import org.bolsillosano.project.domain.money.BudgetCalculator
import org.bolsillosano.project.domain.repository.BudgetRepository
import org.bolsillosano.project.domain.repository.BudgetSaveResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
) : BudgetRepository {
    override fun observeAll(): Flow<List<Budget>> =
        budgetDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Budget?> =
        budgetDao.observeById(id).map { entity -> entity?.toDomain() }

    override suspend fun save(budget: Budget): BudgetSaveResult {
        val existing = budgetDao.listAll().map { it.toDomain() }
        if (BudgetCalculator.isDuplicate(existing, budget)) {
            return BudgetSaveResult.DuplicatePeriod
        }
        budgetDao.upsert(budget.toEntity())
        return BudgetSaveResult.Saved
    }

    override suspend fun delete(id: Long) {
        budgetDao.deleteById(id)
    }
}
