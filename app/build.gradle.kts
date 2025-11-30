plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.dymessagelite"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.dymessagelite"
        minSdk = 28
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
        viewBinding = true
    }
}

dependencies {
    val room_version = "2.8.4"
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("io.github.scwang90:refresh-layout-kernel:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-classics:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-radar:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-falsify:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-material:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-two-level:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-footer-ball:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-footer-classics:3.0.0-alpha")

    implementation("com.google.code.gson:gson:2.10.1")


    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:${room_version}")
    ksp("androidx.room:room-compiler:$room_version")
}