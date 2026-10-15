package org.bolsillosano.project.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.bolsillosano.project.data.repository.DataStorePreferencesRepository
import org.bolsillosano.project.data.repository.RoomBudgetRepository
import org.bolsillosano.project.data.repository.RoomCategoryRepository
import org.bolsillosano.project.data.repository.RoomTransactionRepository
import org.bolsillosano.project.data.time.SystemDateProvider
import org.bolsillosano.project.domain.repository.BudgetRepository
import org.bolsillosano.project.domain.repository.CategoryRepository
import org.bolsillosano.project.domain.repository.PreferencesRepository
import org.bolsillosano.project.domain.repository.TransactionRepository
import org.bolsillosano.project.domain.time.DateProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun transactions(impl: RoomTransactionRepository): TransactionRepository

    @Binds
    @Singleton
    abstract fun categories(impl: RoomCategoryRepository): CategoryRepository

    @Binds
    @Singleton
    abstract fun budgets(impl: RoomBudgetRepository): BudgetRepository

    @Binds
    @Singleton
    abstract fun preferences(impl: DataStorePreferencesRepository): PreferencesRepository

    @Binds
    @Singleton
    abstract fun dates(impl: SystemDateProvider): DateProvider
}
