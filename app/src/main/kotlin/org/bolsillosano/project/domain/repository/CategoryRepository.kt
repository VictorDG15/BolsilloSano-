package org.bolsillosano.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.bolsillosano.project.domain.model.Category

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
}
