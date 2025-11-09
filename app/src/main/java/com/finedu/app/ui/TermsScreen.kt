package com.finedu.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y Condiciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            item {
                Text(
                    text = "TÉRMINOS Y CONDICIONES DE USO DE LA APLICACIÓN FINEDU",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Última actualización: 06 de noviembre de 2025")
                Spacer(modifier = Modifier.height(16.dp))

                // --------------------- 1
                Text(
                    text = "1. Aceptación de los Términos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Al utilizar FinEdu, usted declara que ha leído, entendido y acepta estos Términos y Condiciones. " +
                            "Si no está de acuerdo con cualquiera de ellos, debe dejar de utilizar la aplicación."
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------------- 2
                Text(
                    text = "2. Objetivo de la Aplicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FinEdu es una herramienta destinada a ayudar a los usuarios a gestionar y visualizar sus ingresos, " +
                            "gastos y capital personal con fines educativos e informativos. FinEdu no es una institución financiera, " +
                            "no maneja dinero real ni ofrece asesoramiento financiero profesional."
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------------- 3
                Text(
                    text = "3. Registro y Seguridad de la Cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El usuario es responsable de mantener la confidencialidad de sus datos de acceso y de todas las actividades realizadas en su cuenta."
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------------- 4
                Text(
                    text = "4. Uso Adecuado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El usuario acepta no utilizar la aplicación para actividades ilícitas, fraudulentas o que afecten el funcionamiento normal del sistema."
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------------- 5
                Text(
                    text = "5. Privacidad y Datos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FinEdu almacena información mínima necesaria para el funcionamiento del sistema, como ingresos, gastos y preferencias. " +
                            "Los datos no son compartidos con terceros sin autorización del usuario."
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------------- 6
                Text(
                    text = "6. Limitación de Responsabilidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FinEdu no garantiza la exactitud absoluta de los análisis estadísticos ni se responsabiliza por decisiones financieras tomadas por el usuario basadas en la aplicación."
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------------- 7
                Text(
                    text = "7. Eliminación de Cuenta y Datos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El usuario puede solicitar la eliminación total y definitiva de su cuenta. " +
                            "Una vez eliminados, los datos no podrán recuperarse."
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------------- 8
                Text(
                    text = "8. Modificaciones de los Términos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FinEdu se reserva el derecho de actualizar estos términos en cualquier momento. " +
                            "Los usuarios serán notificados en caso de cambios relevantes."
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Si tiene dudas o comentarios: 1930050@uttt.edu.mx",
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
