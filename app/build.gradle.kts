plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "id.antasari.cityreport"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.antasari.cityreport"
        minSdk = 24
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
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
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
}

dependencies {
    // ===== COMPOSE BOM (SATU SAJA) =====
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core & lifecycle (versi aman untuk AGP baru + compileSdk 36)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose UI + Material 3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Material (M2) untuk SmallTopAppBar (kalau masih dipakai)
    implementation("androidx.compose.material:material")

    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Retrofit + Gson + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Location services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Appwrite SDK
    implementation("io.appwrite:sdk-for-android:6.0.0")

    // OSMDroid for map view
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Coil - Image loading library
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Vico - Charts library for dashboard
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")
    
    // Accompanist - Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:4.3.0")
    
    
    // QR Code generation
    implementation("com.google.zxing:core:3.5.2")
    
    // DataStore for local preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
