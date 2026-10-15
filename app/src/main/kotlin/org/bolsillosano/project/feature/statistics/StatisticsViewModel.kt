package org.bolsillosano.project.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.bolsillosano.project.core.common.MoneyFormatter
import org.bolsillosano.project.core.designsystem.component.ShareBarItem
import org.bolsillosano.project.domain.model.PeriodPreset
import org.bolsillosano.project.domain.money.FinanceSummary
import org.bolsillosano.project.domain.repository.CategoryRepository
import org.bolsillosano.project.domain.repository.PreferencesRepository
import org.bolsillosano.project.domain.repository.TransactionRepository
import org.bolsillosano.project.domain.time.DateProvider
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class WeekBar(
    val label: String,
    val income: String,
    val expense: String,
    val incomeFraction: Float,
    val expenseFraction: Float,
)

data class StatisticsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val period: PeriodPreset = PeriodPreset.ThisMonth,
    val income: String = "",
    val expense: String = "",
    val incomeFraction: Float = 0f,
    val expenseFraction: Float = 0f,
    val shares: List<ShareBarItem> = emptyList(),
    val weeks: List<WeekBar> = emptyList(),
    val empty: Boolean = false,
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    transactions: TransactionRepository,
    categories: CategoryRepository,
    preferences: PreferencesRepository,
    private val dates: DateProvider,
) : ViewModel() {
    private val period = MutableStateFlow(PeriodPreset.ThisMonth)

    val state: StateFlow<StatisticsUiState> = combine(
        transactions.observeAll(),
        categories.observeAll(),
        preferences.observe(),
        period,
    ) { items, categoryList, prefs, preset ->
        val today = dates.today()
        val summary = FinanceSummary.dashboard(items, categoryList, today, preset)
        val maxSide = maxOf(summary.income.minorUnits, summary.expense.minorUnits).coerceAtLeast(1L)
        val expenseBase = summary.expense.minorUnits.coerceAtLeast(1L)
        val weeks = FinanceSummary.weekly(items, today, preset)
        val weekMax = weeks.maxOfOrNull { maxOf(it.income.minorUnits, it.expense.minorUnits) }?.coerceAtLeast(1L) ?: 1L
        StatisticsUiState(
            loading = false,
            period = preset,
            income = MoneyFormatter.format(summary.income, prefs.currency, prefs.showCents),
            expense = MoneyFormatter.format(summary.expense, prefs.currency, prefs.showCents),
            incomeFraction = summary.income.minorUnits.toFloat() / maxSide,
            expenseFraction = summary.expense.minorUnits.toFloat() / maxSide,
            shares = summary.expenseByCategory.map { total ->
                val fraction = total.amount.minorUnits.toFloat() / expenseBase
                val percent = BigDecimal.valueOf(fraction.toDouble() * 100).setScale(1, RoundingMode.HALF_UP)
                ShareBarItem(
                    name = total.category.name,
                    amount = MoneyFormatter.format(total.amount, prefs.currency, prefs.showCents),
                    percentLabel = "${percent.toPlainString().replace('.', ',')} %",
                    fraction = fraction.coerceIn(0f, 1f),
                )
            },
            weeks = weeks.map { week ->
                WeekBar(
                    label = "Semana del ${week.label}",
                    income = MoneyFormatter.format(week.income, prefs.currency, prefs.showCents),
                    expense = MoneyFormatter.format(week.expense, prefs.currency, prefs.showCents),
                    incomeFraction = week.income.minorUnits.toFloat() / weekMax,
                    expenseFraction = week.expense.minorUnits.toFloat() / weekMax,
                )
            },
            empty = !summary.hasTransactionsInPeriod,
        )
    }.catch {
        emit(StatisticsUiState(loading = false, error = "No se pudieron calcular las estadísticas."))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    fun selectPeriod(preset: PeriodPreset) {
        period.value = preset
    }
}
