plugins {
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.androidLibrary)
}

description = "Secure Reliable Transport (SRT) Protocol with Kotlin coroutines for Android"
configurePublication()

android {
    namespace = "io.github.thibaultbee.srtdroid.ktx"
    compileSdk = AndroidVersions.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidVersions.MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    api(project(":srtdroid-core"))

    implementation(libs.core.ktx)

    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

