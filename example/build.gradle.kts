plugins {
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "io.github.thibaultbee.srtdroid.example"
    compileSdk = AndroidVersions.COMPILE_SDK

    defaultConfig {
        minSdk = 21
        versionCode = 1
        versionName = "$version"

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    namespace = "io.github.thibaultbee.srtdroid.example"
}

dependencies {
    implementation(project(":srtdroid-ktx"))
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.multidex)
    implementation(libs.preference.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.guava)

    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

