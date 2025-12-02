@file:OptIn(ExperimentalMaterial3Api::class)

package com.finedu.app.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finedu.app.navigation.AppRutas
import com.finedu.app.ui.dashboard.MainDashboardState
import com.finedu.app.ui.dashboard.MainDashboardViewModel
import com.finedu.app.ui.dictation.VoiceDictationViewModel
import com.finedu.app.ui.dictation.VoiceDictationScreen
import com.finedu.app.ui.theme.SetStatusBarIcons
import com.finedu.app.network.SessionExpiredManager
import kotlinx.coroutines.launch
import com.datadog.android.rum.GlobalRumMonitor   // 👈 Datadog RUM para medir tiempo en Home

sealed class MainScreenDestinations(val route: String) {
    object Home : MainScreenDestinations("home_tab")
    object Profile : MainScreenDestinations("profile_tab")
    object Dictation : MainScreenDestinations("voice_dictation")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(mainNavController: NavController, onLogoutClick: () -> Unit) {
    val internalNavController = rememberNavController()
    val viewModel: MainDashboardViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = getColorScheme()

    SetStatusBarIcons(useDarkIcons = !isSystemInDarkTheme())

    DisposableEffect(Unit) {
        SessionExpiredManager.setListener {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.",
                    duration = SnackbarDuration.Short
                )
                kotlinx.coroutines.delay(1500)
                onLogoutClick()
            }
        }
        onDispose {
            SessionExpiredManager.clearListener()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadDashboardDataForThisMonth() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        topBar = {
            MainTopBar(
                colors = colors,
                onNotificationClick = { mainNavController.navigate(AppRutas.NOTIFICATIONS_SCREEN) },
                onProfileClick = { mainNavController.navigate(AppRutas.PROFILE_SCREEN) },
                onRefreshClick = { viewModel.loadDashboardDataForThisMonth() }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = internalNavController,
            startDestination = MainScreenDestinations.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MainScreenDestinations.Home.route) {
                val state by viewModel.state.collectAsState()
                val backStackEntry = internalNavController.currentBackStackEntry

                HandleBackStackActions(backStackEntry, viewModel, snackbarHostState, scope)

                HomeScreenDashboard(
                    colors = colors,
                    onAddTransactionClick = {
                        internalNavController.navigate(MainScreenDestinations.Dictation.route)
                    },
                    state = state,
                    onDeleteTransaction = { txId ->
                        scope.launch {
                            viewModel.deleteTransaction(txId)
                        }
                    },
                    onNavigateToProfile = { mainNavController.navigate(AppRutas.PROFILE_SCREEN) }
                )
            }

            composable(MainScreenDestinations.Dictation.route) {
                val dictationViewModel: VoiceDictationViewModel = hiltViewModel()
                VoiceDictationScreen(
                    navController = internalNavController,
                    viewModel = dictationViewModel
                )
            }

            composable(MainScreenDestinations.Profile.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Placeholder de Perfil Interno")
                }
            }
        }
    }
}

@Composable
private fun HandleBackStackActions(
    backStackEntry: androidx.navigation.NavBackStackEntry?,
    viewModel: MainDashboardViewModel,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val refreshResult by backStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh_transactions")
        ?.observeAsState() ?: remember { mutableStateOf(null) }

    val successMessage by backStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("show_success_snackbar")
        ?.observeAsState() ?: remember { mutableStateOf(null) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(refreshResult) {
        if (refreshResult == true) {
            viewModel.loadDashboardDataForThisMonth()
            backStackEntry?.savedStateHandle?.set("refresh_transactions", false)
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            backStackEntry?.savedStateHandle?.set("show_success_snackbar", null)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    it,
                    duration = SnackbarDuration.Long,
                    withDismissAction = true
                )
            }
            viewModel.clearError()
        }
    }
}

