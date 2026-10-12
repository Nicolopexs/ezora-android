import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Llave de firma: keystore.properties en la raíz (NO se sube a git) o variables de entorno (CI).
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun signingValue(key: String, env: String): String? =
    keystoreProps.getProperty(key) ?: System.getenv(env)

android {
    namespace = "com.ezora.android"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        // La APK de Capacitor llegó a versionCode 7 (1.6): la nativa empieza en 100 (2.0.0)
        versionCode = 100
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val storePath = signingValue("storeFile", "EZORA_KEYSTORE_FILE")
            if (storePath != null) {
                storeFile = rootProject.file(storePath)
                storePassword = signingValue("storePassword", "EZORA_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "EZORA_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "EZORA_KEY_PASSWORD")
            }
        }
    }

    flavorDimensions += "client"
    productFlavors {
        // Mismo applicationId y misma firma que la APK actual de Cassará: se instala como actualización.
        create("cassara") {
            dimension = "client"
            applicationId = "com.cassara.app"
            buildConfigField("String", "API_BASE_URL", "\"https://cassaraecapp.com/api/v1/\"")
            buildConfigField("String", "BRAND", "\"cassara\"")
            buildConfigField("String", "TERMS_URL", "\"https://cassaraecuador.com/terminos\"")
            resValue("string", "app_name", "Cassará")
        }
        create("ezora") {
            dimension = "client"
            applicationId = "com.ezora.app"
            // Dominio de EZORA: pendiente (lo define Nicolás)
            buildConfigField("String", "API_BASE_URL", "\"https://api.ezora.app/api/v1/\"")
            buildConfigField("String", "BRAND", "\"ezora\"")
            buildConfigField("String", "TERMS_URL", "\"https://ezora.app/terminos\"")
            resValue("string", "app_name", "EZORA")
        }
    }

    buildTypes {
        debug {
            // Se instala junto a la app real para probar sin reemplazarla
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val release = signingConfigs.getByName("release")
            if (release.storeFile != null) signingConfig = release
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
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.security.crypto)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.play.services.location)
    implementation(libs.osmdroid)

    testImplementation(libs.junit)
}
