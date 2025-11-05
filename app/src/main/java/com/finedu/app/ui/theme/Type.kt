package com.finedu.app.ui.theme
import com.finedu.app.R

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val IstokWebFamily = FontFamily(
    // 400 (Regular)
    Font(R.font.istok_web_regular, FontWeight.Normal),

    // 700 (Bold)
    Font(R.font.istok_web_bold, FontWeight.Bold),

    // 400 Italic
    Font(R.font.istok_web_italic, FontWeight.Normal, androidx.compose.ui.text.font.FontStyle.Italic),
)
// ... (tu IstokWebFamily se queda igual) ...

val Typography = Typography(
    // Estilo para texto normal
    bodyLarge = TextStyle(
        fontFamily = IstokWebFamily, // <-- ¡CORREGIDO!
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // Estilo para títulos grandes (Ej: "Bienvenido")
    headlineLarge = TextStyle(
        fontFamily = IstokWebFamily, // <-- ¡CORREGIDO!
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp, // Ajusta el tamaño como en tu diseño
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Estilo para títulos medianos (Ej: "Configuración de Capital")
    titleMedium = TextStyle(
        fontFamily = IstokWebFamily, // <-- ¡CORREGIDO!
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    // Estilo para texto pequeño (Ej: "Define tu capital...")
    bodySmall = TextStyle(
        fontFamily = IstokWebFamily, // <-- ¡CORREGIDO!
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )

    /* ¡Define todos los estilos que necesites aquí! */
)
