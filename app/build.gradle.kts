plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.kotlin.serialization)

}

android {
    namespace = "com.dabi.opensky"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dabi.opensky"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

// Explicitly enable experimental APIs for this module
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-receivers",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi"
        )
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
    implementation(libs.androidx.navigation.common.android)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.androidx.datastore.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Dependency injection
    implementation("com.google.dagger:hilt-android:2.57")
    ksp("com.google.dagger:hilt-android-compiler:2.57")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //Nav composable
    implementation("androidx.navigation:navigation-compose:2.9.4")

    //Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.7")


    // Retrofit
    api(libs.retrofit)

    //Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // JavaX Inject
//    implementation(libs.javax.inject)

    // Moshi Retrofit Converter
    api(libs.retrofit.converter.moshi)

    implementation(libs.moshi.kotlin)
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")
    implementation(libs.okhttp.loggingInterceptor)
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

}