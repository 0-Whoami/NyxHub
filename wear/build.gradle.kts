import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}
tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += listOf("-Xskip-prerelease-check")
        }
    }
}
android {
    namespace = "com.nyxhub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nyxhub"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("x86_64", "armeabi-v7a")
            isUniversalApk = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(libs.horologist.compose.layout)
    implementation(libs.material.icons.extended)
    implementation(libs.play.services.wearable)
    implementation(libs.activity.compose)

    // Compose for Wear OS Dependencies
    implementation(libs.compose.material)
    implementation(project(":constants"))
    implementation(libs.ui.tooling.preview.android)
}