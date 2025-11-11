package com.finedu.app.ui.profile

import com.finedu.app.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.finedu.app.ui.dictation.UiEvent
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Success -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is UiEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message, withDismissAction = true)
                    }
                }
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {

            item {
                ProfileHeader(
                    name = state.name,
                    email = state.email
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ConfigCard(
                    title = "Configuración de Capital",
                    subtitle = "Define tu capital y la frecuencia",
                    icon = Icons.Outlined.AccountBalanceWallet
                ) {
                    ConfiguracionCapitalContent(
                        onSave = { monto, frecuencia ->
                            viewModel.saveCapital(monto, frecuencia)
                        }
                    )
                }
            }
            item {
                ConfigCard(
                    title = "Notificaciones",
                    subtitle = "Configura alertas sobre notificaciones",
                    icon = Icons.Outlined.Notifications
                ) {
                    NotificacionesContent()
                }
            }
            item {
                ConfigCard(
                    title = "Privacidad y Seguridad",
                    subtitle = "Controla tu privacidad y seguridad",
                    icon = Icons.Outlined.Lock
                ) {
                    PrivacidadContent()
                }
            }
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
            item {

                ActionLink(
                    title = "Cerrar Sesión",
                    icon = Icons.Outlined.Logout,
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
}

@Composable
fun ProfileHeader(name: String, email: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.fondo_portada),
            contentDescription = "Banner de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier.height(150.dp)
        )

        // Contenido sobre el banner
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile_pic),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text(text = email,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp)
        }


    }
}
@Composable
fun ConfigCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    // 1. Estado para saber si la tarjeta está expandida
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            // 2. Animamos el cambio de tamaño
            .animateContentSize(animationSpec = tween(0)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 3. Fila superior (la que siempre se ve)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // 4. El clic ahora expande/contrae
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = title, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.SemiBold)
                    Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
                }
                // 5. El icono de la flecha cambia
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Divider(modifier = Modifier.padding(bottom = 8.dp))
                    expandedContent()
                }
            }
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
fun ActionLink(title: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.error, onClick: () -> Unit) {
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionCapitalContent(
    onSave: (String, String) -> Unit
) {
    var monto by remember { mutableStateOf("$ 0.00") }
    val opcionesFrecuencia = listOf("Semanal", "Quincenal", "Mensual")
    var isFrecuenciaExpanded by remember { mutableStateOf(false) }
    var frecuencia by remember { mutableStateOf(opcionesFrecuencia[2]) }

    OutlinedTextField(
        value = monto,
        onValueChange = { monto = it },
        label = { Text("Monto de Capital") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    ExposedDropdownMenuBox(
        expanded = isFrecuenciaExpanded,
        onExpandedChange = { isFrecuenciaExpanded = !isFrecuenciaExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = frecuencia,
            onValueChange = {},
            readOnly = true,
            label = { Text("Frecuencia de Actualización") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFrecuenciaExpanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = isFrecuenciaExpanded,
            onDismissRequest = { isFrecuenciaExpanded = false }
        ) {
            opcionesFrecuencia.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        frecuencia = option
                        isFrecuenciaExpanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = {
            onSave(monto, frecuencia)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Guardar")
    }
}
@Composable
fun NotificacionesContent() {
    var switch1 by remember { mutableStateOf(false) }
    var switch2 by remember { mutableStateOf(false) }

    ConfigRowWithSwitch(
        title = "Recordatorios de Inversión",
        checked = switch1,
        onCheckedChange = { switch1 = it }
    )
    ConfigRowWithSwitch(
        title = "Reportes Mensuales",
        checked = switch2,
        onCheckedChange = { switch2 = it }
    )
}

@Composable
fun PrivacidadContent() {
    var switch1 by remember { mutableStateOf(false) }
    var switch2 by remember { mutableStateOf(false) }

    ConfigRowWithSwitch(
        title = "Autenticación biométrica",
        subtitle = "Usar huella dactilar o Face ID",
        checked = switch1,
        onCheckedChange = { switch1 = it }
    )
    ConfigRowWithSwitch(
        title = "Modo Privado",
        subtitle = "Ocultar saldos en la pantalla principal",
        checked = switch2,
        onCheckedChange = { switch2 = it }
    )
}

@Composable
fun PreferenciasContent() {
    var switch1 by remember { mutableStateOf(false) }

    ConfigRowWithSwitch(
        title = "Mostrar consejos",
        subtitle = "Recibir tips de educación financiera",
        checked = switch1,
        onCheckedChange = { switch1 = it }
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { /* TODO: Exportar datos */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Exportar Datos")
    }
}
@Composable
fun ConfigRowWithSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}