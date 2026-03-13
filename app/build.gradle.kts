plugins {
    alias(libs.plugins.android.application)
    // id("com.google.gms.google-services") // Uncomment after adding google-services.json
}

android {
    namespace = "com.example.bariaxorjak"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.bariaxorjak"
        minSdk = 26
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Firebase BoM (Commented out until google-services.json is added)
    // implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    
    // Firebase dependencies (Commented out until google-services.json is added)
    // implementation("com.google.firebase:firebase-analytics")
    // implementation("com.google.firebase:firebase-firestore")
    // implementation("com.google.firebase:firebase-auth")
    // implementation("com.google.firebase:firebase-storage")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}