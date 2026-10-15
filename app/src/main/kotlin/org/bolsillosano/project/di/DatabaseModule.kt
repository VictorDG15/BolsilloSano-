package org.bolsillosano.project.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import org.bolsillosano.project.data.local.BolsilloSanoDatabase
import org.bolsillosano.project.data.local.BudgetDao
import org.bolsillosano.project.data.local.CategoryDao
import org.bolsillosano.project.data.local.TransactionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): BolsilloSanoDatabase =
        Room.databaseBuilder(
            context,
            BolsilloSanoDatabase::class.java,
            "bolsillosano.db",
        ).setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    @Provides
    fun categoryDao(database: BolsilloSanoDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun transactionDao(database: BolsilloSanoDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun budgetDao(database: BolsilloSanoDatabase): BudgetDao = database.budgetDao()
}
