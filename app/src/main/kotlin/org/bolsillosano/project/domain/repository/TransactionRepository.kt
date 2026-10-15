package org.bolsillosano.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.bolsillosano.project.domain.model.Transaction

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>

    fun observeById(id: Long): Flow<Transaction?>

    suspend fun save(transaction: Transaction): Long

    suspend fun delete(id: Long)

    suspend fun insertDemoData()

    suspend fun deleteDemoData()
}
