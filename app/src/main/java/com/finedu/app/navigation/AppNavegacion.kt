package com.finedu.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Importa tus pantallas y ViewModels
import com.finedu.app.ui.SplashScreen
import com.finedu.app.auth.login.LoginScreen
import com.finedu.app.auth.login.LoginViewModel
import com.finedu.app.auth.register.RegisterScreen
import com.finedu.app.auth.register.RegisterViewModel
import com.finedu.app.ui.MainScreen
import com.finedu.app.ui.NotificationsScreen
import com.finedu.app.ui.TermsScreen
import com.finedu.app.ui.VoiceDictationScreen
import com.finedu.app.ui.profile.ProfileScreen

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRutas.SPLASH_SCREEN
    ) {

        // 1. Pantalla Splash (Línea 35)
        composable(AppRutas.SPLASH_SCREEN) {
            // ¡Llamada corregida!
            SplashScreen(
                navController = navController, // <-- ¡AÑADIDO!
                destinationRoute = AppRutas.LOGIN_SCREEN
            )
        }

        // 2. Pantalla Login (Esta se queda igual, estaba bien)
        // 2. Pantalla Login (¡Correcto!)
        composable(AppRutas.LOGIN_SCREEN) {
            val viewModel: LoginViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    navController.navigate(AppRutas.HOME_SCREEN) {
                        popUpTo(AppRutas.LOGIN_SCREEN) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                onLoginClick = { email, password -> viewModel.login(email, password) },
                onRegisterClick = { navController.navigate(AppRutas.REGISTER_SCREEN) },
                state = state,
                onDismissError = { viewModel.clearError() }
            )
        }

        // 3. Pantalla Register (Esta se queda igual, estaba bien)
        composable(AppRutas.REGISTER_SCREEN) {
            val viewModel: RegisterViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    navController.navigate(AppRutas.HOME_SCREEN) {
                        popUpTo(AppRutas.LOGIN_SCREEN) { inclusive = true }
                    }
                }
            }
            RegisterScreen(
                onRegisterClick = { name, email, password ->
                    viewModel.register(name, email, password)
                },
                onLoginClick = {
                    navController.popBackStack() // Regresa a Login
                },
                onTermsClick = {
                    navController.navigate(AppRutas.TERMS_SCREEN)
                },
                state = state,
                onDismissError = {
                    viewModel.clearError()
                }
            )
        }

        // 4. Pantalla Principal (Línea 96)
        composable(AppRutas.HOME_SCREEN) {
            MainScreen(
                mainNavController = navController, // Le pasamos el navController
                onLogoutClick = {
                    // Lógica para cerrar sesión:
                    // Navega al Login y borra toda la pila anterior.
                    navController.navigate(AppRutas.LOGIN_SCREEN) {
                        popUpTo(AppRutas.HOME_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRutas.NOTIFICATIONS_SCREEN) {
            NotificationsScreen(navController = navController)
        }

        composable(AppRutas.PROFILE_SCREEN) {
            ProfileScreen(
                navController = navController,
                onLogoutClick = {
                    // Lógica para cerrar sesión
                    navController.navigate(AppRutas.LOGIN_SCREEN) {
                        popUpTo(AppRutas.HOME_SCREEN) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRutas.TERMS_SCREEN) {
            TermsScreen(navController = navController)
        }
    }
}