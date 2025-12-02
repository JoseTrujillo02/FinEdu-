// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)

    // Datadog Gradle Plugin (para subir mappings y auto-instrumentar Compose)
    //id("com.datadoghq.dd-sdk-android-gradle-plugin") version "1.21.0"
}

android {
    namespace = "com.finedu.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.finedu.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = "1.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Datadog
        buildConfigField("String", "DD_CLIENT_TOKEN", "\"pubed8d8fc477feb1463798562185e73aa9\"")
        buildConfigField("String", "DD_RUM_APP_ID", "\"a3ffc84b-73bd-432d-b13a-860122786575\"")
        buildConfigField("String", "DD_ENV", "\"dev\"") // o "prod" para release
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        // Necesario para usar buildConfigField (Datadog, etc.)
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*"
            )
        }
    }

    // (Opcional) configuración del plugin Datadog para Compose
    // Si luego quieres auto-instrumentación de Compose:
    // datadog {
    //     composeInstrumentation = "AUTO" // AUTO, ANNOTATION o DISABLE
    // }
}

dependencies {
    // Core + lifecycle + activity-compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.text)

    // Runtime LiveData (sin versión para usar BOM)
    implementation("androidx.compose.runtime:runtime-livedata")
    // Íconos extendidos (también sin versión, se resuelve con el BOM)
    implementation("androidx.compose.material:material-icons-extended")
    // ui-graphics (podrías cambiarlo a alias si quieres)
    implementation("androidx.compose.ui:ui-graphics")

    // Retrofit (usa solo las versiones del catalog)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

    // OkHttp (solo catalog)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // ViewModel + navegación con Hilt
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Test UI
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Navigation (solo una vez, usando catalog)
    implementation(libs.androidx.navigation.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Room (kapt)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Security Crypto
    implementation(libs.androidx.security.crypto)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Biometría
    implementation(libs.androidx.biometric)

    // Firebase (BoM + módulos)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.messaging.ktx)

    // Tests base
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Datadog – RUM, Logs, OkHttp usando el catalog
    implementation(libs.dd.sdk.android.rum)
    implementation(libs.dd.sdk.android.logs)
    implementation(libs.dd.sdk.android.okhttp)
}
