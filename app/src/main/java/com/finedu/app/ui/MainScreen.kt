@file:OptIn(ExperimentalMaterial3Api::class)

package com.finedu.app.ui


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.runtime.State
import androidx.compose.ui.text.style.TextAlign
import com.finedu.app.ui.theme.SetStatusBarIcons
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


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
    SetStatusBarIcons(useDarkIcons = false)

    // Cargar datos automáticamente al entrar
    LaunchedEffect(Unit) {
        viewModel.loadDashboardDataForThisMonth()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MainTopBar(
                onNotificationClick = {
                    mainNavController.navigate(AppRutas.NOTIFICATIONS_SCREEN)
                },
                onProfileClick = {
                    mainNavController.navigate(AppRutas.PROFILE_SCREEN)
                },
                onRefreshClick = {
                    viewModel.loadDashboardDataForThisMonth()
                }
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

                val refreshResultState: State<Boolean?>? = backStackEntry
                    ?.savedStateHandle
                    ?.getLiveData<Boolean>("refresh_transactions")
                    ?.observeAsState()
                val refreshResult = refreshResultState?.value

                LaunchedEffect(refreshResult) {
                    if (refreshResult == true) {
                        viewModel.loadDashboardDataForThisMonth()
                        backStackEntry?.savedStateHandle?.set("refresh_transactions", false)
                    }
                }
                val successMessageState: State<String?>? = backStackEntry
                    ?.savedStateHandle
                    ?.getLiveData<String>("show_success_snackbar")
                    ?.observeAsState()
                val successMessage = successMessageState?.value

                LaunchedEffect(successMessage) {
                    if (successMessage != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = successMessage)
                        }
                        backStackEntry?.savedStateHandle?.set("show_success_snackbar", null)
                    }
                }
                LaunchedEffect(state.error) {
                    val errorMessage = state.error

                    if (errorMessage != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = errorMessage,
                                duration = SnackbarDuration.Long,
                                withDismissAction = true
                            )
                        }
                        viewModel.clearError()
                    }
                }

                HomeScreenDashboard(
                    onAddTransactionClick = {
                        internalNavController.navigate(MainScreenDestinations.Dictation.route)
                    },
                    state = state
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
fun MainTopBar(
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(149.dp)
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 48.sp,
                letterSpacing = -2.sp,
                color = Color.White
            ),
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onRefreshClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refrescar",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(onClick = onNotificationClick) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        IconButton(onClick = onProfileClick) {
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = "Perfil",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenDashboard(
    onAddTransactionClick: () -> Unit,
    state: MainDashboardState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        SaludFinancieraCard(
            ingresos = state.capitalAmount.toCurrencyString(),
            egresos = state.totalEgresos.toCurrencyString()
        )

        Spacer(modifier = Modifier.height(16.dp))

        AddTransactionCard(onClick = onAddTransactionClick)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transacciones Recientes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        // Panel de transacciones con scroll independiente
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Ocupa el espacio restante
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay transacciones",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.transactions) { transaction ->
                        TransactionItemRow(tx = transaction)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SaludFinancieraCard(ingresos: String, egresos: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tu Salud Financiera", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Dinero",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ingresos", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(ingresos, fontSize = 16.sp, fontWeight = FontWeight.Bold, color= Color(0xFF4CAF50))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Egresos", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(egresos, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB81313))
                }
            }
        }
    }
}

@Composable
fun AddTransactionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Agrega tu transacción", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Modo priorizado y recordatorio", color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.Mic, contentDescription = "Grabar", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionItemRow(tx: TransactionItem) {
    val amountColor = if (tx.type == "expense") Color(0xFFB81313) else Color(0xFF4CAF50)
    val amountPrefix = if (tx.type == "expense") "-" else "+"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = tx.category,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.category, fontWeight = FontWeight.SemiBold)

                Text(
                    text = tx.description ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Text(
                    text = tx.date.toFriendlyDateString(),
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }

            Text(
                text = "$amountPrefix${tx.amount.toCurrencyString()}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

fun Double.toCurrencyString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(this)
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toFriendlyDateString(): String {
    return try {
        val instant = Instant.parse(this)
        val formatter = DateTimeFormatter.ofPattern(
            "d MMM, yyyy",
            Locale("es", "MX")
        ).withZone(ZoneId.systemDefault())

        formatter.format(instant)
    } catch (e: Exception) {
        this.take(10)
    }
}