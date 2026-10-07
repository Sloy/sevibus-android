import ScreenshotTestsGenerator.AnnotatedPreview
import org.gradle.api.GradleException
import org.junit.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.message
import strikt.assertions.single

class ScreenshotTestsGeneratorTest {

    // region parsePreviews

    @Test
    fun `parses a light preview with its package`() {
        val previews = parse(
            """
            package com.example.feature

            @ScreenshotTest(ScreenshotSuite.Components)
            @Preview
            @Composable
            internal fun MyComponentPreview() {
            }
            """
        )

        expectThat(previews).single().isEqualTo(
            AnnotatedPreview(
                suite = "Components",
                light = true,
                dark = false,
                name = "MyComponentPreview",
                fqName = "com.example.feature.MyComponentPreview",
            )
        )
    }

    @Test
    fun `uses the bare name when the file has no package`() {
        val previews = parse(
            """
            @ScreenshotTest(ScreenshotSuite.Screens)
            @Preview
            @Composable
            fun MyScreenPreview() {}
            """
        )

        expectThat(previews.map { it.fqName }).containsExactly("MyScreenPreview")
    }

    @Test
    fun `accepts the suite as a named argument`() {
        val previews = parse(
            """
            @ScreenshotTest(suite = ScreenshotSuite.Screens)
            @Preview
            @Composable
            internal fun MyScreenPreview() {}
            """
        )

        expectThat(previews.map { it.suite }).containsExactly("Screens")
    }

    @Test
    fun `generates light and dark for PreviewLightDark, with and without parentheses`() {
        val previews = parse(
            """
            @ScreenshotTest(ScreenshotSuite.Components)
            @PreviewLightDark
            @Composable
            internal fun FirstPreview() {}

            @ScreenshotTest(ScreenshotSuite.Components)
            @PreviewLightDark()
            @Composable
            internal fun SecondPreview() {}
            """
        )

        expectThat(previews.map { it.light to it.dark }).containsExactly(true to true, true to true)
    }

    @Test
    fun `generates light and dark for a pair of previews with day and night uiMode`() {
        val previews = parse(
            """
            @ScreenshotTest(ScreenshotSuite.Components)
            @Preview(showBackground = true, name = "Light", uiMode = UI_MODE_NIGHT_NO)
            @Preview(showBackground = true, name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
            @Composable
            internal fun IconPreview() {}
            """
        )

        expectThat(previews.map { it.light to it.dark }).containsExactly(true to true)
    }

    @Test
    fun `generates only dark for a night preview`() {
        val previews = parse(
            """
            @ScreenshotTest(ScreenshotSuite.Components)
            @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
            @Composable
            internal fun NightPreview() {}
            """
        )

        expectThat(previews.map { it.light to it.dark }).containsExactly(false to true)
    }

    @Test
    fun `detects a night uiMode after a name with parentheses`() {
        val previews = parse(
            """
            @ScreenshotTest(ScreenshotSuite.Components)
            @Preview(name = "Night (dark)", uiMode = UI_MODE_NIGHT_YES)
            @Composable
            internal fun NightPreview() {}
            """
        )

        expectThat(previews.map { it.light to it.dark }).containsExactly(false to true)
    }

    @Test
    fun `parses several annotated previews and skips the rest`() {
        val previews = parse(
            """
            package com.example

            @Preview
            @Composable
            internal fun NotTestedPreview() {}

            @ScreenshotTest(ScreenshotSuite.Components)
            @Preview(widthDp = 300)
            @Composable
            internal fun FirstPreview() {}

            @ScreenshotTest(ScreenshotSuite.Screens)
            @Preview
            @Composable
            internal fun SecondPreview() {}
            """
        )

        expectThat(previews.map { it.suite to it.name })
            .containsExactly("Components" to "FirstPreview", "Screens" to "SecondPreview")
    }

    @Test
    fun `ignores the annotation in comments`() {
        val previews = parse(
            """
            /**
             * @ScreenshotTest(ScreenshotSuite.Components)
             */
            // @ScreenshotTest(ScreenshotSuite.Components)
            @Preview
            @Composable
            internal fun MyComponentPreview() {}
            """
        )

        expectThat(previews).isEmpty()
    }

    @Test
    fun `fails on an unknown suite`() {
        expectThrows<GradleException> {
            parse(
                """
                @ScreenshotTest(ScreenshotSuite.Widgets)
                @Preview
                @Composable
                internal fun MyPreview() {}
                """
            )
        }.message.isEqualTo(
            "MyFile.kt:1: @ScreenshotTest needs one of ScreenshotSuite.Components, ScreenshotSuite.Screens, " +
                "found 'ScreenshotSuite.Widgets'"
        )
    }

    @Test
    fun `fails on a private preview, pointing to its line`() {
        expectThrows<GradleException> {
            parse(
                """
                package com.example

                @ScreenshotTest(ScreenshotSuite.Components)
                @Preview
                @Composable
                private fun MyPreview() {}
                """
            )
        }.message.isEqualTo("MyFile.kt:3: @ScreenshotTest preview MyPreview can't be private, make it internal")
    }

