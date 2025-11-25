package com.finedu.app.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finedu.app.ui.SplashScreen
import com.finedu.app.auth.login.LoginScreen
import com.finedu.app.auth.login.LoginViewModel
import com.finedu.app.auth.register.RegisterScreen
import com.finedu.app.auth.register.RegisterViewModel
import com.finedu.app.auth.changepassword.ChangePasswordScreen
import com.finedu.app.auth.changepassword.ChangePasswordViewModel
import com.finedu.app.data.SessionRepository
import com.finedu.app.data.UserSessionData
import com.finedu.app.ui.MainScreen
import com.finedu.app.ui.NotificationsScreen
import com.finedu.app.ui.TermsScreen
import com.finedu.app.ui.profile.ProfileScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    val repository: SessionRepository
) : ViewModel()

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionRepository = sessionViewModel.repository

    NavHost(
        navController = navController,
        startDestination = AppRutas.SPLASH_SCREEN
    ) {

        composable(AppRutas.SPLASH_SCREEN) {
            val sessionState by sessionRepository.getStoredSession()
                .collectAsState(initial = "LOADING")
            if (sessionState != "LOADING") {
                val destination = if (sessionState is UserSessionData) {
                    AppRutas.HOME_SCREEN
                } else {
                    AppRutas.LOGIN_SCREEN
                }
                SplashScreen(
                    navController = navController,
                    destinationRoute = destination
                )
            }
        }

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
                onDismissError = { viewModel.clearError() },
            )
        }

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
                onRegisterClick = { name, email, password, confirmPassword ->
                    viewModel.register(name, email, password, confirmPassword)
                },
                onLoginClick = {
                    navController.popBackStack()
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

        composable(AppRutas.CHANGE_PASSWORD_SCREEN) {
            val viewModel: ChangePasswordViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            // Navegar de vuelta cuando el cambio sea exitoso
            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    delay(2000) // Esperar para que vea el mensaje de éxito
                    navController.popBackStack()
                }
            }

            // Manejar sesión expirada (error 401)
            LaunchedEffect(state.error) {
                if (state.error?.contains("Sesión expirada") == true ||
                    state.error?.contains("Sesión no válida") == true) {
                    delay(2000)
                    navController.navigate(AppRutas.LOGIN_SCREEN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            ChangePasswordScreen(
                onChangePasswordClick = { newPassword ->
                    viewModel.changePassword(newPassword)
                },
                onBackClick = { navController.popBackStack() },
                state = state,
                onDismissError = { viewModel.clearError() },
                onClearPasswordError = { viewModel.clearPasswordError() }
            )
        }

        composable(AppRutas.HOME_SCREEN) {
            MainScreen(
                mainNavController = navController,
                onLogoutClick = {
                    scope.launch {
                        sessionRepository.clearSession()
                        navController.navigate(AppRutas.LOGIN_SCREEN) {
                            popUpTo(AppRutas.HOME_SCREEN) { inclusive = true }
                        }
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
                    scope.launch {
                        sessionRepository.clearSession()
                        navController.navigate(AppRutas.LOGIN_SCREEN) {
                            popUpTo(AppRutas.HOME_SCREEN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AppRutas.TERMS_SCREEN) {
            TermsScreen(navController = navController)
        }
    }
}