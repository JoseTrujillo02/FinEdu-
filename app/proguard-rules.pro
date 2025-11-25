# ==========================
# 🟦 PROYECTO FINEDU
# Configuración ProGuard / R8
# ==========================

# -----------------------------------------
# 🔵 Mantenemos anotaciones importantes
# -----------------------------------------
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# -----------------------------------------
# 🔵 Evitar que se eliminen Data Classes
# -----------------------------------------
-keep class kotlin.Metadata { *; }

# -----------------------------------------
# 🔵 Mantener clases de ViewModel (Login, Register, etc.)
# -----------------------------------------
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# -----------------------------------------
# 🔵 Mantener clases de coroutines
# -----------------------------------------
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# -----------------------------------------
# 🔵 Retrofit + OkHttp (Networking)
# -----------------------------------------
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.Platform$Java8

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# -----------------------------------------
# 🔵 Mantener modelos de Auth y App
# -----------------------------------------
-keep class com.finedu.app.auth.data.** { *; }
-keep interface com.finedu.app.auth.data.** { *; }

-keep class com.finedu.app.data.** { *; }
-keep interface com.finedu.app.data.** { *; }

# IA FinEdu
-keep class com.finedu.app.ai.data.** { *; }
-keep interface com.finedu.app.ai.data.** { *; }

# -----------------------------------------
# 🔵 Gson Annotations
# -----------------------------------------
-keep class com.google.gson.annotations.** { *; }

# Evitar warnings innecesarios
-dontwarn com.google.gson.**

# -----------------------------------------
# 🔵 Jetpack Compose
# -----------------------------------------
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# -----------------------------------------
# 🔵 Mantener modelos del backend (responses)
# -----------------------------------------
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# -----------------------------------------
# 🔵 Evitar ofuscación de sealed classes
# -----------------------------------------
-keep class ** extends kotlin.sealed.** { *; }

# -----------------------------------------
# 🔵 Evitar eliminar clases usadas por reflexión
# -----------------------------------------
-keepclassmembers class * {
    @kotlin.Metadata *;
}

# --- Errores de validación del registro ---
-keep class com.finedu.app.auth.register.ValidationErrorResponse { *; }
-keep class com.finedu.app.auth.register.ValidationError { *; }
-keep class com.finedu.app.auth.register.FieldError { *; }

