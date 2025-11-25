@file:OptIn(ExperimentalMaterial3Api::class)

package com.finedu.app.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

// Enums para filtros
enum class TransactionFilter {
    ALL, INCOME, EXPENSE
}

enum class TransactionSort {
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC, CATEGORY
}

// =========================
//  Colores adaptativos
// =========================
@Composable
private fun getColorScheme(): AppColors {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        AppColors(
            primary = Color(0xFF66BB6A),
            primaryDark = Color(0xFF4CAF50),
            primaryLight = Color(0xFF81C784),
            expense = Color(0xFFFF8A65),
            textPrimary = Color(0xFFE8EAED),
            textSecondary = Color(0xFFB0B8C1),
            textTertiary = Color(0xFF8A9199),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2A2A2A),
            topBarStart = Color(0xFF263238),
            topBarEnd = Color(0xFF37474F)
        )
    } else {
        AppColors(
            primary = Color(0xFF4CAF50),
            primaryDark = Color(0xFF388E3C),
            primaryLight = Color(0xFF81C784),
            expense = Color(0xFFFF7043),
            textPrimary = Color(0xFF1A2332),
            textSecondary = Color(0xFF3A4F66),
            textTertiary = Color(0xFF94A3B8),
            background = Color(0xFFF5F7FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFAFAFA),
            topBarStart = Color(0xFF2C3E50),
            topBarEnd = Color(0xFF4A5568)
        )
    }
}

data class AppColors(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val expense: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val topBarStart: Color,
    val topBarEnd: Color
)

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
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        letterSpacing = (-1).sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gestiona tus finanzas",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 14.sp
                    )
                )
            }

            listOf(
                Triple(Icons.Outlined.Refresh, "Refrescar", onRefreshClick),
                Triple(Icons.Outlined.Notifications, "Notificaciones", onNotificationClick),
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

// DIÁLOGO: Capital No Configurado
@Composable
fun CapitalRequiredDialog(
    colors: AppColors,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFF9800)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Capital No Configurado",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Para poder registrar transacciones, primero debes configurar tu capital inicial en tu perfil de usuario.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Ve a tu perfil y configura tu capital inicial antes de comenzar a registrar ingresos o gastos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text(
                        "Entendido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
    var showTrends by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var currentSort by remember { mutableStateOf(TransactionSort.DATE_DESC) }
    var searchQuery by remember { mutableStateOf("") }
    var showCapitalDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) } // ✅ NUEVO

    val hasCapital = state.capitalAmount > 0.0

    val filteredAndSortedTransactions = remember(
        state.transactions,
        currentFilter,
        currentSort,
        searchQuery,
        selectedCategory // ✅ NUEVO
    ) {
        var transactions = when (currentFilter) {
            TransactionFilter.ALL -> state.transactions
            TransactionFilter.INCOME -> state.transactions.filter { it.type == "income" }
            TransactionFilter.EXPENSE -> state.transactions.filter { it.type == "expense" }
        }

        // ✅ NUEVO: Filtrar por categoría seleccionada
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
        item {
            SaludFinancieraCard(
                colors,
                state.capitalAmount.toCurrencyString(),
                state.totalEgresos.toCurrencyString()
            )
        }

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

        // FILTROS Y BÚSQUEDA
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
                        // Limpiar categoría cuando se cambia el filtro principal
                        selectedCategory = null
                    },
                    onSortChange = { currentSort = it }
                )

                // ✅ NUEVO: Chips de categorías dinámicas
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

        item {
            ActivityCardContainer(
                colors,
                state.copy(transactions = filteredAndSortedTransactions),
                onDeleteTransaction = onDeleteTransaction
            )
        }
    }
}

// BARRA DE BÚSQUEDA
@Composable
fun SearchBar(
    colors: AppColors,
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                "Buscar por categoría, descripción o monto...",
                color = colors.textTertiary,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Buscar",
                tint = colors.textTertiary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Limpiar",
                        tint = colors.textTertiary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = colors.textTertiary.copy(alpha = 0.2f),
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.primary
        )
    )
}

