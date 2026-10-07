plugins {
    `kotlin-dsl`
}

dependencies {
    // The app applies the Android Gradle Plugin, so it's only needed to compile
    compileOnly(libs.android.gradle.api)

    testImplementation(libs.junit)
    testImplementation(libs.strikt)
}

gradlePlugin {
    plugins {
        register("screenshotTests") {
            id = "sevibus.screenshot-tests"
            implementationClass = "ScreenshotTestsPlugin"
        }
    }
}
