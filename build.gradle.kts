plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.ejer_bd_p"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ejer_bd_p"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.17" // Asegúrate de que esta versión sea compatible con la BOM
    }

}

dependencies {

    // Core y KTX
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.appcompat:appcompat:1.7.0")

    // Compose
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.compose.material3:material3:1.3.0") // Última versión estable para Material 3
    implementation ("androidx.compose.ui:ui:1.7.0") // Asegura compatibilidad con Compose
    implementation ("androidx.compose.ui:ui-graphics:1.7.0")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.7.0")
    implementation ("androidx.compose.material:material-icons-core:1.7.0") // Íconos básicos
    implementation ("androidx.compose.material:material-icons-extended:1.7.0") // Íconos extendidos
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    // SQLite (ya incluida implícitamente en Android, pero asegúrate)
    implementation ("androidx.sqlite:sqlite:2.4.0")

    // Lifecycle para Compose
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // Coil para imágenes (si lo usas)
    implementation ("io.coil-kt:coil-compose:2.5.0")

}