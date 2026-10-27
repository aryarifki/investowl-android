import java.util.Properties
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "me.cugarete.investowl"
    compileSdk = 35

    defaultConfig {
        applicationId = "me.cugarete.investowl"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("SIGNING_KEY_STORE_PATH")
            val keystoreFile = when {
                !keystorePath.isNullOrEmpty() -> {
                    val f = File(keystorePath)
                    if (f.isAbsolute) f else File(rootDir, keystorePath).absoluteFile
                }
                File(rootDir, "release.keystore").exists() -> File(rootDir, "release.keystore").absoluteFile
                File(projectDir, "release.keystore").exists() -> File(projectDir, "release.keystore").absoluteFile
                else -> null
            }
            
            if (keystoreFile != null && keystoreFile.exists()) {
                storeFile = keystoreFile.absoluteFile
                storePassword = System.getenv("SIGNING_STORE_PASSWORD") ?: "investowl123"
                keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: "investowl"
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: "investowl123"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile!!.exists()) {
                signingConfig = releaseSigning
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(project(":feature:home"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:ticker"))
    implementation(project(":feature:ownership"))
    implementation(project(":feature:konglo"))
    implementation(project(":feature:foreign"))
    implementation(project(":feature:whales"))
    implementation(project(":feature:signal"))
    implementation(project(":feature:backtest"))
    implementation(project(":feature:admin"))

    implementation(libs.room.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