    @Test
    fun `fails on a preview with parameters`() {
        expectThrows<GradleException> {
            parse(
                """
                @ScreenshotTest(ScreenshotSuite.Components)
                @Preview
                @Composable
                internal fun MyPreview(@PreviewParameter(Provider::class) value: String) {}
                """
            )
        }.message.isEqualTo("MyFile.kt:1: @ScreenshotTest preview MyPreview can't have parameters")
    }

    // endregion

    // region checkDuplicates

    @Test
    fun `allows the same name in different suites`() {
        ScreenshotTestsGenerator.checkDuplicates(
            listOf(preview("Components", "SamePreview"), preview("Screens", "SamePreview"))
        )
    }

    @Test
    fun `fails on the same name in a suite`() {
        expectThrows<GradleException> {
            ScreenshotTestsGenerator.checkDuplicates(
                listOf(
                    preview("Components", "SamePreview", fqName = "a.SamePreview"),
                    preview("Components", "SamePreview", fqName = "b.SamePreview"),
                )
            )
        }.message.isEqualTo("Duplicated preview name SamePreview in suite Components: a.SamePreview, b.SamePreview")
    }

    // endregion

    // region generateSuite

    @Test
    fun `generates the suite class with the previews of that suite, sorted by name`() {
        val previews = listOf(
            preview("Components", "BPreview", light = true, dark = true),
            preview("Screens", "ScreenPreview"),
            preview("Components", "APreview", light = true, dark = false),
        )

        val source = ScreenshotTestsGenerator.generateSuite("com.example", "Components", previews, "generateTask")

        expectThat(source).isEqualTo(
            """
            // Generated by generateTask from the @ScreenshotTest previews. Don't edit.
            package com.example

            import android.content.res.Configuration
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.tooling.preview.Preview
            import com.android.tools.screenshot.PreviewTest

            class ComponentsScreenshotTests {

                @Preview(locale = "es")
                @PreviewTest
                @Composable
                fun APreview() {
                    com.example.APreview()
                }

                @Preview(locale = "es")
                @Preview(locale = "es", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
                @PreviewTest
                @Composable
                fun BPreview() {
                    com.example.BPreview()
                }
            }

            """.trimIndent()
        )
    }

    // endregion

    // region expectedReferences

    @Test
    fun `lists one reference per light and dark variant`() {
        val previews = listOf(
            preview("Screens", "ScreenPreview", light = false, dark = true),
            preview("Components", "BothPreview", light = true, dark = true),
            preview("Components", "LightPreview", light = true, dark = false),
        )

        expectThat(ScreenshotTestsGenerator.expectedReferences("com.example", previews)).containsExactly(
            "com/example/ComponentsScreenshotTests/BothPreview_b2db1d68",
            "com/example/ComponentsScreenshotTests/BothPreview_f6f1fda3",
            "com/example/ComponentsScreenshotTests/LightPreview_b2db1d68",
            "com/example/ScreensScreenshotTests/ScreenPreview_f6f1fda3",
        )
    }

    // endregion

    // region findOrphans

    @Test
    fun `finds the references no test expects, whatever their index`() {
        val expected = setOf(
            "com/example/ComponentsScreenshotTests/APreview_b2db1d68",
            "com/example/ComponentsScreenshotTests/BPreview_b2db1d68",
        )
        val references = listOf(
            "com/example/ComponentsScreenshotTests/BPreview_b2db1d68_0.png",
            "com/example/ComponentsScreenshotTests/APreview_b2db1d68_0.png",
            "com/example/ComponentsScreenshotTests/APreview_b2db1d68_1.png",
            "com/example/ComponentsScreenshotTests/APreview_f6f1fda3_0.png",
            "com/example/ComponentsScreenshotTests/DeletedPreview_b2db1d68_0.png",
            "com/example/ComponentsScreenshotTests/.DS_Store",
        )

        expectThat(ScreenshotTestsGenerator.findOrphans(expected, references)).containsExactly(
            "com/example/ComponentsScreenshotTests/APreview_f6f1fda3_0.png",
            "com/example/ComponentsScreenshotTests/DeletedPreview_b2db1d68_0.png",
        )
    }

    @Test
    fun `finds a reference left in another suite when a preview changes suite`() {
        val expected = setOf("com/example/ScreensScreenshotTests/MovedPreview_b2db1d68")
        val references = listOf(
            "com/example/ComponentsScreenshotTests/MovedPreview_b2db1d68_0.png",
            "com/example/ScreensScreenshotTests/MovedPreview_b2db1d68_0.png",
        )

        expectThat(ScreenshotTestsGenerator.findOrphans(expected, references))
            .containsExactly("com/example/ComponentsScreenshotTests/MovedPreview_b2db1d68_0.png")
    }

    // endregion

    private fun parse(source: String) = ScreenshotTestsGenerator.parsePreviews(source.trimIndent(), "MyFile.kt")

    private fun preview(
        suite: String,
        name: String,
        light: Boolean = true,
        dark: Boolean = false,
        fqName: String = "com.example.$name",
    ) = AnnotatedPreview(suite = suite, light = light, dark = dark, name = name, fqName = fqName)
}
