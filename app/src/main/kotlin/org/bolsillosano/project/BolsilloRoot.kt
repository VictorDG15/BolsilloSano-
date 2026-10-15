package org.bolsillosano.project

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.bolsillosano.project.core.designsystem.theme.BolsilloSanoTheme
import org.bolsillosano.project.core.navigation.Routes
import org.bolsillosano.project.domain.model.ThemeMode
import org.bolsillosano.project.feature.budgets.BudgetFormScreen
import org.bolsillosano.project.feature.budgets.BudgetsScreen
import org.bolsillosano.project.feature.dashboard.DashboardScreen
import org.bolsillosano.project.feature.profile.AppPreferencesViewModel
import org.bolsillosano.project.feature.profile.ProfileScreen
import org.bolsillosano.project.feature.statistics.StatisticsScreen
import org.bolsillosano.project.feature.transactiondetail.TransactionDetailScreen
import org.bolsillosano.project.feature.transactions.TransactionFormScreen
import org.bolsillosano.project.feature.transactions.TransactionsScreen

private data class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val topDestinations = listOf(
    TopDestination(Routes.Dashboard, "Inicio", Icons.Outlined.Home),
    TopDestination(Routes.Transactions, "Movimientos", Icons.AutoMirrored.Outlined.ReceiptLong),
    TopDestination(Routes.Statistics, "Estadísticas", Icons.Outlined.PieChart),
    TopDestination(Routes.Budgets, "Presupuestos", Icons.Outlined.AccountBalanceWallet),
    TopDestination(Routes.Profile, "Perfil", Icons.Outlined.Person),
)

@Composable
fun BolsilloRoot(viewModel: AppPreferencesViewModel = hiltViewModel()) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val darkTheme = when (preferences.themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    BolsilloSanoTheme(darkTheme = darkTheme) {
        BolsilloNavHost()
    }
}

@Composable
private fun BolsilloNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(backStack) {
        val message = backStack?.savedStateHandle?.remove<String>(Routes.Flash) ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute in topDestinations.map { it.route }) {
                NavigationBar {
                    topDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Dashboard,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Dashboard) {
                DashboardScreen(
                    onCreate = { type -> navController.navigate(Routes.createTransaction(type)) },
                    onOpenTransaction = { id -> navController.navigate(Routes.transactionDetail(id)) },
                    onOpenTransactions = {
                        navController.navigate(Routes.Transactions) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Routes.Transactions) {
                TransactionsScreen(
                    onCreate = { navController.navigate(Routes.createTransaction("expense")) },
                    onOpen = { id -> navController.navigate(Routes.transactionDetail(id)) },
                )
            }
            composable(Routes.Statistics) { StatisticsScreen() }
            composable(Routes.Budgets) {
                BudgetsScreen(
                    onCreate = { navController.navigate(Routes.CreateBudget) },
                    onEdit = { id -> navController.navigate(Routes.editBudget(id)) },
                )
            }
            composable(Routes.Profile) { ProfileScreen() }
            composable(
                route = Routes.CreateTransaction,
                arguments = listOf(navArgument("type") { type = NavType.StringType; defaultValue = "expense" }),
            ) {
                TransactionFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(Routes.Flash, "Movimiento guardado")
                        navController.popBackStack()
                    },
                )
            }
            composable(
                route = Routes.EditTransaction,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) {
                TransactionFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(Routes.Flash, "Movimiento actualizado")
                        navController.popBackStack()
                    },
                )
            }
            composable(
                route = Routes.TransactionDetail,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                TransactionDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editTransaction(id)) },
                    onDeleted = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(Routes.Flash, "Movimiento eliminado")
                        navController.popBackStack()
                    },
                )
            }
            composable(Routes.CreateBudget) {
                BudgetFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(Routes.Flash, "Presupuesto guardado")
                        navController.popBackStack()
                    },
                )
            }
            composable(
                route = Routes.EditBudget,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) {
                BudgetFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(Routes.Flash, "Presupuesto actualizado")
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
