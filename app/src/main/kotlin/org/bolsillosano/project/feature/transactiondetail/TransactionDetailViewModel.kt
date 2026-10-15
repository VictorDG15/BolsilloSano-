package org.bolsillosano.project.feature.transactiondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.bolsillosano.project.core.common.DateFormatter
import org.bolsillosano.project.core.common.MoneyFormatter
import org.bolsillosano.project.domain.repository.CategoryRepository
import org.bolsillosano.project.domain.repository.PreferencesRepository
import org.bolsillosano.project.domain.repository.TransactionRepository
import javax.inject.Inject

data class TransactionDetailUiState(
    val id: Long = 0,
    val loading: Boolean = true,
    val missing: Boolean = false,
    val title: String = "",
    val amount: String = "",
    val income: Boolean = false,
    val category: String = "",
    val iconKey: String = "",
    val date: String = "",
    val note: String = "",
    val demo: Boolean = false,
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactions: TransactionRepository,
    categories: CategoryRepository,
    preferences: PreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id: Long = savedStateHandle.get<Long>("id") ?: 0L

    val state: StateFlow<TransactionDetailUiState> = combine(
        transactions.observeById(id),
        categories.observeAll(),
        preferences.observe(),
    ) { transaction, categoryList, prefs ->
        if (transaction == null) {
            TransactionDetailUiState(loading = false, missing = true)
        } else {
            val category = categoryList.firstOrNull { it.id == transaction.categoryId }
            TransactionDetailUiState(
                id = transaction.id,
                loading = false,
                title = transaction.description,
                amount = MoneyFormatter.formatSigned(transaction.amount, transaction.type, prefs.currency, prefs.showCents),
                income = transaction.type.name == "Income",
                category = category?.name ?: "Sin categoría",
                iconKey = category?.iconKey ?: "other_expense",
                date = DateFormatter.short(transaction.date),
                note = transaction.note,
                demo = transaction.isDemo,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionDetailUiState())

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            transactions.delete(id)
            onDeleted()
        }
    }
}
