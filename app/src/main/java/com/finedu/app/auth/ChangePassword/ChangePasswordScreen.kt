package com.finedu.app.auth.changepassword

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finedu.app.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onChangePasswordClick: (String) -> Unit,
    onBackClick: () -> Unit,
    state: ChangePasswordState = ChangePasswordState(), // ✅ Cambiar a ChangePasswordState
    onDismissError: () -> Unit = {},
    onClearPasswordError: () -> Unit = {}
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordsMatchError by remember { mutableStateOf<String?>(null) }

    // ✅ Navegar automáticamente después del éxito
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            delay(2000) // Esperar 2 segundos para que el usuario vea el mensaje
            onBackClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8E8E8))
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // TopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = "Cambiar Contraseña",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7BBB3E),
                    titleContentColor = Color.White
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Notificación general de éxito/error
                AnimatedVisibility(
                    visible = state.error != null || state.isSuccess,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -it }
                    ) + fadeOut(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    NotificationCard(
                        message = state.error ?: "¡Contraseña cambiada exitosamente!",
                        isError = state.error != null,
                        onDismiss = onDismissError
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nueva Contraseña",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Ingresa tu nueva contraseña",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Campo de Nueva Contraseña
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                passwordsMatchError = null
                                if (state.passwordError != null) {
                                    onClearPasswordError()
                                }
                            },
                            placeholder = {
                                Text(
                                    "Nueva contraseña",
                                    color = Color.Gray.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Password",
                                    tint = if (state.passwordError != null)
                                        Color(0xFFFF5252)
                                    else
                                        Color.Gray
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (newPasswordVisible)
                                                android.R.drawable.ic_menu_view
                                            else
                                                android.R.drawable.ic_secure
                                        ),
                                        contentDescription = if (newPasswordVisible) "Ocultar" else "Mostrar",
                                        tint = if (state.passwordError != null)
                                            Color(0xFFFF5252)
                                        else
                                            Color.Gray
                                    )
                                }
                            },
                            isError = state.passwordError != null,
                            supportingText = if (state.passwordError != null) {
                                {
                                    Text(
                                        text = state.passwordError,
                                        color = Color(0xFFFF5252),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            } else null,
                            visualTransformation = if (newPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorContainerColor = Color.White,
                                focusedBorderColor = if (state.passwordError != null)
                                    Color(0xFFFF5252)
                                else
                                    Color.Transparent,
                                unfocusedBorderColor = if (state.passwordError != null)
                                    Color(0xFFFF5252)
                                else
                                    Color.Transparent,
                                errorBorderColor = Color(0xFFFF5252),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                errorTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            enabled = !state.isLoading
                        )

                        // Campo de Confirmar Contraseña
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                passwordsMatchError = null
                            },
                            placeholder = {
                                Text(
                                    "Confirmar contraseña",
                                    color = Color.Gray.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Confirm Password",
                                    tint = if (passwordsMatchError != null)
                                        Color(0xFFFF5252)
                                    else
                                        Color.Gray
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (confirmPasswordVisible)
                                                android.R.drawable.ic_menu_view
                                            else
                                                android.R.drawable.ic_secure
                                        ),
                                        contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar",
                                        tint = if (passwordsMatchError != null)
                                            Color(0xFFFF5252)
                                        else
                                            Color.Gray
                                    )
                                }
                            },
                            isError = passwordsMatchError != null,
                            supportingText = if (passwordsMatchError != null) {
                                {
                                    Text(
                                        text = passwordsMatchError!!,
                                        color = Color(0xFFFF5252),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            } else null,
                            visualTransformation = if (confirmPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorContainerColor = Color.White,
                                focusedBorderColor = if (passwordsMatchError != null)
                                    Color(0xFFFF5252)
                                else
                                    Color.Transparent,
                                unfocusedBorderColor = if (passwordsMatchError != null)
                                    Color(0xFFFF5252)
                                else
                                    Color.Transparent,
                                errorBorderColor = Color(0xFFFF5252),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                errorTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            enabled = !state.isLoading
                        )

                        // Info de requisitos
                        Text(
                            text = "• La contraseña debe tener entre 8 y 20 caracteres",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        )

                        // Botón de cambiar contraseña
                        Button(
                            onClick = {
                                if (newPassword != confirmPassword) {
                                    passwordsMatchError = "Las contraseñas no coinciden"
                                } else {
                                    onChangePasswordClick(newPassword)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7BBB3E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.isLoading && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Cambiar Contraseña",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val backgroundColor = if (isError) {
        Color(0xFFFF5252).copy(alpha = 0.95f)
    } else {
        Color(0xFF4CAF50).copy(alpha = 0.95f)
    }

    LaunchedEffect(message) {
        delay(4000)
        onDismiss()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = message,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}