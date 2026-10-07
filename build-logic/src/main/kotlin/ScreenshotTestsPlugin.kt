import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.HostTestBuilder
import com.android.build.api.variant.SourceDirectories
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Generates the screenshotTest wrappers from the @ScreenshotTest previews in main, and checks for orphan references.
 *
 * Only the screenshotTest compilation depends on the generator, so regular builds don't run it.
 * Reference images no test uses anymore fail validation and are deleted when updating the references.
 * The generated test classes go in the app namespace.
 */
class ScreenshotTestsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.android.application") {
            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java).onVariants { variant ->
                val screenshotTest = variant.hostTests[HostTestBuilder.SCREENSHOT_TEST_TYPE] ?: return@onVariants
                project.registerTasks(variant.name, variant.namespace, screenshotTest.sources.kotlin)
            }
        }
    }

    private fun Project.registerTasks(
        variant: String,
        namespace: Provider<String>,
        kotlinSources: SourceDirectories.Flat?,
    ) {
        val variantName = variant.replaceFirstChar { it.uppercase() }
        val generateTask = tasks.register("generate${variantName}ScreenshotTests", GenerateScreenshotTestsTask::class.java) {
            sources.from(fileTree("src/main/java") { include("**/*.kt") })
            packageName.set(namespace)
            referencesFile.set(layout.buildDirectory.file("intermediates/screenshotReferences/$variant/expected.txt"))
        }
        kotlinSources?.addGeneratedSourceDirectory(generateTask, GenerateScreenshotTestsTask::outputDir)

        val deleteTaskName = "delete${variantName}OrphanScreenshotReferences"
        fun registerOrphanTask(name: String, delete: Boolean) =
            tasks.register(name, OrphanScreenshotReferencesTask::class.java) {
                expectedReferences.set(generateTask.flatMap { it.referencesFile })
                referenceDir.set(layout.projectDirectory.dir("src/screenshotTest$variantName/reference"))
                baseDir.set(rootProject.layout.projectDirectory)
                this.delete.set(delete)
                this.deleteTaskName.set(deleteTaskName)
                reportFile.set(layout.buildDirectory.file("reports/screenshotTest/orphans-$variant.txt"))
            }
        val checkTask = registerOrphanTask("check${variantName}ScreenshotReferences", delete = false)
        val deleteTask = registerOrphanTask(deleteTaskName, delete = true)
        tasks.matching { it.name == "validate${variantName}ScreenshotTest" }.configureEach { finalizedBy(checkTask) }
        tasks.matching { it.name == "update${variantName}ScreenshotTest" }.configureEach { finalizedBy(deleteTask) }
    }
}
