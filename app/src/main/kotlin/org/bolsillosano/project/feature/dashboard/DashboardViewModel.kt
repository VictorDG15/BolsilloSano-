package org.bolsillosano.project.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.bolsillosano.project.core.common.DateFormatter
import org.bolsillosano.project.core.common.MoneyFormatter
import org.bolsillosano.project.domain.model.Category
import org.bolsillosano.project.domain.model.PeriodPreset
import org.bolsillosano.project.domain.model.Transaction
import org.bolsillosano.project.domain.model.TransactionType
import org.bolsillosano.project.domain.model.UserPreferences
import org.bolsillosano.project.domain.money.FinanceSummary
import org.bolsillosano.project.domain.repository.CategoryRepository
import org.bolsillosano.project.domain.repository.PreferencesRepository
import org.bolsillosano.project.domain.repository.TransactionRepository
import org.bolsillosano.project.domain.time.DateProvider
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class RecentMovement(
    val id: Long,
    val title: String,
    val category: String,
    val iconKey: String,
    val amount: String,
    val income: Boolean,
    val isDemo: Boolean,
)

data class ExpenseShare(
    val name: String,
    val iconKey: String,
    val amount: String,
    val percentLabel: String,
    val fraction: Float,
)

data class DashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val greeting: String = "",
    val dateLabel: String = "",
    val period: PeriodPreset = PeriodPreset.ThisMonth,
    val balance: String = "",
    val income: String = "",
    val expense: String = "",
    val variation: String? = null,
    val shares: List<ExpenseShare> = emptyList(),
    val recent: List<RecentMovement> = emptyList(),
    val showWelcome: Boolean = false,
    val periodEmpty: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactions: TransactionRepository,
    categories: CategoryRepository,
    preferences: PreferencesRepository,
    private val dates: DateProvider,
) : ViewModel() {
    private val period = MutableStateFlow(PeriodPreset.ThisMonth)

    val state: StateFlow<DashboardUiState> = combine(
        transactions.observeAll(),
        categories.observeAll(),
        preferences.observe(),
        period,
    ) { items, categoryList, prefs, preset ->
        toState(items, categoryList, prefs, preset)
    }.catch {
        emit(DashboardUiState(loading = false, error = "No se pudieron leer los movimientos."))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun selectPeriod(preset: PeriodPreset) {
        period.value = preset
    }

    fun loadDemo() {
        viewModelScope.launch { transactions.insertDemoData() }
    }

    private fun toState(
        items: List<Transaction>,
        categories: List<Category>,
        preferences: UserPreferences,
        preset: PeriodPreset,
    ): DashboardUiState {
        val today = dates.today()
        val summary = FinanceSummary.dashboard(items, categories, today, preset)
        val expenseBase = summary.expense.minorUnits.coerceAtLeast(1L)
        val name = preferences.displayName
        val hello = when (dates.hour()) {
            in 5..11 -> "Buenos días"
            in 12..19 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        return DashboardUiState(
            loading = false,
            greeting = if (name.isBlank()) hello else "$hello, $name",
            dateLabel = DateFormatter.short(today),
            period = preset,
            balance = MoneyFormatter.format(summary.balance, preferences.currency, preferences.showCents),
            income = MoneyFormatter.format(summary.income, preferences.currency, preferences.showCents),
            expense = MoneyFormatter.format(summary.expense, preferences.currency, preferences.showCents),
            variation = summary.variationPercent?.let(::variationLabel),
            shares = summary.expenseByCategory.map { total ->
                val fraction = total.amount.minorUnits.toFloat() / expenseBase
                ExpenseShare(
                    name = total.category.name,
                    iconKey = total.category.iconKey,
                    amount = MoneyFormatter.format(total.amount, preferences.currency, preferences.showCents),
                    percentLabel = percent(fraction),
                    fraction = fraction.coerceIn(0f, 1f),
                )
            },
            recent = summary.recent.map { transaction ->
                val category = categories.firstOrNull { it.id == transaction.categoryId }
                RecentMovement(
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
            showWelcome = !summary.hasAnyTransaction,
            periodEmpty = summary.hasAnyTransaction && !summary.hasTransactionsInPeriod,
        )
    }

    private fun variationLabel(value: BigDecimal): String {
        val text = value.abs().toPlainString().replace('.', ',')
        return if (value.signum() >= 0) {
            "Subió $text % frente al periodo anterior"
        } else {
            "Bajó $text % frente al periodo anterior"
        }
    }

    private fun percent(fraction: Float): String {
        val value = BigDecimal.valueOf(fraction.toDouble() * 100).setScale(1, RoundingMode.HALF_UP)
        return "${value.toPlainString().replace('.', ',')} %"
    }
}
