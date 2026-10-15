package org.bolsillosano.project.feature.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bolsillosano.project.domain.model.Category
import org.bolsillosano.project.domain.model.Money
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import org.bolsillosano.project.domain.money.MoneyParseResult
import org.bolsillosano.project.domain.money.MoneyParser
import org.bolsillosano.project.domain.repository.CategoryRepository
import org.bolsillosano.project.domain.repository.TransactionRepository
import org.bolsillosano.project.domain.time.DateProvider
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

data class TransactionFormUiState(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val editing: Boolean = false,
    val type: TransactionType = TransactionType.Expense,
    val amountText: String = "",
    val description: String = "",
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val amountError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val saving: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val transactions: TransactionRepository,
    private val categories: CategoryRepository,
    private val dates: DateProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val transactionId: Long? = savedStateHandle.get<Long>("id")
    private val _state = MutableStateFlow(
        TransactionFormUiState(
            editing = transactionId != null,
            type = if (savedStateHandle.get<String>("type") == "income") TransactionType.Income else TransactionType.Expense,
            date = dates.today(),
        ),
    )
    val state: StateFlow<TransactionFormUiState> = _state.asStateFlow()
    private var savedTransaction: Transaction? = null

    init {
        viewModelScope.launch {
            val loadedCategories = categories.observeAll().first { it.isNotEmpty() }
            val existing = transactionId?.let { transactions.observeById(it).first() }
            if (transactionId != null && existing == null) {
                _state.update { it.copy(loading = false, missing = true, categories = loadedCategories) }
            } else {
                savedTransaction = existing
                _state.update {
                    it.copy(
                        loading = false,
                        categories = loadedCategories,
                        type = existing?.type ?: it.type,
                        amountText = existing?.amount?.toInput().orEmpty(),
                        description = existing?.description.orEmpty(),
                        note = existing?.note.orEmpty(),
                        date = existing?.date ?: it.date,
                        categoryId = existing?.categoryId,
                    )
                }
            }
        }
    }

    fun onType(type: TransactionType) = _state.update { state ->
        val stillValid = state.categories.any { it.id == state.categoryId && it.type == type }
        state.copy(type = type, categoryId = if (stillValid) state.categoryId else null)
    }

    fun onAmount(value: String) = _state.update { it.copy(amountText = value, amountError = null) }

    fun onDescription(value: String) = _state.update { it.copy(description = value, descriptionError = null) }

    fun onNote(value: String) = _state.update { it.copy(note = value) }

    fun onDate(value: LocalDate) = _state.update { it.copy(date = value) }

    fun onCategory(id: Long) = _state.update { it.copy(categoryId = id, categoryError = null) }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (current.saving) return
        val parsed = MoneyParser.parse(current.amountText)
        val amountError = (parsed as? MoneyParseResult.Invalid)?.message
        val descriptionError = if (current.description.isBlank()) "La descripción es obligatoria" else null
        val categoryError = if (current.categoryId == null) "Elige una categoría" else null
        if (amountError != null || descriptionError != null || categoryError != null) {
            _state.update {
                it.copy(amountError = amountError, descriptionError = descriptionError, categoryError = categoryError)
            }
            return
        }
        val amount = (parsed as MoneyParseResult.Valid).money
        _state.update { it.copy(saving = true, message = null) }
        viewModelScope.launch {
            transactions.save(
                Transaction(
                    id = savedTransaction?.id ?: 0,
                    type = current.type,
                    amount = amount,
                    description = current.description,
                    categoryId = current.categoryId ?: return@launch,
                    date = current.date,
                    note = current.note,
                    isDemo = savedTransaction?.isDemo ?: false,
                ),
            )
            onSaved()
        }
    }

    private fun Money.toInput(): String =
        BigDecimal.valueOf(minorUnits).movePointLeft(2).toPlainString().replace('.', ',')
}
