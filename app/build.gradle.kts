plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "com.example.compraapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.compraapp"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}
dependencies {
// ROOM
    implementation(libs.androidx.room.runtime) //implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.room.ktx) // kapt("androidx.room:room-compiler:2.6.1")
    kapt(libs.androidx.room.compiler) // implementation("androidx.room:room-ktx:2.6.1")

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

// GLIDE
    implementation(libs.glide) // implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor(libs.compiler) // annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

// MATERIAL DESIGN TextInputLayout y TextInputEditText
    implementation(libs.material.v1110) // implementation("com.google.android.material:material:1.11.0")

// FLOATING ACTION BUTTON
    implementation(libs.material) //implementation("com.google.android.material:material:1.11.0")

// LIVE DATA
    implementation(libs.androidx.lifecycle.runtime.ktx) // implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation(libs.androidx.lifecycle.livedata.ktx) // implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)

    implementation (libs.glide.transformations) // implementation ("jp.wasabeef:glide-transformations:4.3.0")
    implementation (libs.gpuimage) // implementation ("jp.co.cyberagent.android:gpuimage:2.1.0")

}