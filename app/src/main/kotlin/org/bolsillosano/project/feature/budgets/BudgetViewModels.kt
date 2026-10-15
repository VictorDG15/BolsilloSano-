package org.bolsillosano.project.feature.budgets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bolsillosano.project.core.common.MoneyFormatter
import org.bolsillosano.project.domain.model.Budget
import org.bolsillosano.project.domain.model.Category
import org.bolsillosano.project.domain.model.TransactionType
import org.bolsillosano.project.domain.money.BudgetCalculator
import org.bolsillosano.project.domain.money.BudgetLevel
import org.bolsillosano.project.domain.money.MoneyParseResult
import org.bolsillosano.project.domain.money.MoneyParser
import org.bolsillosano.project.domain.repository.BudgetRepository
import org.bolsillosano.project.domain.repository.BudgetSaveResult
import org.bolsillosano.project.domain.repository.CategoryRepository
import org.bolsillosano.project.domain.repository.PreferencesRepository
import org.bolsillosano.project.domain.repository.TransactionRepository
import org.bolsillosano.project.domain.time.DateProvider
import java.math.BigDecimal
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class BudgetRow(
    val id: Long,
    val category: String,
    val iconKey: String,
    val spent: String,
    val limit: String,
    val remaining: String,
    val percent: String,
    val level: BudgetLevel,
)

data class BudgetsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val monthLabel: String = "",
    val rows: List<BudgetRow> = emptyList(),
    val empty: Boolean = false,
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgets: BudgetRepository,
    transactions: TransactionRepository,
    categories: CategoryRepository,
    preferences: PreferencesRepository,
    dates: DateProvider,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.from(dates.today()))

    val state: StateFlow<BudgetsUiState> = combine(
        budgets.observeAll(),
        transactions.observeAll(),
        categories.observeAll(),
        preferences.observe(),
        month,
    ) { budgetList, items, categoryList, prefs, selected ->
        val visible = budgetList.filter { it.period == selected }
        val byCategory = categoryList.associateBy { it.id }
        BudgetsUiState(
            loading = false,
            monthLabel = selected.formatMonth(),
            rows = visible.map { budget ->
                val status = BudgetCalculator.assess(budget, items)
                val category = byCategory[budget.categoryId]
                BudgetRow(
                    id = budget.id,
                    category = category?.name ?: "Sin categoría",
                    iconKey = category?.iconKey ?: "other_expense",
                    spent = MoneyFormatter.format(status.spent, prefs.currency, prefs.showCents),
                    limit = MoneyFormatter.format(status.budget.limit, prefs.currency, prefs.showCents),
                    remaining = MoneyFormatter.format(status.remaining, prefs.currency, prefs.showCents),
                    percent = "${status.consumedPercent.toPlainString().replace('.', ',')} %",
                    level = status.level,
                )
            },
            empty = visible.isEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsUiState())

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun delete(id: Long) {
        viewModelScope.launch { budgets.delete(id) }
    }

    fun selectedMonth(): YearMonth = month.value

    private fun YearMonth.formatMonth(): String {
        val label = atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-PE")))
        return label.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es-PE")) else it.toString() }
    }
}

data class BudgetFormUiState(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val editing: Boolean = false,
    val categories: List<Category> = emptyList(),
    val categoryId: Long? = null,
    val amountText: String = "",
    val month: YearMonth = YearMonth.now(),
    val amountError: String? = null,
    val categoryError: String? = null,
    val formError: String? = null,
    val saving: Boolean = false,
)

@HiltViewModel
class BudgetFormViewModel @Inject constructor(
    private val budgets: BudgetRepository,
    private val categories: CategoryRepository,
    private val dates: DateProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val budgetId: Long? = savedStateHandle.get<Long>("id")
    private val _state = MutableStateFlow(BudgetFormUiState(editing = budgetId != null, month = YearMonth.from(dates.today())))
    val state: StateFlow<BudgetFormUiState> = _state.asStateFlow()
    private var existingId: Long = 0

    init {
        viewModelScope.launch {
            val expenseCategories = categories.observeAll().first { it.isNotEmpty() }
                .filter { it.type == TransactionType.Expense }
            val existing = budgetId?.let { budgets.observeById(it).first() }
            if (budgetId != null && existing == null) {
                _state.update { it.copy(loading = false, missing = true, categories = expenseCategories) }
            } else {
                existingId = existing?.id ?: 0
                _state.update {
                    it.copy(
                        loading = false,
                        categories = expenseCategories,
                        categoryId = existing?.categoryId,
                        amountText = existing?.limit?.let { money ->
                            BigDecimal.valueOf(money.minorUnits).movePointLeft(2).toPlainString().replace('.', ',')
                        }.orEmpty(),
                        month = existing?.period ?: it.month,
                    )
                }
            }
        }
    }

    fun onCategory(id: Long) = _state.update { it.copy(categoryId = id, categoryError = null, formError = null) }

    fun onAmount(value: String) = _state.update { it.copy(amountText = value, amountError = null, formError = null) }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (current.saving) return
        val parsed = MoneyParser.parse(current.amountText)
        val amountError = (parsed as? MoneyParseResult.Invalid)?.message
        val categoryError = if (current.categoryId == null) "Elige una categoría" else null
        if (amountError != null || categoryError != null) {
            _state.update { it.copy(amountError = amountError, categoryError = categoryError) }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val result = budgets.save(
                Budget(
                    id = existingId,
                    categoryId = current.categoryId ?: return@launch,
                    period = current.month,
                    limit = (parsed as MoneyParseResult.Valid).money,
                ),
            )
            when (result) {
                BudgetSaveResult.Saved -> onSaved()
                BudgetSaveResult.DuplicatePeriod -> _state.update {
                    it.copy(saving = false, formError = "Ya existe un presupuesto de esa categoría en este mes.")
                }
            }
        }
    }
}
