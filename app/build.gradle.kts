plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.firebasePerformance)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose.compiler)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.sloy.sevibus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sloy.sevibus"
        minSdk = 26
        targetSdk = 36
        versionCode = VersionConfig.versionCode
        versionName = VersionConfig.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        register("release") {
            val realKeystoreFile = file("../certs/release.keystore")
            val fakeKeystoreFile = file("../certs/fakeRelease.keystore")

            if (realKeystoreFile.exists() && System.getenv("KEYSTORE_PASSWORD") != null) {
                storeFile = realKeystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "no_value"
                keyAlias = System.getenv("KEY_ALIAS") ?: "no_value"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "no_value"
            } else {
                storeFile = fakeKeystoreFile
                storePassword = "banana"
                keyAlias = "banana"
                keyPassword = "banana"
            }
        }
        named("debug") {
            storeFile = file("../certs/debug.keystore")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs["release"]
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs["debug"]
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
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.icons)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.composeunstyled)

    implementation(libs.accompanist.permissions)
    implementation(libs.coil)
    implementation(libs.lottie)
    implementation(libs.reorderable)

    implementation(libs.coroutines.android)
    implementation(libs.coroutines.playServices)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)
    implementation(libs.playServices.location)
    implementation(libs.maps)
    implementation(libs.credentials)
    implementation(libs.credentials.playServices)
    implementation(libs.credentials.googleIdentity)
    implementation(libs.play.review)

    implementation(libs.amplitude)
    implementation(libs.statsig.sdk)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.kotlinx.serialization)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.compose)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.inAppUpdateCompose.material)

    debugImplementation(project(":debug-menu"))
    releaseImplementation(project(":debug-menu-noop"))

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.chucker)


    testImplementation(libs.junit)
    testImplementation(libs.strikt)
    testImplementation(libs.coroutines.testing)
    testImplementation(libs.mockito.kotlin)
}

secrets {
    propertiesFileName = "secret.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}
