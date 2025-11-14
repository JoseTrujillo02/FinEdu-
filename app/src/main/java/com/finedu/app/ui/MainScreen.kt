@file:OptIn(ExperimentalMaterial3Api::class)

package com.finedu.app.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finedu.app.auth.data.TransactionItem
import com.finedu.app.navigation.AppRutas
import com.finedu.app.ui.dashboard.MainDashboardState
import com.finedu.app.ui.dashboard.MainDashboardViewModel
import com.finedu.app.ui.dictation.VoiceDictationViewModel
import com.finedu.app.ui.theme.SetStatusBarIcons
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.finedu.app.network.SessionExpiredManager


// Enums para filtros
enum class TransactionFilter {
    ALL, INCOME, EXPENSE
}

enum class TransactionSort {
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC, CATEGORY
}

// Colores adaptativos para tema claro y oscuro
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
                    onAddTransactionClick = { internalNavController.navigate(MainScreenDestinations.Dictation.route) },
                    state = state,
                    onDeleteTransaction = { scope.launch { snackbarHostState.showSnackbar("Funcionalidad próximamente") } },
                    onNavigateToProfile = { mainNavController.navigate(AppRutas.PROFILE_SCREEN) }
                )
            }

            composable(MainScreenDestinations.Dictation.route) {
                val dictationViewModel: VoiceDictationViewModel = hiltViewModel()
                VoiceDictationScreen(navController = internalNavController, viewModel = dictationViewModel)
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
    val refreshResult by backStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh_transactions")?.observeAsState() ?: remember { mutableStateOf(null) }
    val successMessage by backStackEntry?.savedStateHandle?.getLiveData<String>("show_success_snackbar")?.observeAsState() ?: remember { mutableStateOf(null) }
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
            scope.launch { snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long, withDismissAction = true) }
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
            .background(Brush.horizontalGradient(listOf(colors.topBarStart, colors.topBarEnd)))
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
                    modifier = Modifier.background(Color.White.copy(alpha = 0.20f), CircleShape)
                ) {
                    Icon(icon, desc, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono informativo
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
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Para poder registrar transacciones, primero debes configurar tu capital inicial en tu perfil de usuario.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4A5568),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card con información adicional
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
                            "Ve a tu perfil y configura tu capital inicial antes de comenzar a registrar ingresos o gastos. Puedes establecerlo por periodo mensual o el periodo que prefieras.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2C3E50),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para cerrar
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
    onDeleteTransaction: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var showTrends by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var currentSort by remember { mutableStateOf(TransactionSort.DATE_DESC) }
    var searchQuery by remember { mutableStateOf("") }
    var showCapitalDialog by remember { mutableStateOf(false) }

    // Verificar si hay capital configurado
    val hasCapital = state.capitalAmount > 0.0

    // Aplicar filtros, búsqueda y ordenamiento
    val filteredAndSortedTransactions = remember(state.transactions, currentFilter, currentSort, searchQuery) {
        var transactions = when (currentFilter) {
            TransactionFilter.ALL -> state.transactions
            TransactionFilter.INCOME -> state.transactions.filter { it.type == "income" }
            TransactionFilter.EXPENSE -> state.transactions.filter { it.type == "expense" }
        }

        // Aplicar búsqueda
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

    // Mostrar dialog de capital requerido
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
        item { SaludFinancieraCard(colors, state.capitalAmount.toCurrencyString(), state.totalEgresos.toCurrencyString()) }
        item {
            if (showTrends) {
                TendenciasCard(colors, state.transactions)
            } else {
                EstadisticasCard(colors, state.capitalAmount, state.totalEgresos)
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
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Actividad Reciente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text("${filteredAndSortedTransactions.size} transacciones", style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary, fontWeight = FontWeight.Medium)
            }
        }

        // Filtros, búsqueda y ordenamiento
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Barra de búsqueda
                SearchBar(
                    colors = colors,
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = { searchQuery = "" }
                )

                // Filtros y ordenamiento
                FilterAndSortRow(
                    colors = colors,
                    currentFilter = currentFilter,
                    currentSort = currentSort,
                    onFilterChange = { currentFilter = it },
                    onSortChange = { currentSort = it }
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                when {
                    state.isLoading -> LoadingState(colors)
                    filteredAndSortedTransactions.isEmpty() -> EmptyState(colors, currentFilter)
                    else -> TransactionsList(colors, filteredAndSortedTransactions, onDeleteTransaction)
                }
            }
        }
    }
}

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
        // Filtros
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

        // Ordenamiento
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
                    SortMenuItem(colors, "Más recientes", Icons.Outlined.ArrowDownward, currentSort == TransactionSort.DATE_DESC) {
                        onSortChange(TransactionSort.DATE_DESC)
                        showSortMenu = false
                    }
                    SortMenuItem(colors, "Más antiguos", Icons.Outlined.ArrowUpward, currentSort == TransactionSort.DATE_ASC) {
                        onSortChange(TransactionSort.DATE_ASC)
                        showSortMenu = false
                    }
                    SortMenuItem(colors, "Mayor monto", Icons.Outlined.TrendingUp, currentSort == TransactionSort.AMOUNT_DESC) {
                        onSortChange(TransactionSort.AMOUNT_DESC)
                        showSortMenu = false
                    }
                    SortMenuItem(colors, "Menor monto", Icons.Outlined.TrendingDown, currentSort == TransactionSort.AMOUNT_ASC) {
                        onSortChange(TransactionSort.AMOUNT_ASC)
                        showSortMenu = false
                    }
                    SortMenuItem(colors, "Categoría", Icons.Outlined.Category, currentSort == TransactionSort.CATEGORY) {
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

@Composable
private fun LoadingState(colors: AppColors) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.primary)
    }
}

