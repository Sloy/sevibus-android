import com.android.compose.screenshot.tasks.PreviewScreenshotValidationTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.screenshot)
    id("sevibus.screenshot-tests")
}

android {
    namespace = "com.sloy.debugmenu"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

tasks.withType<PreviewScreenshotValidationTask>().configureEach {
    testEngineInput.threshold.set(0.01f)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.icons)
    implementation(libs.kotlinx.serialization)
    implementation(libs.coroutines.android)
    implementation(libs.playServices.codeScanner)
    api(libs.okhttp)

    debugImplementation(libs.androidx.ui.tooling)

    screenshotTestImplementation(libs.compose.screenshot.validation)
    screenshotTestImplementation(libs.androidx.ui.tooling)
    screenshotTestImplementation(platform(libs.androidx.compose.bom))
    screenshotTestImplementation(libs.androidx.ui)

    testImplementation(libs.junit)
    testImplementation(libs.strikt)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.testing)
}
