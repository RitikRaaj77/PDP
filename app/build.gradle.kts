plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.pdp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pdp"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Remove hardcoded Compose versions to avoid conflicts with BOM
    implementation("androidx.activity:activity-compose") // Version controlled by BOM
    implementation("androidx.compose.material3:material3") // Version controlled by BOM
    implementation("androidx.compose.ui:ui") // Version controlled by BOM
    implementation("androidx.compose.runtime:runtime") // Version controlled by BOM

    // Update Coil to the latest version
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Retrofit and OkHttp (align versions)
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // Latest as of May 2025
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Compatible with Retrofit 2.11.0

    // Coroutines (already latest)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
}