@Composable
private fun EmptyState(colors: AppColors, filter: TransactionFilter = TransactionFilter.ALL) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Outlined.Receipt, null, modifier = Modifier.size(64.dp), tint = colors.textTertiary.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                when (filter) {
                    TransactionFilter.ALL -> "No hay transacciones"
                    TransactionFilter.INCOME -> "No hay ingresos"
                    TransactionFilter.EXPENSE -> "No hay egresos"
                },
                color = colors.textSecondary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                when (filter) {
                    TransactionFilter.ALL -> "Registra tu primera transacción usando el botón de arriba"
                    else -> "No se encontraron transacciones con este filtro"
                },
                color = colors.textTertiary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TransactionsList(colors: AppColors, transactions: List<TransactionItem>, onDelete: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transactions) { tx -> TransactionItemRow(colors, tx, onDelete) }
    }
}

@Composable
fun SaludFinancieraCard(colors: AppColors, ingresos: String, egresos: String) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(colors.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.TrendingUp, null, tint = colors.primary, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Tu Salud Financiera", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FinancialItem(colors, Icons.Outlined.ArrowUpward, "Ingresos", ingresos, colors.primary, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                FinancialItem(colors, Icons.Outlined.ArrowDownward, "Egresos", egresos, colors.expense, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun EstadisticasCard(colors: AppColors, ingresos: Double, egresos: Double) {
    val total = ingresos + egresos
    val ingresosPercent = if (total > 0) (ingresos / total).toFloat() else 0.5f
    val egresosPercent = if (total > 0) (egresos / total).toFloat() else 0.5f
    val balance = ingresos - egresos

    val animIngreso = remember { Animatable(0f) }
    val animEgreso = remember { Animatable(0f) }

    LaunchedEffect(ingresos, egresos) {
        animIngreso.snapTo(0f)
        animEgreso.snapTo(0f)
        animIngreso.animateTo(ingresosPercent, tween(1000, easing = FastOutSlowInEasing))
        animEgreso.animateTo(egresosPercent, tween(1000, 200, FastOutSlowInEasing))
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Balance del Mes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(balance.toCurrencyString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = if (balance >= 0) colors.primary else colors.expense)
                }

                val balanceColor = if (balance >= 0) colors.primary else colors.expense
                Box(
                    modifier = Modifier.size(56.dp).background(balanceColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (balance >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown, null, tint = balanceColor, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                ChartBar(
                    colors = colors,
                    amount = ingresos.toCurrencyString(),
                    height = animIngreso.value,
                    color = colors.primary,
                    icon = Icons.Outlined.ArrowUpward,
                    label = "Ingresos",
                    gradient = listOf(colors.primaryLight, colors.primary, colors.primaryDark)
                )
                Spacer(modifier = Modifier.width(24.dp))
                ChartBar(
                    colors = colors,
                    amount = egresos.toCurrencyString(),
                    height = animEgreso.value,
                    color = colors.expense,
                    icon = Icons.Outlined.ArrowDownward,
                    label = "Egresos",
                    gradient = listOf(colors.expense.copy(alpha = 0.7f), colors.expense, colors.expense.copy(red = colors.expense.red * 0.8f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.textTertiary.copy(alpha = 0.2f)))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PercentageItem(colors, (ingresosPercent * 100).toInt(), colors.primary)
                PercentageItem(colors, (egresosPercent * 100).toInt(), colors.expense)
            }
        }
    }
}

@Composable
private fun RowScope.ChartBar(
    colors: AppColors,
    amount: String,
    height: Float,
    color: Color,
    icon: ImageVector,
    label: String,
    gradient: List<Color>
) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(amount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.width(80.dp).fillMaxHeight(0.85f), contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(height.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Brush.verticalGradient(gradient))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
        }
    }
}

