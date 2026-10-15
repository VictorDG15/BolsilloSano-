package org.bolsillosano.project.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.bolsillosano.project.core.common.DateFormatter
import org.bolsillosano.project.core.common.MoneyFormatter
import org.bolsillosano.project.domain.model.Category
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import org.bolsillosano.project.domain.model.UserPreferences
import org.bolsillosano.project.domain.money.TransactionQuery
import org.bolsillosano.project.domain.money.applyQuery
import org.bolsillosano.project.domain.repository.CategoryRepository
import org.bolsillosano.project.domain.repository.PreferencesRepository
import org.bolsillosano.project.domain.repository.TransactionRepository
import org.bolsillosano.project.domain.time.DateProvider
import java.time.LocalDate
import javax.inject.Inject

data class TransactionRow(
    val id: Long,
    val title: String,
    val category: String,
    val iconKey: String,
    val amount: String,
    val income: Boolean,
    val isDemo: Boolean,
)

data class TransactionSection(
    val title: String,
    val rows: List<TransactionRow>,
)

data class TransactionsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val type: TransactionType? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val sections: List<TransactionSection> = emptyList(),
    val empty: Boolean = false,
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactions: TransactionRepository,
    categories: CategoryRepository,
    preferences: PreferencesRepository,
    private val dates: DateProvider,
) : ViewModel() {
    private val query = MutableStateFlow(TransactionQuery())

    val state: StateFlow<TransactionsUiState> = combine(
        transactions.observeAll(),
        categories.observeAll(),
        preferences.observe(),
        query,
    ) { items, categoryList, prefs, currentQuery ->
        toState(items, categoryList, prefs, currentQuery)
    }.catch {
        emit(TransactionsUiState(loading = false, error = "No se pudieron leer los movimientos."))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun onQueryChange(value: String) = query.update { it.copy(text = value) }

    fun onTypeChange(type: TransactionType?) = query.update { it.copy(type = type) }

    fun onFromChange(date: LocalDate?) = query.update { it.copy(from = date) }

    fun onToChange(date: LocalDate?) = query.update { it.copy(to = date) }

    fun clearDates() = query.update { it.copy(from = null, to = null) }

    private fun toState(
        items: List<Transaction>,
        categories: List<Category>,
        preferences: UserPreferences,
        currentQuery: TransactionQuery,
    ): TransactionsUiState {
        val today = dates.today()
        val byCategory = categories.associateBy { it.id }
        val filtered = items.applyQuery(currentQuery)
        val sections = filtered.groupBy { it.date }.map { (date, transactions) ->
            TransactionSection(
                title = DateFormatter.section(date, today),
                rows = transactions.map { transaction ->
                    val category = byCategory[transaction.categoryId]
                    TransactionRow(
                        id = transaction.id,
                        title = transaction.description,
                        category = category?.name ?: "Sin categoría",
                        iconKey = category?.iconKey ?: "other_expense",
                        amount = MoneyFormatter.formatSigned(
                            transaction.amount,
                            transaction.type,
                            preferences.currency,
                            preferences.showCents,
                        ),
                        income = transaction.type == TransactionType.Income,
                        isDemo = transaction.isDemo,
                    )
                },
            )
        }
        return TransactionsUiState(
            loading = false,
            query = currentQuery.text,
            type = currentQuery.type,
            from = currentQuery.from,
            to = currentQuery.to,
            sections = sections,
            empty = filtered.isEmpty(),
        )
    }
}