// FILTROS Y ORDENAMIENTO
@Composable
fun FilterAndSortRow(
    colors: AppColors,
    currentFilter: TransactionFilter,
    currentSort: TransactionSort,
    onFilterChange: (TransactionFilter) -> Unit,
    onSortChange: (TransactionSort) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    colors = colors,
                    label = "Todas",
                    icon = Icons.Outlined.List,
                    isSelected = currentFilter == TransactionFilter.ALL,
                    onClick = { onFilterChange(TransactionFilter.ALL) }
                )
            }
            item {
                FilterChip(
                    colors = colors,
                    label = "Ingresos",
                    icon = Icons.Outlined.ArrowUpward,
                    isSelected = currentFilter == TransactionFilter.INCOME,
                    onClick = { onFilterChange(TransactionFilter.INCOME) },
                    chipColor = colors.primary
                )
            }
            item {
                FilterChip(
                    colors = colors,
                    label = "Egresos",
                    icon = Icons.Outlined.ArrowDownward,
                    isSelected = currentFilter == TransactionFilter.EXPENSE,
                    onClick = { onFilterChange(TransactionFilter.EXPENSE) },
                    chipColor = colors.expense
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ordenar por:",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colors.primary.copy(alpha = 0.05f),
                        contentColor = colors.primary
                    )
                ) {
                    Text(
                        text = when (currentSort) {
                            TransactionSort.DATE_DESC -> "Más recientes"
                            TransactionSort.DATE_ASC -> "Más antiguos"
                            TransactionSort.AMOUNT_DESC -> "Mayor monto"
                            TransactionSort.AMOUNT_ASC -> "Menor monto"
                            TransactionSort.CATEGORY -> "Categoría"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    SortMenuItem(
                        colors,
                        "Más recientes",
                        Icons.Outlined.ArrowDownward,
                        currentSort == TransactionSort.DATE_DESC
                    ) {
                        onSortChange(TransactionSort.DATE_DESC)
                        showSortMenu = false
                    }
                    SortMenuItem(
                        colors,
                        "Más antiguos",
                        Icons.Outlined.ArrowUpward,
                        currentSort == TransactionSort.DATE_ASC
                    ) {
                        onSortChange(TransactionSort.DATE_ASC)
                        showSortMenu = false
                    }
                    SortMenuItem(
                        colors,
                        "Mayor monto",
                        Icons.Outlined.TrendingUp,
                        currentSort == TransactionSort.AMOUNT_DESC
                    ) {
                        onSortChange(TransactionSort.AMOUNT_DESC)
                        showSortMenu = false
                    }
                    SortMenuItem(
                        colors,
                        "Menor monto",
                        Icons.Outlined.TrendingDown,
                        currentSort == TransactionSort.AMOUNT_ASC
                    ) {
                        onSortChange(TransactionSort.AMOUNT_ASC)
                        showSortMenu = false
                    }
                    SortMenuItem(
                        colors,
                        "Categoría",
                        Icons.Outlined.Category,
                        currentSort == TransactionSort.CATEGORY
                    ) {
                        onSortChange(TransactionSort.CATEGORY)
                        showSortMenu = false
                    }
                }
            }
        }
    }
}

@Composable
fun SortMenuItem(
    colors: AppColors,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) colors.primary else colors.textSecondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    label,
                    color = if (isSelected) colors.primary else colors.textPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        onClick = onClick
    )
}

@Composable
fun FilterChip(
    colors: AppColors,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    chipColor: Color = colors.primary
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (isSelected) chipColor.copy(alpha = 0.15f) else colors.surfaceVariant,
            labelColor = if (isSelected) chipColor else colors.textSecondary,
            iconColor = if (isSelected) chipColor else colors.textSecondary,
            selectedContainerColor = chipColor.copy(alpha = 0.15f),
            selectedLabelColor = chipColor,
            selectedLeadingIconColor = chipColor
        ),
        border = if (isSelected) {
            BorderStroke(1.5.dp, chipColor.copy(alpha = 0.5f))
        } else {
            BorderStroke(1.dp, colors.textTertiary.copy(alpha = 0.2f))
        }
    )
}

// ✅ NUEVO COMPONENTE: Fila de filtros por categoría
@Composable
fun CategoryFilterRow(
    colors: AppColors,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtrar por categoría:",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            if (selectedCategory != null) {
                TextButton(
                    onClick = { onCategorySelected(selectedCategory) },
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Limpiar filtro",
                        modifier = Modifier.size(16.dp),
                        tint = colors.textTertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Limpiar",
                        fontSize = 13.sp,
                        color = colors.textTertiary
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                val isSelected = selectedCategory == category

                // Determinar icono según categoría
                val categoryIcon = when (category.lowercase()) {
                    "comida", "alimentos" -> Icons.Outlined.Restaurant
                    "transporte" -> Icons.Outlined.DirectionsCar
                    "entretenimiento" -> Icons.Outlined.Theaters
                    "salud" -> Icons.Outlined.LocalHospital
                    "educación" -> Icons.Outlined.School
                    "ropa" -> Icons.Outlined.Checkroom
                    "servicios" -> Icons.Outlined.Build
                    "hogar" -> Icons.Outlined.Home
                    else -> Icons.Outlined.Category
                }

                FilterChip(
                    colors = colors,
                    label = category,
                    icon = categoryIcon,
                    isSelected = isSelected,
                    onClick = { onCategorySelected(category) },
                    chipColor = Color(0xFF9C27B0) // Color morado para categorías
                )
            }
        }
    }
}