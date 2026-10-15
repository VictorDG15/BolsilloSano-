package org.bolsillosano.project.core.navigation

object Routes {
    const val Dashboard = "dashboard"
    const val Transactions = "transactions"
    const val Statistics = "statistics"
    const val Budgets = "budgets"
    const val Profile = "profile"
    const val CreateTransaction = "transactions/create?type={type}"
    const val TransactionDetail = "transactions/{id}"
    const val EditTransaction = "transactions/{id}/edit"
    const val CreateBudget = "budgets/create"
    const val EditBudget = "budgets/{id}/edit"
    const val Flash = "flash_message"

    fun createTransaction(type: String): String = "transactions/create?type=$type"

    fun transactionDetail(id: Long): String = "transactions/$id"

    fun editTransaction(id: Long): String = "transactions/$id/edit"

    fun editBudget(id: Long): String = "budgets/$id/edit"
}
