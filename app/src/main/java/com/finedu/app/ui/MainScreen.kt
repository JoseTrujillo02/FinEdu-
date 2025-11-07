@file:OptIn(ExperimentalMaterial3Api::class)

package com.finedu.app.ui

// --- 1. IMPORTS ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh // <-- ¡Importa el icono de Refrescar!
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.livedata.observeAsState
// (Ya no se importan 'pulltorefresh' ni 'nestedScroll')
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.finedu.app.ui.dashboard.MainDashboardViewModel // Importa el ViewModel
import com.finedu.app.ui.dictation.VoiceDictationViewModel
import java.text.NumberFormat
import java.util.Locale
// --- FIN DE IMPORTS ---


sealed class MainScreenDestinations(val route: String) {
    object Home : MainScreenDestinations("home_tab")
    object Profile : MainScreenDestinations("profile_tab")
    object Dictation : MainScreenDestinations("voice_dictation")
}

@Composable
fun MainScreen(mainNavController: NavController, onLogoutClick: () -> Unit) {
    val internalNavController = rememberNavController()

    // Obtenemos el ViewModel aquí para pasárselo al TopBar
    val viewModel: MainDashboardViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            MainTopBar(
                onNotificationClick = {
                    mainNavController.navigate(AppRutas.NOTIFICATIONS_SCREEN)
                },
                onProfileClick = {
                    mainNavController.navigate(AppRutas.PROFILE_SCREEN)
                },
                // Añadimos la acción de refresco
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

            // 1. RUTA HOME
            composable(MainScreenDestinations.Home.route) {

                // Obtenemos el estado del ViewModel
                val state by viewModel.state.collectAsState()
                val backStackEntry = internalNavController.currentBackStackEntry
                val refreshResultState = backStackEntry
                    ?.savedStateHandle
                    ?.getLiveData<Boolean>("refresh_transactions")
                    ?.observeAsState()

                // --- 2. OBTIENE EL VALOR MANUALMENTE ---
                val refreshResult = refreshResultState?.value

                LaunchedEffect(refreshResult) {
                    if (refreshResult == true) {
                        viewModel.loadDashboardDataForThisMonth()
                        // Resetea el "recado"
                        backStackEntry?.savedStateHandle["refresh_transactions"] = false
                    }
                }
                // --- FIN DE LA LÓGICA DEL RECADO ---

                HomeScreenDashboard(
                    onAddTransactionClick = {
                        internalNavController.navigate(MainScreenDestinations.Dictation.route)
                    },
                    state = state
                )
            }

            // 2. Ruta de dictado por voz
            composable(MainScreenDestinations.Dictation.route) {
                val dictationViewModel: VoiceDictationViewModel = hiltViewModel()
                VoiceDictationScreen(
                    navController = internalNavController,
                    viewModel = dictationViewModel
                )
            }

            // 3. RUTA DE PERFIL (Placeholder)
            composable(MainScreenDestinations.Profile.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Placeholder de Perfil Interno")
                }
            }
        }
    }
}

// --- Componentes de MainScreen ---

@Composable
fun MainTopBar(
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRefreshClick: () -> Unit // <-- Acepta la nueva acción
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(149.dp)
            .background(Color.Black)
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

        // --- BOTÓN DE REFRESCAR AÑADIDO ---
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

@Composable
fun HomeScreenDashboard(
    onAddTransactionClick: () -> Unit,
    state: MainDashboardState
    // (Ya no se necesita onRefresh aquí)
) {
    // --- LÓGICA DE PULL-TO-REFRESH ELIMINADA ---

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SaludFinancieraCard(
                ingresos = state.totalIngresos.toCurrencyString(),
                egresos = state.totalEgresos.toCurrencyString()
            )
        }

        item { AddTransactionCard(onClick = onAddTransactionClick) }

        item { MetasCard() }

        item {
            Text(
                text = "Transacciones Recientes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        // --- Lógica de Carga Simplificada ---
        if (state.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(state.transactions) { transaction ->
                TransactionItemRow(tx = transaction)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
    // --- CONTENEDOR DE PULL-TO-REFRESH ELIMINADO ---
}

@Composable
fun SaludFinancieraCard(ingresos: String, egresos: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tu Salud Financiera", style = MaterialTheme.typography.titleMedium)
            Text("Excelente - 30% de ahorro", color = Color(0xFF4CAF50), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Dinero",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ingresos", fontSize = 14.sp, color = Color.Gray)
                    Text(ingresos, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Egresos", fontSize = 14.sp, color = Color.Gray)
                    Text(egresos, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
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

@Composable
fun MetasCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Metas", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            MetaItem(titulo = "Adquirir Automóvil", progreso = 0.85f)
            Spacer(modifier = Modifier.height(16.dp))
            MetaItem(titulo = "Adquirir Automóvil", progreso = 0.65f)
        }
    }
}

@Composable
fun MetaItem(titulo: String, progreso: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = titulo,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            LinearProgressIndicator(
                progress = progreso,
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Text("${(progreso * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
fun TransactionItemRow(tx: TransactionItem) {
    val amountColor = if (tx.type == "expense") Color.Red else Color(0xFF4CAF50)
    val amountPrefix = if (tx.type == "expense") "-" else "+"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart, // Icono temporal
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
                Text(tx.description ?: "", fontSize = 12.sp, color = Color.Gray)
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

// --- FUNCIÓN DE AYUDA PARA FORMATEAR MONEDA ---
fun Double.toCurrencyString(): String {
    // Usamos Locale("es", "MX") para pesos mexicanos como ejemplo
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(this)
}