@Composable
fun MainTopBar(
    colors: AppColors,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    // 🔹 Tamaño de fuente fijo que NO escala con la configuración del sistema
    val fixedTitleSize = with(LocalDensity.current) {
        (38f / fontScale).sp
    }
    val fixedSubtitleSize = with(LocalDensity.current) {
        (14f / fontScale).sp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(149.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(colors.topBarStart, colors.topBarEnd)
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bienvenido",
                    fontSize = fixedTitleSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gestiona tus finanzas",
                    fontSize = fixedSubtitleSize,
                    color = Color.White.copy(alpha = 0.90f),
                    maxLines = 1
                )
            }

            listOf(
                Triple(Icons.Outlined.Refresh, "Refrescar", onRefreshClick),
                Triple(Icons.Outlined.Person, "Perfil", onProfileClick)
            ).forEach { (icon, desc, onClick) ->
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.background(
                        Color.White.copy(alpha = 0.20f),
                        CircleShape
                    )
                ) {
                    Icon(icon, desc, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenDashboard(
    colors: AppColors,
    onAddTransactionClick: () -> Unit,
    state: MainDashboardState,
    onDeleteTransaction: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // 🔹 Datadog RUM: medir tiempo que el usuario pasa en Home
    DisposableEffect(Unit) {
        val viewKey = "home_dashboard_view"
        val viewName = "Home - Dashboard"

        GlobalRumMonitor.get().startView(
            viewKey,   // key
            viewName   // nombre visible en Datadog
        )

        onDispose {
            GlobalRumMonitor.get().stopView(viewKey)
        }
    }

    var showTrends by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var currentSort by remember { mutableStateOf(TransactionSort.DATE_DESC) }
    var searchQuery by remember { mutableStateOf("") }
    var showCapitalDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val hasCapital = state.capitalAmount > 0.0

    val filteredAndSortedTransactions = remember(
        state.transactions,
        currentFilter,
        currentSort,
        searchQuery,
        selectedCategory
    ) {
        var transactions = when (currentFilter) {
            TransactionFilter.ALL -> state.transactions
            TransactionFilter.INCOME -> state.transactions.filter { it.type == "income" }
            TransactionFilter.EXPENSE -> state.transactions.filter { it.type == "expense" }
        }

        if (selectedCategory != null) {
            transactions = transactions.filter { it.category == selectedCategory }
        }

        if (searchQuery.isNotBlank()) {
            transactions = transactions.filter { tx ->
                tx.category.contains(searchQuery, ignoreCase = true) ||
                        tx.description?.contains(searchQuery, ignoreCase = true) == true ||
                        tx.amount.toString().contains(searchQuery)
            }
        }

        transactions = when (currentSort) {
            TransactionSort.DATE_DESC -> transactions.sortedByDescending { it.date }
            TransactionSort.DATE_ASC -> transactions.sortedBy { it.date }
            TransactionSort.AMOUNT_DESC -> transactions.sortedByDescending { it.amount }
            TransactionSort.AMOUNT_ASC -> transactions.sortedBy { it.amount }
            TransactionSort.CATEGORY -> transactions.sortedBy { it.category }
        }

        transactions
    }

    if (showCapitalDialog) {
        CapitalRequiredDialog(
            colors = colors,
            onDismiss = { showCapitalDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de Salud Financiera
        item {
            SaludFinancieraCard(
                colors,
                state.capitalAmount.toCurrencyString(),
                state.totalEgresos.toCurrencyString()
            )
        }

        // Botón para agregar transacción
        item {
            AddTransactionCard(
                colors = colors,
                onClick = {
                    if (hasCapital) {
                        onAddTransactionClick()
                    } else {
                        showCapitalDialog = true
                    }
                }
            )
        }

        // Título de Actividad Reciente
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Actividad Reciente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    "${filteredAndSortedTransactions.size} transacciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Filtros y búsqueda
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchBar(
                    colors = colors,
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = { searchQuery = "" }
                )

                FilterAndSortRow(
                    colors = colors,
                    currentFilter = currentFilter,
                    currentSort = currentSort,
                    onFilterChange = {
                        currentFilter = it
                        selectedCategory = null
                    },
                    onSortChange = { currentSort = it }
                )

                if (state.availableCategories.isNotEmpty()) {
                    CategoryFilterRow(
                        colors = colors,
                        categories = state.availableCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            selectedCategory = if (selectedCategory == category) null else category
                        }
                    )
                }
            }
        }

        // Lista de transacciones
        item {
            ActivityCardContainer(
                colors,
                state.copy(transactions = filteredAndSortedTransactions),
                onDeleteTransaction = onDeleteTransaction
            )
        }

        // Gráficas al final
        item {
            if (showTrends) {
                TendenciasCard(colors, state.transactions)
            } else {
                EstadisticasCard(
                    colors = colors,
                    ingresos = state.capitalAmount,
                    egresos = state.totalEgresos,
                    transactions = state.transactions
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ToggleViewButton(colors, showTrends) { showTrends = !showTrends }
            }
        }
    }
}