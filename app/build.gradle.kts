import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val envFile = rootProject.file(".env")
val envProps = Properties()
if (envFile.exists()) {
    envProps.load(envFile.inputStream())
}

android {
    namespace = "com.zerointrusion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zerointrusion"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Env variables
        buildConfigField("String", "API_REGISTRATION", "\"${envProps["API_REGISTRATION"] ?: ""}\"")
        buildConfigField("String", "API_LOGIN", "\"${envProps["API_LOGIN"] ?: ""}\"")
        buildConfigField("String", "API_DEVICE_REGISTRATION", "\"${envProps["API_DEVICE_REGISTRATION"] ?: ""}\"")
        buildConfigField("String", "API_RECOVERY_SETTINGS", "\"${envProps["API_RECOVERY_SETTINGS"] ?: ""}\"")
        buildConfigField("String", "API_ALLOW_APPLICATION_LIST", "\"${envProps["API_ALLOW_APPLICATION_LIST"] ?: ""}\"")
        buildConfigField("String", "API_ALLOW_DELETE_DOMAIN", "\"${envProps["API_ALLOW_DELETE_DOMAIN"] ?: ""}\"")
        buildConfigField("String", "API_ALLOW_DELETE_APPLICATIONS", "\"${envProps["API_ALLOW_DELETE_APPLICATIONS"] ?: ""}\"")
        buildConfigField("String", "API_ALLOW_EDIT_APPLICATIONS", "\"${envProps["API_ALLOW_EDIT_APPLICATIONS"] ?: ""}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // QR scanning
    implementation(libs.zxing.android.embedded)

    // LazySodium
    implementation(files("libs/jna-5.8.0.aar"))
    implementation("com.goterl:lazysodium-android:5.1.0") {
        exclude(group = "net.java.dev.jna", module = "jna")
    }
    implementation(libs.lazysodium.android.v502) {
        exclude(group = "net.java.dev.jna", module = "jna")
    }

    // Networking
    implementation(libs.okhttp)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-messaging")
}
