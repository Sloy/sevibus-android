package com.sloy.sevibus.ui.preview

/**
 * Adds the annotated preview to the screenshot tests.
 *
 * The `generate<Variant>ScreenshotTests` Gradle task reads the main sources and generates a `@PreviewTest` wrapper
 * calling the preview in the [suite] class. The test is named after the preview function.
 * The preview must be `internal` or `public` and have no parameters.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class ScreenshotTest(val suite: ScreenshotSuite)

enum class ScreenshotSuite {
    /** Reusable components and screen sections, generated in `ComponentsScreenshotTests`. */
    Components,

    /** Full screens, generated in `ScreensScreenshotTests`. */
    Screens,
}