@Composable
private fun RowScope.PercentageItem(colors: AppColors, percent: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text("$percent%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text("del total", fontSize = 12.sp, color = colors.textTertiary)
    }
}

@Composable
fun FinancialItem(colors: AppColors, icon: ImageVector, label: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AddTransactionCard(colors: AppColors, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val scale by infiniteTransition.animateFloat(1f, 1.15f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
    val glowAlpha by infiniteTransition.animateFloat(0.4f, 0.85f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primary)
    ) {
        Row(modifier = Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(90.dp).scale(scale).background(Color.White.copy(alpha = glowAlpha * 0.25f), CircleShape))
                Box(modifier = Modifier.size(75.dp).scale(scale * 0.95f).background(Color.White.copy(alpha = glowAlpha * 0.35f), CircleShape))
                Box(
                    modifier = Modifier.size(60.dp).background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.2f))), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Mic, "Grabar", tint = Color.White, modifier = Modifier.size(32.dp).scale(scale * 1.05f))
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Registrar transacción", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 0.3.sp)
                Spacer(modifier = Modifier.height(5.dp))
                Text("Usa tu voz para agregar", color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp)
            }

            Icon(Icons.Outlined.ArrowForward, null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
fun ToggleViewButton(colors: AppColors, showTrends: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, colors.primary.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.primary.copy(alpha = 0.08f),
            contentColor = colors.primary
        )
    ) {
        Icon(
            if (showTrends) Icons.Outlined.BarChart else Icons.Outlined.Timeline,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (showTrends) "Ver Gráfica" else "Ver Tendencias",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Outlined.SwapHoriz,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colors.primary.copy(alpha = 0.7f)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TendenciasCard(colors: AppColors, transactions: List<TransactionItem>) {
    val categoryTotals = transactions
        .filter { it.type == "expense" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val totalExpenses = categoryTotals.sumOf { it.second }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(colors.expense.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Timeline, null, tint = colors.expense, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Tendencias de Gastos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Top 5 categorías", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (categoryTotals.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.PieChart, null, modifier = Modifier.size(48.dp), tint = colors.textTertiary.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No hay datos de gastos", color = colors.textSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                categoryTotals.forEach { (category, amount) ->
                    val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100).toInt() else 0
                    TrendItem(colors, category, amount, percentage)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun TrendItem(colors: AppColors, category: String, amount: Double, percentage: Int) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = percentage / 100f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    val categoryIcon = when (category.lowercase()) {
        "comida", "alimentos" -> Icons.Outlined.Restaurant
        "transporte" -> Icons.Outlined.DirectionsCar
        "entretenimiento" -> Icons.Outlined.Theaters
        "salud" -> Icons.Outlined.LocalHospital
        "educación" -> Icons.Outlined.School
        else -> Icons.Outlined.ShoppingBag
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(36.dp).background(colors.expense.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoryIcon, null, tint = colors.expense, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(category, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = colors.textPrimary)
                    Text("$percentage% del total", fontSize = 12.sp, color = colors.textTertiary)
                }
            }
            Text(
                amount.toCurrencyString(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.expense
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.expense.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                colors.expense.copy(alpha = 0.7f),
                                colors.expense
                            )
                        )
                    )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionItemRow(colors: AppColors, tx: TransactionItem, onDelete: () -> Unit) {
    val isExpense = tx.type == "expense"
    val iconColor = if (isExpense) colors.expense else colors.primary
    val amountPrefix = if (isExpense) "-" else "+"
    val categoryIcon = when {
        isExpense -> when (tx.category.lowercase()) {
            "comida", "alimentos" -> Icons.Outlined.Restaurant
            "transporte" -> Icons.Outlined.DirectionsCar
            "entretenimiento" -> Icons.Outlined.Theaters
            "salud" -> Icons.Outlined.LocalHospital
            "educación" -> Icons.Outlined.School
            else -> Icons.Outlined.ShoppingBag
        }
        else -> Icons.Outlined.AccountBalanceWallet
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon, tx.category, tint = iconColor, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.category, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                if (!tx.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(tx.description, fontSize = 13.sp, color = colors.textSecondary, maxLines = 1)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(tx.date.toFriendlyDateString(), fontSize = 12.sp, color = colors.textTertiary, fontWeight = FontWeight.Medium)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("$amountPrefix${tx.amount.toCurrencyString()}", color = iconColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Delete, "Eliminar", tint = colors.textTertiary.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

fun Double.toCurrencyString(): String = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(this)

@RequiresApi(Build.VERSION_CODES.O)
fun String.toFriendlyDateString(): String {
    return try {
        val instant = Instant.parse(this)
        DateTimeFormatter.ofPattern("d MMM, yyyy", Locale("es", "MX")).withZone(ZoneId.systemDefault()).format(instant)
    } catch (e: Exception) {
        this.take(10)
    }
}