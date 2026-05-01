import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

// Read API keys from gradle.properties or local.properties.
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

// Prefer environment/local.properties for secrets (avoid committing to VCS).
val geminiApiKey = providers.environmentVariable("GEMINI_API_KEY").orNull
    ?: localProperties.getProperty("GEMINI_API_KEY")
    ?: (project.findProperty("GEMINI_API_KEY") as? String)
    ?: ""

val escapedGeminiApiKey = geminiApiKey.replace("\"", "\\\"")
val mailjetApiKey = (localProperties.getProperty("MAILJET_API_KEY") ?: "").replace("\\", "\\\\").replace("\"", "\\\"")
val mailjetSecretKey = (localProperties.getProperty("MAILJET_SECRET_KEY") ?: "").replace("\\", "\\\\").replace("\"", "\\\"")
val mailjetFromEmail = (localProperties.getProperty("MAILJET_FROM_EMAIL") ?: "").replace("\\", "\\\\").replace("\"", "\\\"")
val mailjetFromName = (localProperties.getProperty("MAILJET_FROM_NAME") ?: "Yanagh").replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.example.yanagh"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.yanagh"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Used by Gemini client (see GeminiApi.java).
        buildConfigField("String", "GEMINI_API_KEY", "\"$escapedGeminiApiKey\"")
        // Mailjet transactional email.
        buildConfigField("String", "MAILJET_API_KEY", "\"$mailjetApiKey\"")
        buildConfigField("String", "MAILJET_SECRET_KEY", "\"$mailjetSecretKey\"")
        buildConfigField("String", "MAILJET_FROM_EMAIL", "\"$mailjetFromEmail\"")
        buildConfigField("String", "MAILJET_FROM_NAME", "\"$mailjetFromName\"")

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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
