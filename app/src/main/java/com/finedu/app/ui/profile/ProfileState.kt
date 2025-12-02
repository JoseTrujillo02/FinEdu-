package com.finedu.app.ui.profile

/**
 * Define la información que 'ProfileScreen' necesita para dibujarse.
 */
data class ProfileState(
    // Datos del Usuario
    val name: String = "Cargando...",
    val email: String = "Cargando...",

    // Datos de Configuración de Capital
    val capitalAmount: String = "",
    val capitalPeriodicity: String = "Mensual",
    val isLoadingCapital: Boolean = true, // Para el spinner de carga (GET)
    val isSavingCapital: Boolean = false  // Para el spinner del botón (PUT)
)