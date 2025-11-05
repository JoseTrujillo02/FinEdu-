@file:OptIn(ExperimentalMaterial3Api::class)

package com.finedu.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finedu.app.R // Asegúrate de tener R.drawable.ic_car
import com.finedu.app.navigation.AppRutas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel
import com.finedu.app.ui.dictation.VoiceDictationViewModel

sealed class MainScreenDestinations(val route: String) {
    object Home : MainScreenDestinations("home_tab")
    object Profile : MainScreenDestinations("profile_tab")
    object Dictation : MainScreenDestinations("voice_dictation")
}

@Composable
fun MainScreen(mainNavController: NavController, onLogoutClick: () -> Unit) {
    val internalNavController = rememberNavController()

    Scaffold(
        topBar = {
            MainTopBar(
                onNotificationClick = {
                    // Usa el controlador PRINCIPAL para ir a Notificaciones
                    mainNavController.navigate(AppRutas.NOTIFICATIONS_SCREEN)
                },
                onProfileClick = {
                    // Usa el controlador PRINCIPAL para ir a Perfil
                    mainNavController.navigate(AppRutas.PROFILE_SCREEN)
                }
            )
        }
    ) { paddingValues ->
        // El NavHost interno se queda
        NavHost(
            navController = internalNavController,
            startDestination = MainScreenDestinations.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            // 1. RUTA HOME (¡Ahora con la UI del Dashboard!)
            composable(MainScreenDestinations.Home.route) {
                HomeScreenDashboard(
                    onAddTransactionClick = {
                        // Usa el controlador INTERNO para ir a Dictado
                        internalNavController.navigate(MainScreenDestinations.Dictation.route)
                    }
                )
            }

            //2 Ruta de dictado por voz
            composable(MainScreenDestinations.Dictation.route) {

                // --- 2. ¡CREA EL VIEWMODEL AQUÍ! ---
                val viewModel: VoiceDictationViewModel = hiltViewModel()

                // Le pasamos el ViewModel a la pantalla
                VoiceDictationScreen(
                    navController = internalNavController,
                    viewModel = viewModel // <-- 3. Pásalo
                )
            }

            // 3. RUTA DE PERFIL (Se queda igual, la llamaremos desde un ícono)
            // (Esta es la pantalla de Perfil que construimos antes,
            // la llamaremos desde el TopBar o un BottomNavBar)
        }
    }
}

// --- Componentes de MainScreen ---

@Composable
fun MainTopBar(onNotificationClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 2. Aplicamos la altura y fondo de tu diseño
            .height(149.dp)
            .background(Color.Black)
            .padding(horizontal = 16.dp), // Padding para que no se pegue a los bordes
        verticalAlignment = Alignment.CenterVertically // Centramos todo verticalmente
    ) {

        // 3. Este es tu Título "Bienvenido"
        Text(
            text = "Bienvenido",
            // Usamos .copy() como aprendimos, para modificar el estilo base
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Normal, // Tu imagen usa una fuente regular, no bold
                fontSize = 48.sp,
                letterSpacing = -2.sp,
                color = Color.White
            ),
            // 4. ¡Importante! .weight(1f) empuja los iconos a la derecha
            modifier = Modifier.weight(1f)
        )

        // 5. Icono de Notificaciones
        IconButton(onClick = onNotificationClick) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = Color.White, // ¡Le damos tinte blanco!
                modifier = Modifier.size(32.dp) // Tamaño consistente
            )
        }

        // 6. Círculo de Perfil
        IconButton(onClick = onProfileClick) {
            Image(
                imageVector = Icons.Default.Person, // Icono temporal
                contentDescription = "Perfil",
                modifier = Modifier
                    .size(32.dp) // Tamaño del círculo
                    .clip(CircleShape) // Lo hace círculo
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
        }
    }
}
@Composable
fun HomeScreenDashboard(onAddTransactionClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item { SaludFinancieraCard() }

        item { AddTransactionCard(onClick = onAddTransactionClick) }

        item { MetasCard() }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SaludFinancieraCard() {
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
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Reemplaza con tu ícono de €
                    contentDescription = "Dinero",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ingresos", fontSize = 14.sp, color = Color.Gray)
                    Text("$3,500", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Egresos", fontSize = 14.sp, color = Color.Gray)
                    Text("$2,800", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
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
        colors = CardDefaults.cardColors(containerColor = Color.Black) // Como en tu diseño
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

            // Meta 1
            MetaItem(titulo = "Adquirir Automóvil", progreso = 0.85f)
            Spacer(modifier = Modifier.height(16.dp))
            // Meta 2
            MetaItem(titulo = "Adquirir Automóvil", progreso = 0.65f) // Cambia el título por tu otra meta
        }
    }
}

@Composable
fun MetaItem(titulo: String, progreso: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Icono de auto (debes añadirlo a res/drawable)
        Image(
            // painter = painterResource(id = R.drawable.ic_car),
            imageVector = Icons.Default.ArrowForward, // Icono temporal
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