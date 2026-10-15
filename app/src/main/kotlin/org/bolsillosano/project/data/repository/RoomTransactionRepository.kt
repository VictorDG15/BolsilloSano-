package org.bolsillosano.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bolsillosano.project.data.demo.DemoTransactions
import org.bolsillosano.project.data.local.TransactionDao
import org.bolsillosano.project.data.mapper.toDomain
import org.bolsillosano.project.data.mapper.toEntity
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.repository.TransactionRepository
import org.bolsillosano.project.domain.time.DateProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dateProvider: DateProvider,
) : TransactionRepository {
    override fun observeAll(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Transaction?> =
        transactionDao.observeById(id).map { entity -> entity?.toDomain() }

    override suspend fun save(transaction: Transaction): Long =
        transactionDao.upsert(transaction.toEntity())

    override suspend fun delete(id: Long) {
        transactionDao.deleteById(id)
    }

    override suspend fun insertDemoData() {
        transactionDao.deleteDemo()
        DemoTransactions.create(dateProvider.today()).forEach { sample ->
            transactionDao.upsert(sample.toEntity())
        }
    }

    override suspend fun deleteDemoData() {
        transactionDao.deleteDemo()
    }
}
