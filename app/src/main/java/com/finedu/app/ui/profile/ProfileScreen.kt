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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

import com.finedu.app.ui.theme.SetStatusBarIcons
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
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showForceLogoutDialog by remember { mutableStateOf(false) }
    var forceLogoutTitle by remember { mutableStateOf("") }   // Título dinámico
    var forceLogoutMessage by remember { mutableStateOf("") }

    SetStatusBarIcons(useDarkIcons = false)

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProfileUiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is ProfileUiEvent.NavigateToLogin -> {
                    onLogoutClick()
                }
                is ProfileUiEvent.SaveCapitalSuccess -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("show_success_snackbar", "¡Capital guardado!")
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_transactions", true)
                    navController.popBackStack()
                }
                is ProfileUiEvent.ShowForceLogoutDialog -> {
                    forceLogoutTitle = event.title
                    forceLogoutMessage = event.message
                    showForceLogoutDialog = true
                }
            }
        }
    }

    if (showForceLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onLogoutClick() },
            icon = {
                // Cambiamos el icono según el título para que se vea bonito
                if (forceLogoutTitle.contains("Expirada"))
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                else
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
            },
            title = { Text(forceLogoutTitle) },
            text = { Text(forceLogoutMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showForceLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text("Aceptar e Iniciar Sesión")
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { newPass ->
                showPasswordDialog = false
                viewModel.changePassword(newPass)
            }
        )
    }
    if (showConfirmDialog) {
        ConfirmDeleteDialog(
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                viewModel.deleteAccount() // Llama al ViewModel
            }
        )
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
                        // Pasa los datos desde el state
                        amount = state.capitalAmount,
                        periodicity = state.capitalPeriodicity,
                        isLoading = state.isLoadingCapital,
                        isSaving = state.isSavingCapital,
                        // Pasa las acciones al ViewModel
                        onAmountChanged = { viewModel.onCapitalAmountChanged(it) },
                        onPeriodicityChanged = { viewModel.onPeriodicityChanged(it) },
                        onSave = { viewModel.saveCapital() }
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
                    title = "Cambiar Contraseña",
                    icon = Icons.Outlined.LockReset, // Asegúrate de tener este icono o usa Icons.Default.Lock
                    color = MaterialTheme.colorScheme.primary, // Color normal
                    onClick = { showPasswordDialog = true }
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
                    onClick = {
                        showConfirmDialog = true
                    }
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
    // Acepta el estado y los eventos desde el ViewModel
    amount: String,
    periodicity: String,
    isLoading: Boolean,
    isSaving: Boolean,
    onAmountChanged: (String) -> Unit,
    onPeriodicityChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    // Ya no usa 'remember' para 'monto' o 'frecuencia'
    val opcionesFrecuencia = listOf("Semanal", "Quincenal", "Mensual")
    var isFrecuenciaExpanded by remember { mutableStateOf(false) }

    // Muestra un spinner mientras carga los datos (GET)
    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Muestra los campos cuando los datos están listos
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChanged, // Llama al ViewModel
            label = { Text("Monto de Capital") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            readOnly = isSaving // Deshabilita si está guardando
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = isFrecuenciaExpanded,
            onExpandedChange = { if (!isSaving) isFrecuenciaExpanded = !isFrecuenciaExpanded }, // Deshabilita
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = periodicity,
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
                            onPeriodicityChanged(option) // Llama al ViewModel
                            isFrecuenciaExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSave, // Llama al ViewModel
            enabled = !isSaving, // Se deshabilita si está guardando (PUT)
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Guardar")
            }
        }
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


@Composable
fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = "Advertencia") },
        title = { Text("Eliminar Cuenta") },
        text = { Text("¿Estás seguro? Esta acción es permanente y no se puede deshacer. Todos tus datos serán eliminados.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sí, Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Contraseña") },
        text = {
            Column {
                Text("Ingresa tu nueva contraseña (mínimo 8 caracteres):")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newPassword) },
                enabled = newPassword.length >= 8 // Validación simple UI
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}