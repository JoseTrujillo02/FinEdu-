package com.finedu.app.ui.profile


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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
                icon = Icons.Outlined.AccountBalanceWallet
            ) {
                // Este bloque es el 'expandedContent'
                ConfiguracionCapitalContent()
            }
        }
        item {
            ConfigCard(
                title = "Notificaciones",
                subtitle = "Configura alertas sobre notificaciones",
                icon = Icons.Outlined.Notifications
            ) {
                // Contenido expandido para Notificaciones
                NotificacionesContent()
            }
        }
        item {
            ConfigCard(
                title = "Privacidad y Seguridad",
                subtitle = "Controla tu privacidad y seguridad",
                icon = Icons.Outlined.Lock
            ) {
                // Contenido expandido para Privacidad
                PrivacidadContent()
            }
        }
        item {
            ConfigCard(
                title = "Preferencias de la App",
                subtitle = "Personaliza tu experiencia",
                icon = Icons.Outlined.Tune
            ) {
                // Contenido expandido para Preferencias
                PreferenciasContent()
            }
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
            imageVector = Icons.Default.Adb, // Icono temporal
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
fun ConfigCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    // ¡NUEVO! Acepta un Composable como contenido expandido
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

            // 6. ¡El contenido expandido!
            // Se muestra/oculta con una animación
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

@OptIn(ExperimentalMaterial3Api::class) // <-- ¡AÑADE ESTA LÍNEA!
@Composable
fun ConfiguracionCapitalContent() {
    var monto by remember { mutableStateOf("$ 0.00") }

    // --- 1. Define tus opciones y el estado de expansión ---
    val opcionesFrecuencia = listOf("Semanal", "Quincenal", "Mensual")
    var isFrecuenciaExpanded by remember { mutableStateOf(false) }
    var frecuencia by remember { mutableStateOf(opcionesFrecuencia[2]) } // "Mensual" por defecto

    // (El campo de "Monto de Capital" se queda igual)
    OutlinedTextField(
        value = monto,
        onValueChange = { monto = it },
        label = { Text("Monto de Capital") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // --- 2. Reemplaza tu TextField de Frecuencia por este Box ---
    ExposedDropdownMenuBox(
        expanded = isFrecuenciaExpanded,
        onExpandedChange = { isFrecuenciaExpanded = !isFrecuenciaExpanded }, // Abre/cierra el menú
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- 3. Este es el TextField que se MUESTRA (el "ancla") ---
        OutlinedTextField(
            value = frecuencia, // Muestra la opción seleccionada
            onValueChange = {}, // No permitas que se escriba
            readOnly = true,
            label = { Text("Frecuencia de Actualización") },
            trailingIcon = {
                // Icono de flecha que rota automáticamente
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFrecuenciaExpanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor() // ¡Importante! Esto conecta el TextField con el menú
        )

        // --- 4. Este es el Menú que se DESPLIEGA ---
        ExposedDropdownMenu(
            expanded = isFrecuenciaExpanded,
            onDismissRequest = { isFrecuenciaExpanded = false } // Cierra si se hace clic fuera
        ) {
            // 5. Itera sobre tus opciones y crea un item por cada una
            opcionesFrecuencia.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        frecuencia = option // Actualiza el estado con la nueva opción
                        isFrecuenciaExpanded = false // Cierra el menú
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { /* TODO: Guardar capital (ahora puedes usar 'monto' y 'frecuencia') */ },
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

// --- COMPONENTE DE AYUDA (NUEVO) ---

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