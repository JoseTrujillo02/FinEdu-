# --- Reglas para Retrofit y OkHttp (Red) ---
# (Evita que se eliminen clases que Retrofit usa internamente)
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.Platform$Java8
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }

# Regla para tus modelos de Auth (LoginResponse, User, Tokens, etc.)
-keep class com.finedu.app.auth.data.** { *; }
-keep interface com.finedu.app.auth.data.** { *; }

# Regla para tus modelos de App (UserSessionData)
-keep class com.finedu.app.data.** { *; }
-keep interface com.finedu.app.data.** { *; }

# Regla para tus modelos de IA (FinancialAiService)
-keep class com.finedu.app.ai.data.** { *; }
-keep interface com.finedu.app.ai.data.** { *; }

# Regla para las anotaciones de GSON (SerializedName)
-keepattributes Annotation
-keep class com.google.gson.annotations.** { *; }

# --- Reglas para Retrofit y OkHttp (Red) ---
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.Platform$Java8
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }

# --- ¡Reglas para tus Modelos de GSON! ---
# Esto le dice a ProGuard que no toque NINGUNA clase en tu paquete 'data'.
-keep class com.finedu.app.auth.data.** { *; }
-keep interface com.finedu.app.auth.data.** { *; }

# Regla para las anotaciones de GSON (SerializedName)
-keepattributes Annotation
-keep class com.google.gson.annotations.**{*;}