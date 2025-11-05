package com.finedu.app.ui.profile


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogoutClick: () -> Unit, // Función para manejar el cierre de sesión
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Usamos LazyColumn para que la pantalla sea deslizable
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {

        // --- 1. Cabecera con Banner y Avatar ---
        item {
            ProfileHeader(
                name = state.name, // O usa state.name si lo tienes
                email = state.email, // "c.andres@gmail.com"
                onSettingsClick = { /* TODO: Navegar a ajustes de perfil */ }
            )
        }

        // --- 2. Tarjetas de Configuración ---
        item { Spacer(modifier = Modifier.height(8.dp)) } // Espacio después de la cabecera
        item {
            ConfigCard(
                title = "Configuración de Capital",
                subtitle = "Define tu capital y la frecuencia",
                icon = Icons.Outlined.AccountBalanceWallet,
                onClick = { /* navController.navigate("config_capital") */ }
            )
        }
        item {
            ConfigCard(
                title = "Notificaciones",
                subtitle = "Configura alertas sobre notificaciones",
                icon = Icons.Outlined.Notifications,
                onClick = { /* navController.navigate("notificaciones") */ }
            )
        }
        item {
            ConfigCard(
                title = "Privacidad y Seguridad",
                subtitle = "Controla tu privacidad y seguridad",
                icon = Icons.Outlined.Lock,
                onClick = { /* navController.navigate("privacidad") */ }
            )
        }
        item {
            ConfigCard(
                title = "Preferencias de la App",
                subtitle = "Personaliza tu experiencia",
                icon = Icons.Outlined.Tune,
                onClick = { /* navController.navigate("preferencias") */ }
            )
        }

        // --- 3. Enlaces de Ayuda ---
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            InfoLink(
                title = "Contactar Soporte",
                icon = Icons.Outlined.ContactSupport,
                onClick = { /* TODO: Abrir chat de soporte */ }
            )
        }
        item {
            InfoLink(
                title = "Política de Privacidad",
                icon = Icons.Outlined.Policy,
                onClick = { /* TODO: Abrir enlace web */ }
            )
        }
        item {
            InfoLink(
                title = "Centro de Ayuda",
                icon = Icons.Outlined.HelpOutline,
                onClick = { /* TODO: Abrir enlace web */ }
            )
        }

        // --- 4. Acciones de Peligro ---
        item {
            ActionLink(
                title = "Cerrar Sesión",
                icon = Icons.Outlined.Logout,
                // ¡Usamos la función que nos pasaron!
                onClick = onLogoutClick
            )
        }
        item {
            ActionLink(
                title = "Eliminar Cuenta",
                icon = Icons.Outlined.DeleteForever,
                color = MaterialTheme.colorScheme.error,
                onClick = { /* TODO: Mostrar diálogo de confirmación */ }
            )
        }

        // --- 5. Footer (Versión) ---
        item {
            Text(
                text = "FinEdu v1.0.0\n© 2024 Todos los derechos reservados",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )
        }
    }
}

// --- Componentes Reutilizables ---
// (Estos van en el mismo archivo, debajo de la función principal)

@Composable
fun ProfileHeader(name: String, email: String, onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // Altura de la cabecera
    ) {
        // Fondo de banner (Debes añadir esta imagen a res/drawable)
        Image(
            // painter = painterResource(id = R.drawable.profile_banner),
            imageVector = Icons.Default.Place, // Icono temporal
            contentDescription = "Banner de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Contenido sobre el banner
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            // Avatar (Debes añadir esta imagen a res/drawable)
            Image(
                // painter = painterResource(id = R.drawable.avatar_cristian),
                imageVector = Icons.Default.Person, // Avatar temporal
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = email, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }

        // Icono de Ajustes (arriba a la derecha)
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = Color.White)
        }
    }
}

@Composable
fun ConfigCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun InfoLink(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp), // Más padding horizontal
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun ActionLink(title: String, icon: ImageVector, color: Color = Color.Red, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp), tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 14.sp, color = color)
    }
}