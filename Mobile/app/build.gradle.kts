plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.firstaidfront"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.firstaidfront"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.firstaid.app"
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    implementation (libs.lottie)
    implementation(libs.glide.v4160)
    implementation(libs.androidx.foundation.android)
    annotationProcessor(libs.compiler)

    implementation (libs.glide)
    implementation(libs.appauth)
    implementation(libs.androidx.browser)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation (libs.pierfrancescosoffritti.core)
    implementation (libs.androidx.viewpager2)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("androidx.core:core-ktx:1.7.0")

    implementation ("io.github.sceneview:arsceneview:0.10.0")

    // PyTorch Mobile
    implementation ("org.pytorch:pytorch_android:1.13.1") // Change this line
    implementation ("org.pytorch:pytorch_android_torchvision:1.13.1")  // And this line
    // CameraX
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")


}