import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Generates the screenshotTest wrappers for the previews annotated with `@ScreenshotTest`.
 *
 * It reads the Kotlin sources instead of the compiled classes, so it doesn't depend on compiling main.
 * Each suite becomes a `<Suite>ScreenshotTests` class with one `@PreviewTest` function per preview, named after it.
 * Previews with `@PreviewLightDark` or a night `uiMode` get a dark `@Preview` too, next to or instead of the light one.
 * It also lists the reference images the generated tests expect, for [OrphanScreenshotReferencesTask].
 */
@CacheableTask
abstract class GenerateScreenshotTestsTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:IgnoreEmptyDirectories
    abstract val sources: ConfigurableFileCollection

    /** Package of the generated test classes. */
    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /** Expected reference images, one `<package path>/<class>/<test>_<hash>` per line. */
    @get:OutputFile
    abstract val referencesFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val previews = sources.files.sortedBy { it.path }
            .flatMap { ScreenshotTestsGenerator.parsePreviews(it.readText(), it.path) }
        ScreenshotTestsGenerator.checkDuplicates(previews)

        val packageDir = outputDir.get().asFile.resolve(packageName.get().replace('.', '/'))
        packageDir.deleteRecursively()
        packageDir.mkdirs()
        ScreenshotTestsGenerator.SUITES.forEach { suite ->
            packageDir.resolve("${ScreenshotTestsGenerator.className(suite)}.kt")
                .writeText(ScreenshotTestsGenerator.generateSuite(packageName.get(), suite, previews, name))
        }
        val references = ScreenshotTestsGenerator.expectedReferences(packageName.get(), previews)
        referencesFile.get().asFile.writeText(references.joinToString("\n", postfix = "\n"))
    }
}
