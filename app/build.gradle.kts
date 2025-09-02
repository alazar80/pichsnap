plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.pichsnap"
    compileSdk = 35

    // Generate BuildConfig.java
    buildFeatures { buildConfig = true }

    defaultConfig {
        applicationId = "com.example.pichsnap"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- values used in ApiClient/DeckRepository ---
        buildConfigField("String", "BACKEND_BASE_URL", "\"http://10.0.2.2:3000/\"")
        buildConfigField("String", "BACKEND_AUTH_TOKEN", "\"CHANGE_ME_DEV_TOKEN\"")
    }

    buildTypes {
        debug {
            // keep local base URL for emulator
            buildConfigField("String", "BACKEND_BASE_URL", "\"http://10.0.2.2:3000/\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BACKEND_BASE_URL", "\"https://api.example.com/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Retrofit / OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room (Java)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
