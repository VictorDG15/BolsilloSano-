package org.bolsillosano.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.bolsillosano.project.data.local.CategoryDao
import org.bolsillosano.project.data.local.CategorySeed
import org.bolsillosano.project.data.mapper.toDomain
import org.bolsillosano.project.domain.model.Category
import org.bolsillosano.project.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> = categoryDao.observeAll()
        .onStart { seedIfEmpty() }
        .map { entities -> entities.map { it.toDomain() } }

    private suspend fun seedIfEmpty() {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(CategorySeed.entities)
        }
    }
}
